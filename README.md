[![Release](https://jitpack.io/v/umjammer/vavi-nio-file-jnode.svg)](https://jitpack.io/#umjammer/vavi-nio-file-jnode)
[![Java CI](https://github.com/umjammer/vavi-nio-file-jnode/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-nio-file-jnode/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-nio-file-jnode/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-nio-file-jnode/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)
[![Parent](https://img.shields.io/badge/Parent-vavi--apps--fuse-pink)](https://github.com/umjammer/vavi-apps-fuse)

# vavi-nio-file-jnode

A Java NIO FileSystem implementation based on [jnode](https://github.com/jnode/jnode).

all formats are mounted by fuse also!

### Status

| fs                           | list | upload | download | copy | move | rm  | mkdir | cache | watch | comment                                               |
|------------------------------|------|--------|----------|------|------|-----|-------|-------|-------|-------------------------------------------------------|
| nfs2                         |      |        |          |      |      |     |       |       |       |                                                       |
| exfat                        | ✅    |        |          |      |      |     |       |       |       |                                                       |
| iso9660                      |      |        |          |      |      |     |       |       |       |                                                       |
| jfat                         | ✅    |        |          |      |      |     |       |       |       |                                                       |
| ext2                         |      |        |          |      |      |     |       |       |       |                                                       |
| hfs                          |      |        |          |      |      |     |       |       |       |                                                       |
| ftpfs                        |      |        |          |      |      |     |       |       |       | [edtFTPj](https://enterprisedt.com/products/edtftpj/) |
| smbfs                        |      |        |          |      |      |     |       |       |       | [jcifs-ng](https://github.com/AgNO3/jcifs-ng)         |
| ntfs                         |      |        |          |      |      |     |       |       |       |                                                       |
| fat                          |      |        |          |      |      |     |       |       |       |                                                       |
| hfsplus                      |      |        |          |      |      |     |       |       |       |                                                       |
||||||||
| apm                          |      |        |          |      |      |     |       |       |       | partition                                             |
| gpt                          |      |        |          |      |      |     |       |       |       | partition                                             |
| ibm (dmg:jfat(fat16))        | ✅    |        |          |      |      |     |       |       |       | partition                                             |
| pc98 (jfat(fat16))           | ✅    |        |          |      |      |     |       |       |       | partition                                             |
| raw (exfat)                  | ✅    |        |          |      |      |     |       |       |       | virtual partition                                     |
| vdisk (nhd:pc98:fat16)       | ✅    |        |          |      |      |     |       |       |       | [virtual disk](vavi-nio-file-emu), partition          |
| fuse (vdisk(nhd):pc98:fat16) | ✅    |        |          |      |      |     |       |       |       | [fuse](vavi-net-fuse), virtualDisk, partition         |
| vdisk (d88:pc98:n88)         | 🚧   |        |          |      | | | | |       | not solid image is not supported by DeviceAPI         |
| vdisk (fdi:pc98:fat12)       | ✅    |        |          |      | | | | |       | [virtual disk](vavi-nio-file-emu), partition          |

## Install

 * [maven](https://jitpack.io/#umjammer/vavi-nio-file-jnode)

## Usage

### JSR-203 & fuse

```java
    URI uri = URI.create("jnode:file:/foo/bar.nhd");
    fs = FileSystems.newFileSystem(uri, Collections.emptyList());
    Fuse fuse = Fuse.getFuse().mount(fs, MOUNT_POINT, Collections.emptyList());
```

### for emulator user

it's possible to mount old school japanese computer pc-9801's virtual disk by fuse.

we can see nostalgic files `autoexec.bat`, `command.com`, `mifes...` etc.

time stamps are so old lol.

<img alt="mount nhd" src="https://lh3.googleusercontent.com/pw/AM-JKLVzJc46TaLOLtacSQdNJF-11XE6gw1eBN-57aIazw22VK1HHsPIoXNO3cVjHWnnEq36bjJxFBiRP3ipe57fXTfpITi8-FybMbTvpHXR-X2ZzQ2MI-HirwnI1PCyhpL6pUb8SDbCRBOyzr_sHRUKMxZB=w1024-h981-no?authuser=0" width=480 />

## References

 * [vavi-nio-file-emu](https://jitpack.io/#umjammer/vavi-nio-file-emu) ... PC-98 FAT

## TODO

 * `BlockDeviceAPI` can only support \[header\] + solid image
   * api separation from device is in high esteem
   * however we need accessing disk data by logical sector No. but offset like `BiosDeviceAPI` for emu disks like d88