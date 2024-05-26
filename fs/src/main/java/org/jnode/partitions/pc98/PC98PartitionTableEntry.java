/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.partitions.pc98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.jnode.driver.Device;
import org.jnode.driver.block.VirtualDiskDevice;
import org.jnode.partitions.PartitionTableEntry;
import vavix.io.partition.PC98PartitionEntry;

import static java.lang.System.getLogger;


/**
 * PC98PartitionTableEntry.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/06 umjammer initial version <br>
 */
public class PC98PartitionTableEntry implements PartitionTableEntry {

    private static final Logger logger = getLogger(PC98PartitionTableEntry.class.getName());

    private final PC98PartitionEntry pe;

    // disk geometries
    private int heads = 0, secs = 0;

    /**
     * Creates a new entry.
     */
    public PC98PartitionTableEntry(PC98PartitionEntry pe, Device device) {
        this.pe = pe;
        if (device instanceof VirtualDiskDevice) {
            heads = ((VirtualDiskDevice) device).getHeads();
            secs = ((VirtualDiskDevice) device).getSectors();
        }
logger.log(Level.DEBUG, String.format("heads: %d, secs: %d, device: %s", heads, secs, device.getClass().getName()));
    }

    // @see "https://github.com/aaru-dps/Aaru.Helpers/blob/4640bb88d3eb907d0f0617d5ee5159fbc13c5653/CHS.cs"
    private static int toLBA(int cyl, int head, int sector, int maxHead, int maxSector) {
        return maxHead == 0 || maxSector == 0 ? (((cyl * 16)      + head) * 63)        + sector - 1
                                              : (((cyl * maxHead) + head) * maxSector) + sector - 1;
    }

    @Override
    public boolean isValid() {
        return pe.isValid();
    }

    @Override
    public PC98PartitionTable getChildPartitionTable() {
        throw new UnsupportedOperationException("No child partitions.");
    }

    @Override
    public boolean hasChildPartitionTable() {
        return false;
    }

    @Override
    public long getStartOffset(int sectorSize) {
logger.log(Level.DEBUG, String.format("s.c: %d, s.h: %d, s.s: %d, heads: %d, secs: %d, bps: %d", pe.startCylinder, pe.startHeader, pe.startSector, heads, secs, sectorSize));
        if (heads != 0 && secs != 0) {
            return (long) toLBA(pe.startCylinder, pe.startHeader, pe.startSector + 1, heads, secs) * sectorSize;
        } else {
            // when device is not VirtualDiskDevice
logger.log(Level.WARNING, "@@@@@@@@@@@@@@@@@@@@@@@@ magic number is used @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            return 0x20000;
        }
    }

    @Override
    public long getEndOffset(int sectorSize) {
logger.log(Level.DEBUG, String.format("e.c: %d, e.h: %d, e.s: %d, heads: %d, secs: %d, bps: %d", pe.endCylinder, pe.endHeader, pe.endSector, heads, secs, sectorSize));
        return (long) toLBA(pe.endCylinder, pe.endHeader, pe.endSector + 1, heads, secs) * sectorSize;
    }
}
