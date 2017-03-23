package com.gordianyuan.qiniu;

import com.google.common.base.Strings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
@ConfigurationProperties
public class QiniuConfiguration {

  private static final String DEFAULT_BASE_DIR = "backup";

  private static final String DEFAULT_FILE_DIR = "files";

  private static final String DEFAULT_DATA_FILE = "data.json";

  private String domain;

  private String bucket;

  private String accessKey;

  private String secretKey;

  private String baseDir = DEFAULT_BASE_DIR;

  private String fileDir = DEFAULT_FILE_DIR;

  private String dataFile = DEFAULT_DATA_FILE;

  private String command;

  private String prefix;

  private String originalPrefix;

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getBaseDir() {
    return baseDir;
  }

  public void setBaseDir(String baseDir) {
    this.baseDir = baseDir;
  }

  public String getFileDir() {
    return fileDir;
  }

  public void setFileDir(String fileDir) {
    this.fileDir = Strings.isNullOrEmpty(fileDir) ? DEFAULT_FILE_DIR : fileDir;
  }

  public String getAbsoluteFileDir() {
    if (fileDir.startsWith("/")) {
      return Paths.get(fileDir).toAbsolutePath().toString();
    } else {
      return Paths.get(baseDir, fileDir).toAbsolutePath().toString();
    }
  }

  public String getAbsoluteDataFile() {
    return Paths.get(baseDir, dataFile).toAbsolutePath().toString();
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getOriginalPrefix() {
    return originalPrefix;
  }

  public void setOriginalPrefix(String originalPrefix) {
    this.originalPrefix = originalPrefix;
  }

}
