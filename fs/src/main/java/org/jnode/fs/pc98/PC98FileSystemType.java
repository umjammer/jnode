/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.fs.pc98;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.jfat.BootSector;
import org.jnode.fs.jfat.FatFileSystem;
import org.jnode.partitions.PartitionTableEntry;

import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * PC98FileSystemType.
 * <p>
 * system property<br/>
 * "org.jnode.file.encoding" ... filename encoding for {@link Charset#forName(String)}, default is "MS932"
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/08 umjammer initial version <br>
 */
public class PC98FileSystemType implements BlockDeviceFileSystemType<FatFileSystem> {

    @Override
    public String getName() {
        return "PC98";
    }

    @Override
    public String getScheme() {
        return "pc98";
    }

    // TODO
    @Override
    public boolean supports(PartitionTableEntry pte, byte[] firstSectors, FSBlockDeviceAPI devApi) {
Debug.println("\n" + StringUtil.getDump(firstSectors));

        if (firstSectors[0x3] != 'N' ||
            firstSectors[0x4] != 'E' ||
            firstSectors[0x5] != 'C') {
            // Missing magic number
Debug.printf("Missing magic number 'NEC': %c%c%c%n", firstSectors[0x3] & 0xff, firstSectors[0x4] & 0xff, firstSectors[0x5] & 0xff);
            return false;
        }

        if (!new String(firstSectors, 0x36, 3, StandardCharsets.US_ASCII).equals("FAT")) {
Debug.println("strings FAT is not found");
            return false;
        }

        return true;
    }

    @Override
    public FatFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        BootSector bs = new PC98BootSector();
        return new FatFileSystem(device, bs, System.getProperty("org.jnode.file.encoding", "MS932"), readOnly);
    }
}
