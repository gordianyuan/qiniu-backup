package com.gordianyuan.qiniu;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DefaultQiniuRestore extends AbstractQiniuSupport implements QiniuRestore {

  @Override
  public void execute() {
    Map<String, QiniuFileInfo> fileInfos = loadDataFromFile();
    if (!fileInfos.isEmpty()) {
      uploadFiles(fileInfos);
    }
  }

  private void uploadFiles(Map<String, QiniuFileInfo> qiniuFiles) {
    CountDownLatch latch = new CountDownLatch(qiniuFiles.size());
    AtomicLong failCount = new AtomicLong();
    Stopwatch stopwatch = Stopwatch.createStarted();

    qiniuFiles.values().forEach(file -> uploadFile(file, latch, failCount));

    log.info("Upload finished. Total: {}, Failed: {}. Time elapsed: {}.",
        qiniuFiles.size(), failCount.longValue(), stopwatch);
  }

  private void uploadFile(QiniuFileInfo qiniuFile, CountDownLatch latch, AtomicLong failCount) {
    String fileKey = qiniuFile.getKey();
    log.info("Start upload {}", fileKey);

    File file = Paths.get(qiniuConfig.getFileDir(), fileKey).toFile();
    String newFileKey = getNewFileKey(fileKey);
    String updateToken = createUploadToken();
    UploadManager uploadManager = createUploadManager();
    try {
      try (FileInputStream fis = FileUtils.openInputStream(file)) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        uploadManager.put(fis, newFileKey, updateToken, null, null);
        log.info("Succeed to upload {}, took {} ms", newFileKey, stopwatch.elapsed(TimeUnit.MILLISECONDS));
      }
    } catch (IOException e) {
      log.error("failed to upload " + fileKey, e);
      failCount.incrementAndGet();
    } finally {
      latch.countDown();
    }
  }

  private String getNewFileKey(String fileKey) {
    String newKey = StringUtils.removeStart(fileKey, qiniuConfig.getBackupPrefix());
    return Strings.nullToEmpty(qiniuConfig.getRestorePrefix()) + newKey;
  }

  private UploadManager createUploadManager() {
    Configuration configuration = new Configuration();
    return new UploadManager(configuration);
  }

  private String createUploadToken() {
    Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
    return auth.uploadToken(qiniuConfig.getBucket());
  }

}
