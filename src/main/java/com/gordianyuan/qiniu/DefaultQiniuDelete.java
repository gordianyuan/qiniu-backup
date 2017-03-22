package com.gordianyuan.qiniu;

import com.google.common.base.Stopwatch;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DefaultQiniuDelete extends AbstractQiniuSupport implements QiniuDelete {

  @Override
  public void execute() {
    Map<String, QiniuFileInfo> fileInfos = getQiniuFileInfos();
    if (fileInfos.isEmpty()) {
      log.info("No files need to be deleted.");
      return;
    }

    deleteFiles(fileInfos);
  }

  private void deleteFiles(Map<String, QiniuFileInfo> fileInfos) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    AtomicLong failCount = new AtomicLong();
    String bucket = qiniuConfig.getBucket();
    fileInfos.keySet().forEach(key -> deleteFile(bucket, key, failCount));
    log.info("Download finished. Total: {}, Failed: {}.  Time elapsed: {}.",
        fileInfos.size(), failCount.longValue(), stopwatch);
  }

  private void deleteFile(String bucket, String key, AtomicLong failCount) {
    BucketManager bucketManager = createBucketManager();
    try {
      bucketManager.delete(bucket, key);
      log.info("succeed to delete {}", key);
    } catch (QiniuException e) {
      failCount.incrementAndGet();
      log.error("failed to delete " + key, e);
    }
  }

}
