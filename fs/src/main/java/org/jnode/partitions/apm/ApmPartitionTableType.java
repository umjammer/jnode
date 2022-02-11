/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.partitions.apm;

import java.nio.charset.Charset;

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableException;
import org.jnode.partitions.PartitionTableType;

/**
 * Apple Partition Map (APM) partition table type.
 *
 * @author Luke Quinane
 */
public class ApmPartitionTableType implements PartitionTableType {

    @Override
    public PartitionTable<?> create(byte[] firstSector, Device device) throws PartitionTableException {
        return new ApmPartitionTable(firstSector, device);
    }

    @Override
    public String getName() {
        return "APM";
    }

    /** */
    public String getScheme() {
        return "apm";
    }

    /**
     * Checks if the given boot sector contain a APM partition table.
     *
     * @param first16KiB the first 16,384 bytes of the disk.
     */
    @Override
    public boolean supports(byte[] first16KiB, BlockDeviceAPI devApi) {
        if (first16KiB.length < 0x250) {
            // Not enough data for detection
            return false;
        }

        if ((first16KiB[0x200] & 0xFF) != 0x50) {
            return false;
        }
        if ((first16KiB[0x201] & 0xFF) != 0x4d) {
            return false;
        }

        byte[] typeBytes = new byte[31];
        System.arraycopy(first16KiB, 0x230, typeBytes, 0, typeBytes.length);
        String type = new String(typeBytes, Charset.forName("ASCII")).replace("\u0000", "");

        if (!"Apple_partition_map".equalsIgnoreCase(type)) {
            return false;
        }

        return true;
    }
}
