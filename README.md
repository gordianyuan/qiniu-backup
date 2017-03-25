# Qiniu Backup 七牛备份恢复工具

## 功能

- 批量下载七牛文件到本地
- 批量上传本地文件到七牛
- 批量删除七牛文件

## 如何使用

**需要 JDK 8 或以上版本**

1. 下载 [程序](http://on5rv61k7.bkt.clouddn.com/qiniu-backup/qiniu-backup-v0.1.0.zip) 并解压。
2. 编辑 `application.properties`，修改配置项。
3. 打开控制台，执行命令 `java -jar qiniu-backup.jar --command=<command> [--prefix=<prefix>]`

## 配置

| 配置项      | 说明                                                  |
|-------------|-------------------------------------------------------|
| access-key  | 七牛账户 Access Key（必填）                           |
| secret-key  | 七牛账户 Access Key（必填）                           |
| bucket      | 七牛空间名称（必填）                                  |
| domain      | 七牛空间域名（必填）                                  |
| prefix      | 七牛文件路径前缀（默认为空）                          |
| base-dir    | 本地工作目录，存放下载数据和日志文件（默认为当前目录）|

### 配置示例

```properties
access-key=E7Ohx4r1FnES7Ld9l9scbrR3Q3JTX9Wdqt8YTxKc
secret-key=NDmM2t3THEAQSsNy5zHSNMZ0yW1J20iZwpM5LZ9l
domain=3d4hpv.com1.z0.glb.clouddn.com
bucket=blog
# base-dir=/tmp/qiniu
```

## 命令

### backup（批量下载七牛文件到本地）

`java -jar qiniu-backup.jar --command=backup [--prefix=<prefix>]`

- `prefix`：七牛文件路径前缀，默认为空

### restore（批量上传本地文件到七牛）

`java -jar qiniu-backup.jar --command=restore [--prefix=<prefix>] [--original-prefix=<original-prefix>] [--file-dir=<file-dir>]`

- `prefix`：七牛文件路径前缀，默认为空
- `original-prefix`: 七牛文件原前缀，默认为空
- `file-dir`: 本地文件目录，默认为 `<base-dir> + /files`

### delete（批量删除七牛文件）

`java -jar qiniu-backup.jar --command=delete [--prefix=<prefix>]`

- `prefix`：七牛文件路径前缀，默认为空

