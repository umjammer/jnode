/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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

package org.jnode.test.partitions.ibm;

import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.util.LittleEndian;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IBMPartitionTableEntryTest {

    @Test
    public void testhasChildPartitionTable() {
        byte[] bootSector = getBootSector();
        LittleEndian.setInt8(bootSector, 450, 0x85);
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null, bootSector, 0);
        assertTrue(pte.hasChildPartitionTable());
    }

    @Test
    public void testhasNoChildPartitionTable() {
        byte[] bootSector = getBootSector();
        LittleEndian.setInt8(bootSector, 450, 0x84);
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null, bootSector, 0);
        assertFalse(pte.hasChildPartitionTable());
    }

    @Test
    public void testIsValid() {
        byte[] bootSector = getBootSector();
        LittleEndian.setInt32(bootSector, 446, 0x80); // bootable
        LittleEndian.setInt32(bootSector, 450, 0x85); // valid system id
        LittleEndian.setInt32(bootSector, 458, 1); // has sectors
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null, bootSector, 0);
        assertTrue(pte.isValid());
    }

    @Test
    public void testIsNotValidEmptyBootSector() {
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null, getBootSector(), 0);
        assertFalse(pte.isValid());
    }

    @Test
    public void testIsEmpty() {
        IBMPartitionTableEntry pte = new IBMPartitionTableEntry(null, getBootSector(), 0);
        assertTrue(pte.isEmpty());
    }

    private byte[] getBootSector() {
        byte[] bs = new byte[500];
        return bs;
    }

}
