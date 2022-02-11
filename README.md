# vavi-nio-file-jnode

A Java NIO FileSystem implementation based on [jnode](https://github.com/jnode/jnode).

## Status

| fs       | list | upload | download | copy | move | rm | mkdir | cache | watch | comment |
|----------|------|--------|----------|------|------|----|-------|-------|-------|---------|
| nfs2     |            |     |        |    |   |  |    |    |       | 
| exfat                 | ✅  |     |        |    |   |  |    |    |       | 
| iso9660  |            |     |        |    |   |  |    |    |       | 
| jfat     |            |     |        |    |   |  |    |    |       | 
| ext2     |            |     |        |    |   |  |    |    |       | 
| hfs      |            |     |        |    |   |  |    |    |       | 
| ftpfs    |            |     |        |    |   |  |    |    |       | 
| smbfs    |            |     |        |    |   |  |    |    |       | 
| ntfs     |            |     |        |    |   |  |    |    |       | 
| fat      |            |     |        |    |   |  |    |    |       | 
| hfsplus  |            |     |        |    |   |  |    |    |       | 
| apm      |            |     |        |    |   |  |    |    |       | partition
| gpt      |            |     |        |    |   |  |    |    |       | partition
| ibm (dmg:jfat(fat16)) | ✅ |     |        |    |   |  |    |    |       | partition
| pc98 (jfat(fat16))    | ✅ |     |        |    |   |  |    |    |       | partition
| raw (exfat)           | ✅ |     |        |    |   |  |    |    |       | virtual partition
| virtualDisk (nhd:pc98:fat16) | ✅ |     |        |    |   |  |    |    |       | [virtual disk](vavi-nio-file-emu), partition
| fuse (virtualDisk(nhd):pc98:fat16) | ✅ |     |        |    |   |  |    |    |       | [fuse](vavi-net-fuse), virtualDisk, partition
