[![Release](https://jitpack.io/v/umjammer/vavi-nio-file-jnode.svg)](https://jitpack.io/#umjammer/vavi-nio-file-jnode)
[![Java CI](https://github.com/umjammer/vavi-nio-file-jnode/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-nio-file-jnode/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-nio-file-jnode/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-nio-file-jnode/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-8-b07219)
[![Parent](https://img.shields.io/badge/Parent-vavi--apps--fuse-pink)](https://github.com/umjammer/vavi-apps-fuse)

# vavi-nio-file-jnode

A Java NIO FileSystem implementation based on [jnode](https://github.com/jnode/jnode).

## Status

| fs                           | list | upload | download | copy | move | rm | mkdir | cache | watch | comment                                               |
|------------------------------|------|--------|----------|------|------|----|-------|-------|-------|-------------------------------------------------------|
| nfs2                         |      |        |        |    |   |  |    |    |       |                                                       |
| exfat                        | ✅    |        |        |    |   |  |    |    |       |                                                       |
| iso9660                      |      |        |        |    |   |  |    |    |       |                                                       |
| jfat                         | ✅    |        |        |    |   |  |    |    |       |                                                       |
| ext2                         |      |        |        |    |   |  |    |    |       |                                                       |
| hfs                          |      |        |        |    |   |  |    |    |       |                                                       |
| ftpfs                        |      |        |        |    |   |  |    |    |       | [edtFTPj](https://enterprisedt.com/products/edtftpj/) |
| smbfs                        |      |        |        |    |   |  |    |    |       | [jcifs-ng](https://github.com/AgNO3/jcifs-ng)         |
| ntfs                         |      |        |        |    |   |  |    |    |       |                                                       |
| fat                          |      |        |        |    |   |  |    |    |       |                                                       |
| hfsplus                      |      |        |        |    |   |  |    |    |       |                                                       |
||||||||
| apm                          |      |        |        |    |   |  |    |    |       | partition                                             |
| gpt                          |      |        |        |    |   |  |    |    |       | partition                                             |
| ibm (dmg:jfat(fat16))        | ✅    |        |        |    |   |  |    |    |       | partition                                             |
| pc98 (jfat(fat16))           | ✅    |        |        |    |   |  |    |    |       | partition                                             |
| raw (exfat)                  | ✅    |        |        |    |   |  |    |    |       | virtual partition                                     |
| vdisk (nhd:pc98:fat16)       | ✅    |        |        |    |   |  |    |    |       | [virtual disk](vavi-nio-file-emu), partition          |
| fuse (vdisk(nhd):pc98:fat16) | ✅    |        |        |    |   |  |    |    |       | [fuse](vavi-net-fuse), virtualDisk, partition         |


## for emulator user

it's possible to mount old school japanese computer pc-9801's virtual disk by fuse.

we can see nostalgic files `autoexec.bat`, `command.com`, `mifes...` etc.

time stamps are so old lol.

![](https://lh3.googleusercontent.com/pw/AM-JKLVzJc46TaLOLtacSQdNJF-11XE6gw1eBN-57aIazw22VK1HHsPIoXNO3cVjHWnnEq36bjJxFBiRP3ipe57fXTfpITi8-FybMbTvpHXR-X2ZzQ2MI-HirwnI1PCyhpL6pUb8SDbCRBOyzr_sHRUKMxZB=w1024-h981-no?authuser=0)