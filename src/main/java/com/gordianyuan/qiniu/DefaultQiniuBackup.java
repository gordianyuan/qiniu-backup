package com.gordianyuan.qiniu;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DefaultQiniuBackup extends AbstractQiniuSupport implements QiniuBackup {

  private static final OkHttpClient httpClient = new OkHttpClient();

  @Override
  public void execute() {
    checkEnvironment();
    Map<String, QiniuFileInfo> fileInfos = getQiniuFiles();
    if (!fileInfos.isEmpty()) {
      downloadFiles(fileInfos);
      saveDataToFile(fileInfos);
    }
  }

  private void checkEnvironment() {
    String fileDir = qiniuConfig.getFileDir();
    if (isNotEmptyDirectory(Paths.get(fileDir))) {
      throw new IllegalArgumentException(String.format("File Directory %s is not empty.", fileDir));
    }
  }

  private Map<String, QiniuFileInfo> getQiniuFiles() {
    String bucket = qiniuConfig.getBucket();
    String prefix = qiniuConfig.getBackupPrefix();
    BucketManager bucketManager = createBucketManager();
    Map<String, QiniuFileInfo> allFileInfos = new ConcurrentHashMap<>();
    BucketManager.FileListIterator fileListIterator = bucketManager.createFileListIterator(bucket, prefix);
    while (fileListIterator.hasNext()) {
      FileInfo[] fileInfos = fileListIterator.next();
      allFileInfos.putAll(Stream.of(fileInfos).collect(Collectors.toMap(f -> f.key, this::createQiniuFile)));
    }
    printQiniuFilesSummary(allFileInfos);
    return allFileInfos;
  }

  private QiniuFileInfo createQiniuFile(FileInfo fileInfo) {
    QiniuFileInfo qiniuFile = new QiniuFileInfo();
    qiniuFile.setKey(fileInfo.key);
    qiniuFile.setHash(fileInfo.hash);
    qiniuFile.setSize(fileInfo.fsize);
    return qiniuFile;
  }

  private void printQiniuFilesSummary(Map<String, QiniuFileInfo> qiniuFiles) {
    String bucket = qiniuConfig.getBucket();
    String prefix = qiniuConfig.getBackupPrefix();
    if (Strings.isNullOrEmpty(prefix)) {
      log.info("Found {} objects on bucket {}.", qiniuFiles.size(), bucket);
    } else {
      log.info("Found {} objects on bucket {} with prefix {}.", qiniuFiles.size(), bucket, prefix);
    }

    long totalFileSize = qiniuFiles.values().stream().parallel().mapToLong(QiniuFileInfo::getSize).sum();
    log.info("Total objects size is {}.", FileUtils.byteCountToDisplaySize(totalFileSize));
  }

  private void downloadFiles(Map<String, QiniuFileInfo> qiniuFiles) {

    CountDownLatch latch = new CountDownLatch(qiniuFiles.size());
    AtomicLong failCount = new AtomicLong();
    Stopwatch stopwatch = Stopwatch.createStarted();

    qiniuFiles.values().forEach(file -> downloadFile(file, latch, failCount));

    try {
      latch.await(1, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    log.info("Download finished. Total: {}, Failed: {}. Time elapsed: {}.",
        qiniuFiles.size(), failCount.longValue(), stopwatch);
  }

  private void downloadFile(QiniuFileInfo file, CountDownLatch latch, AtomicLong failCount) {
    String fileKey = file.getKey();
    log.info("Start download {}", fileKey);

    Path filePath = getFilePath(fileKey);
    Request request = createRequest(fileKey);
    httpClient.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        file.setDownloadStatus(QiniuFileInfo.DownloadStatus.FAILED);
        log.error("Failed to download " + fileKey, e);
        latch.countDown();
      }

      @Override
      public void onResponse(Call call, Response response) {
        try {
          if (response.isSuccessful()) {
            try (BufferedSink sink = Okio.buffer(Okio.sink(filePath))) {
              sink.writeAll(response.body().source());
            }
            long elapsed = System.currentTimeMillis() - response.sentRequestAtMillis();
            log.info("Succeed to download {}, took {} ms.", fileKey, elapsed);
            file.setDownloadStatus(QiniuFileInfo.DownloadStatus.SUCCEED);
          } else {
            log.error("Failed to download {}, code: {}, message: {}", fileKey, response.code(), response.message());
            file.setDownloadStatus(QiniuFileInfo.DownloadStatus.FAILED);
          }
        } catch (IOException e) {
          log.error("Failed to download " + fileKey, e);
          file.setDownloadStatus(QiniuFileInfo.DownloadStatus.FAILED);
        } finally {
          response.body().close();
          latch.countDown();
        }
      }
    });
  }

  private Auth createAuth() {
    return Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
  }

  private Path getFilePath(String key) {
    Path path = Paths.get(qiniuConfig.getFileDir(), key);
    try {
      Files.createParentDirs(path.toFile());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return path;
  }

  private static boolean isNotEmptyDirectory(Path dir) {
    String[] files = dir.toFile().list();
    return files != null && files.length != 0;
  }

  private Request createRequest(String key) {
    String publicUrl = "http://" + qiniuConfig.getDomain() + "/" + key;
    String privateUrl = createAuth().privateDownloadUrl(publicUrl);
    return new Request.Builder().url(privateUrl).build();
  }

}
