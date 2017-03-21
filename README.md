# Qiniu Backup

Qiniu backup and restore tools.

- Backup means batch download files to local storage.
- Restore means batch upload files to Qiniu bucket.

## How to use it

**Requires JDK 8 or above.**

1. Download [latest](http://on5rv61k7.bkt.clouddn.com/qiniu-backup/qiniu-backup-v0.0.1.zip) version
2. Edit `application.properties`
3. Run `java -jar <qiniu-backup-version>.jar --command=<command>`

## Commands

- `backup`: Batch download files to local storage
- `restore`: Batch upload files to Qiniu bucket
- `cleanup`: Delete backed up files on Qiniu bucket

## Configuraton

- `access-key`: Qiniu access key
- `secret-key`: Qiniu secret key
- `domain`: Qiniu domain
- `bucket`: Qiniu bucket name
- `backup-prefix`: Backup files prefix
- `restore-prefix`: Restore files prefix
- `base-dir`: Local storeage directory, used to store downloaded files and data logs.
