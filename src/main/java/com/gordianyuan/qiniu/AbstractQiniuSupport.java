package com.gordianyuan.qiniu;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class AbstractQiniuSupport {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  protected QiniuConfiguration qiniuConfig;

  protected BucketManager createBucketManager() {
    Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
    return new BucketManager(auth, new Configuration());
  }

  protected Map<String, QiniuFileInfo> loadDataFromFile() {
    log.info("Start loading data file.");
    Map<String, QiniuFileInfo> qiniuFiles = ImmutableMap.of();
    ObjectMapper objectMapper = new ObjectMapper();
    File dataFile = Paths.get(qiniuConfig.getDataFile()).toFile();
    try {
      qiniuFiles = objectMapper.readValue(dataFile, new TypeReference<Map<String, QiniuFileInfo>>() {
      });
      log.info("The data file is loaded.");
    } catch (IOException e) {
      log.error("Failed to load data file.", e);
    }
    return qiniuFiles;
  }

  protected void saveDataToFile(Map<String, QiniuFileInfo> qiniuFiles) {
    log.info("Start write data file.");
    File dataFile = Paths.get(qiniuConfig.getDataFile()).toFile();
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      objectMapper.writeValue(dataFile, qiniuFiles);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    log.info("The data file has been written.");
  }

}
