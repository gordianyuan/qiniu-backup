package com.gordianyuan.qiniu;

public class QiniuFileInfo {

  private String key;

  private String hash;

  private Long size;

  private DownloadStatus downloadStatus;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public DownloadStatus getDownloadStatus() {
    return downloadStatus;
  }

  public void setDownloadStatus(DownloadStatus downloadStatus) {
    this.downloadStatus = downloadStatus;
  }

  public enum DownloadStatus {
    PENDING, SUCCEED, FAILED
  }

}
