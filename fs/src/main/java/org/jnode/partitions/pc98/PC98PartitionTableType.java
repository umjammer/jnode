/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.partitions.pc98;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableException;
import org.jnode.partitions.PartitionTableType;
import org.jnode.util.LittleEndian;

import vavi.util.Debug;
import vavi.util.serdes.Serdes;

import vavix.io.partition.PC98PartitionEntry;


/**
 * PC98PartitionTableType.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/06 umjammer initial version <br>
 */
public class PC98PartitionTableType implements PartitionTableType {

    @Override
    public PartitionTable<?> create(byte[] firstSector, Device device) throws PartitionTableException {
        return new PC98PartitionTable(firstSector, device);
    }

    @Override
    public String getName() {
        return "PC98";
    }

    /** */
    public String getScheme() {
        return "pc98";
    }

    private static final String[] iplSignatures = {
        "IPL1", "Linux 98", "GRUB/98 "
    };

    // works don't touch
    @Override
    public boolean supports(byte[] bootSector, BlockDeviceAPI devApi) {
        if (bootSector.length < 0x400) {
Debug.printf("Not enough data for detection: %04x/%04x%n", bootSector.length, 0x400);
            return false;
        }

        if (LittleEndian.getUInt16(bootSector, 510) != 0xaa55) {
Debug.printf("No aa55 magic: %04x%n", LittleEndian.getUInt16(bootSector, 510));
            return false;
        }

        if (Arrays.stream(iplSignatures).noneMatch(s ->
            new String(bootSector, 4, s.length(), StandardCharsets.US_ASCII).equals(s)
        )) {
Debug.println("no maching signature is found: " + new String(bootSector, 4, 4, StandardCharsets.US_ASCII));
            return false;
        }

        if (new String(bootSector, 0x36, 3, StandardCharsets.US_ASCII).equals("FAT")) {
Debug.println("strings FAT is found, this partition might be for AT");
            return false;
        }

        int count = 0;
        ByteArrayInputStream baos = new ByteArrayInputStream(bootSector, 512, 512);
        for (int i = 0; i < 16; i++) {
            PC98PartitionEntry pe = new PC98PartitionEntry();
            try {
                Serdes.Util.deserialize(baos, pe);
                if (!pe.isValid()) {
                    continue;
                }
            } catch (IOException e) {
                continue;
            }
Debug.println("[" + count + "]: " + pe);
            count++;
        }

        return count > 0;
    }
}
