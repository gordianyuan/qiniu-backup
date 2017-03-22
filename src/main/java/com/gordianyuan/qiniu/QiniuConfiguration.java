package com.gordianyuan.qiniu;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties
public class QiniuConfiguration {

  private String domain;

  private String bucket;

  private String accessKey;

  private String secretKey;

  private String baseDir = "backup";

  private String command;

  private String prefix;

  private String originalPrefix;

  private static final String FILE_DIR = "files";

  private static final String DATA_FILE = "data.json";

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
    Path baseDirPath = Paths.get(baseDir);
    Path fileDirPath = baseDirPath.resolve(Paths.get(FILE_DIR));
    return baseDirPath.toAbsolutePath().toString();
  }

  public String getDataFile() {
    Path baseDirPath = Paths.get(baseDir);
    Path dataFilePath = baseDirPath.resolve(Paths.get(DATA_FILE));
    return dataFilePath.toAbsolutePath().toString();
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
