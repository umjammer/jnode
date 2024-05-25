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

package org.jnode.fs.fat;

public enum FatType {
    FAT12(0xFFF, 1.5f), FAT16(0xFFFF, 2.0f), FAT32(0xffff_ffffL, 4.0f);

    private final long minReservedEntry;
    private final long maxReservedEntry;
    private final long eofCluster;
    private final long eofMarker;
    private final float entrySize;

    FatType(long bitMask, float entrySize) {
        this.minReservedEntry = (0xffff_fff0L & bitMask);
        this.maxReservedEntry = (0xffff_fff6L & bitMask);
        this.eofCluster = (0xffff_fff8L & bitMask);
        this.eofMarker = (0xffff_ffffL & bitMask);
        this.entrySize = entrySize;
    }

    public final boolean isReservedCluster(long entry) {
        return ((entry >= minReservedEntry) && (entry <= maxReservedEntry));
    }

    public final boolean isEofCluster(long entry) {
        return (entry >= eofCluster);
    }

    public final long getEofMarker() {
        return eofMarker;
    }

    public final float getEntrySize() {
        return entrySize;
    }
}
