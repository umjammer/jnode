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

package org.jnode.fs.ntfs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jnode.driver.Device;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.ntfs.attribute.NTFSAttribute;
import org.jnode.fs.ntfs.attribute.NTFSResidentAttribute;
import org.jnode.fs.spi.AbstractFileSystem;

/**
 * NTFS filesystem implementation.
 *
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSFileSystem extends AbstractFileSystem<FSEntry> {

    private final NTFSVolume volume;
    private FSEntry root;

    /** */
    public NTFSFileSystem(Device device, boolean readOnly) throws FileSystemException {
        super(device, readOnly);

        try {
            // initialize the NTFS volume
            volume = new NTFSVolume(getApi());
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public FSEntry getRootEntry() throws IOException {
        if (root == null) {
            root = new NTFSEntry(this, getNTFSVolume().getRootDirectory(), -1);
        }
        return root;
    }

    /**
     * @return Returns the volume.
     */
    public NTFSVolume getNTFSVolume() {
        return this.volume;
    }

    @Override
    public String getVolumeName() throws IOException {
        NTFSEntry entry = new NTFSEntry(this, getNTFSVolume().getMFT().getRecord(MasterFileTable.SystemFiles.VOLUME),
            MasterFileTable.SystemFiles.ROOT);

        NTFSAttribute attribute = entry.getFileRecord().findAttributeByType(NTFSAttribute.Types.VOLUME_NAME);

        if (attribute instanceof NTFSResidentAttribute residentAttribute) {
            byte[] nameBuffer = new byte[residentAttribute.getAttributeLength()];

            residentAttribute.getData(residentAttribute.getAttributeOffset(), nameBuffer, 0, nameBuffer.length);

            // XXX: For Java 6, should use the version that accepts a Charset.
            return new String(nameBuffer, StandardCharsets.UTF_16LE);
        }

        return "";
    }

    /**
     * Gets the volume's ID.
     *
     * @return the volume ID.
     * @throws IOException if an error occurs.
     */
    public byte[] getVolumeId() throws IOException {
        NTFSEntry entry = (NTFSEntry) getRootEntry().getDirectory().getEntry("$Volume");
        if (entry == null) {
            return null;
        }

        NTFSAttribute attribute = entry.getFileRecord().findAttributeByType(NTFSAttribute.Types.OBJECT_ID);

        if (attribute instanceof NTFSResidentAttribute residentAttribute) {
            byte[] idBuffer = new byte[residentAttribute.getAttributeLength()];

            residentAttribute.getData(residentAttribute.getAttributeOffset(), idBuffer, 0, idBuffer.length);
            return idBuffer;
        }

        return null;
    }

    /**
     * Flush all data.
     */
    @Override
    public void flush() throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    protected FSFile createFile(FSEntry entry) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected FSDirectory createDirectory(FSEntry entry) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected NTFSEntry createRootEntry() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getFreeSpace() throws IOException {
        FileRecord bitmapRecord = getNTFSVolume().getMFT().getRecord(MasterFileTable.SystemFiles.BITMAP);

        int bitmapSize = (int) bitmapRecord.getAttributeTotalSize(NTFSAttribute.Types.DATA, null);
        byte[] buffer = new byte[bitmapSize];
        bitmapRecord.readData(0, buffer, 0, buffer.length);

        int usedBlocks = 0;

        for (byte b : buffer) {
            for (int i = 0; i < 8; i++) {
                if ((b & 0x1) != 0) {
                    usedBlocks++;
                }

                b >>= 1;
            }
        }

        long usedSpace = (long) usedBlocks * getNTFSVolume().getClusterSize();

        return getTotalSpace() - usedSpace;
    }

    @Override
    public long getTotalSpace() throws IOException {
        FileRecord bitmapRecord = getNTFSVolume().getMFT().getRecord(MasterFileTable.SystemFiles.BITMAP);
        long bitmapSize = bitmapRecord.getFileNameAttribute().getRealSize();
        return bitmapSize * 8 * getNTFSVolume().getClusterSize();
    }

    @Override
    public long getUsableSpace() {
        // TODO implement me
        return -1;
    }
}
