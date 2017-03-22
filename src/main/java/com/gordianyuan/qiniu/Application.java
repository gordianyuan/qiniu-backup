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

  @Autowired
  private QiniuBackup qiniuBackup;

  @Autowired
  private QiniuRestore qiniuRestore;

  @Autowired
  private QiniuDelete qiniuDelete;

  @Autowired
  private QiniuConfiguration qiniuConfig;

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
      case "delete":
        qiniuDelete.execute();
        break;
      default:
        log.error("Bad command");
    }
  }

}
