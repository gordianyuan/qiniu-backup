package com.gordianyuan.qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DefaultQiniuCleanup extends AbstractQiniuSupport implements QiniuCleanup {

  @Override
  public void execute() {
    Map<String, QiniuFileInfo> fileInfos = loadDataFromFile();
    if (!fileInfos.isEmpty()) {
      deleteFiles(fileInfos);
    }
  }

  private void deleteFiles(Map<String, QiniuFileInfo> fileInfos) {
    AtomicLong failCount = new AtomicLong();
    String bucket = qiniuConfig.getBucket();
    fileInfos.keySet().forEach(key -> deleteFile(bucket, key, failCount));
    log.info("Download finished. Total: {}, Failed: {}", fileInfos.size(), failCount.longValue());
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
