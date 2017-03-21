package com.gordianyuan.qiniu;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Application {

  private static final Logger log = LoggerFactory.getLogger(Application.class);

  private final QiniuBackup qiniuBackup;

  private final QiniuRestore qiniuRestore;

  private final QiniuCleanup qiniuCleanup;

  private final QiniuConfiguration qiniuConfig;

  @Autowired
  public Application(QiniuBackup qiniuBackup, QiniuRestore qiniuRestore, QiniuCleanup qiniuCleanup, QiniuConfiguration qiniuConfig) {
    this.qiniuBackup = qiniuBackup;
    this.qiniuRestore = qiniuRestore;
    this.qiniuCleanup = qiniuCleanup;
    this.qiniuConfig = qiniuConfig;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @PostConstruct
  public void execute() {
    executeCommand();
    System.exit(0);
  }

  private void executeCommand() {
    String command = qiniuConfig.getCommand();
    if (Strings.isNullOrEmpty(command)) {
      log.error("Please specify the command");
      return;
    }

    switch (command) {
      case "backup":
        qiniuBackup.execute();
        break;
      case "restore":
        qiniuRestore.execute();
        break;
      case "cleanup":
        qiniuCleanup.execute();
        break;
      default:
        log.error("Bad command");
    }
  }

}
