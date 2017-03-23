package com.gordianyuan.qiniu;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbstractQiniuSupport {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  protected QiniuConfiguration qiniuConfig;

  protected BucketManager createBucketManager() {
    Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
    return new BucketManager(auth, new Configuration());
  }

  protected Map<String, QiniuFileInfo> getQiniuFileInfos() {
    String bucket = qiniuConfig.getBucket();
    String prefix = qiniuConfig.getPrefix();
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
    String prefix = qiniuConfig.getPrefix();
    if (Strings.isNullOrEmpty(prefix)) {
      log.info("Found {} objects on bucket {}.", qiniuFiles.size(), bucket);
    } else {
      log.info("Found {} objects on bucket {} with prefix {}.", qiniuFiles.size(), bucket, prefix);
    }

    long totalFileSize = qiniuFiles.values().stream().parallel().mapToLong(QiniuFileInfo::getSize).sum();
    log.info("Total objects size is {}.", FileUtils.byteCountToDisplaySize(totalFileSize));
  }

  protected Map<String, QiniuFileInfo> loadDataFromFile() {
    log.info("Start loading data file.");
    Map<String, QiniuFileInfo> qiniuFiles = ImmutableMap.of();
    ObjectMapper objectMapper = new ObjectMapper();
    File dataFile = Paths.get(qiniuConfig.getAbsoluteDataFile()).toFile();
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
    File dataFile = Paths.get(qiniuConfig.getAbsoluteDataFile()).toFile();
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      Files.createParentDirs(dataFile);
      objectMapper.writeValue(dataFile, qiniuFiles);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    log.info("The data file has been written.");
  }

}
