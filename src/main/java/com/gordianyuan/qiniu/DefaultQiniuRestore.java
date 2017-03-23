package com.gordianyuan.qiniu;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DefaultQiniuRestore extends AbstractQiniuSupport implements QiniuRestore {

  @Override
  public void execute() {
    Map<String, File> files = getFiles();
    if (files.isEmpty()) {
      log.info("No files need to restore.");
      return;
    }

    uploadFiles(files);
  }

  private void uploadFiles(Map<String, File> files) {
    CountDownLatch latch = new CountDownLatch(files.size());
    AtomicLong failCount = new AtomicLong();
    Stopwatch stopwatch = Stopwatch.createStarted();

    files.forEach((fileKey, file) -> uploadFile(fileKey, file, latch, failCount));

    log.info("Upload finished. Total: {}, Failed: {}. Time elapsed: {}.",
        files.size(), failCount.longValue(), stopwatch);
  }

  private void uploadFile(String fileKey, File file, CountDownLatch latch, AtomicLong failCount) {
    UploadManager uploadManager = createUploadManager();
    String updateToken = createUploadToken();
    String newFileKey = getNewFileKey(fileKey);
    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      uploadManager.put(file, newFileKey, updateToken);
      log.info("Succeed to upload {}, took {} ms", newFileKey, stopwatch.elapsed(TimeUnit.MILLISECONDS));
    } catch (QiniuException e) {
      log.error("failed to upload " + fileKey, e);
      failCount.incrementAndGet();
    } finally {
      latch.countDown();
    }
  }

  private Map<String, File> getFiles() {
    Map<String, File> files = Maps.newHashMap();
    Path fileDir = Paths.get(qiniuConfig.getAbsoluteFileDir());
    try {
      Files.walkFileTree(fileDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          // Ignore mac-specific files
          if (file.getFileName().endsWith(".DS_Store")) {
            return FileVisitResult.CONTINUE;
          }

          String fileKey = getFileKey(file, fileDir);
          files.putIfAbsent(fileKey, file.toFile());
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      log.error("Failed to load files information.", e);
    }

    return ImmutableMap.copyOf(files);
  }

  private String getFileKey(Path file, Path dir) {
    String dirString = StringUtils.appendIfMissing(dir.toString(), "/");
    return StringUtils.removeStart(file.toString(), dirString);
  }

  private String getNewFileKey(String fileKey) {
    fileKey = StringUtils.removeStart(fileKey, qiniuConfig.getOriginalPrefix());
    return Strings.nullToEmpty(qiniuConfig.getPrefix()) + fileKey;
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
