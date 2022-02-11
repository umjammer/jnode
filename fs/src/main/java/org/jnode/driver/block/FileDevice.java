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

package org.jnode.driver.block;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.util.ByteBufferUtils;

import vavi.util.Debug;

/**
 * This class is a device wrapping a simple file
 *
 * @author epr
 */
public class FileDevice extends Device implements FSBlockDeviceAPI {

    private static final Logger logger = LogManager.getLogger(FileDevice.class);

    private RandomAccessFile raf;

    /** virtual offset */
    private long offset = 0;

    /**
     * for partition entry
     * works! don't touch
     */
    public void addOffset(long offset) {
        this.offset += offset;
Debug.printf("offset: %08x + %08x -> %08x", (this.offset - offset), offset, this.offset);
    }

    /**
     * Create a new FileDevice
     *
     * @param file
     * @param mode see {@link RandomAccessFile}
     * @throws FileNotFoundException
     */
    public FileDevice(File file, String mode) throws IOException {
        super("file" + System.currentTimeMillis());
        raf = new RandomAccessFile(file, mode);
        //registerAPI(BlockDeviceAPI.class, this);
        registerAPI(FSBlockDeviceAPI.class, this);
    }

    /**
     * Create a new FileDevice with virtual offset
     *
     * @param file
     * @param mode see {@link RandomAccessFile}
     * @param offset virtual offset
     * @throws FileNotFoundException
     */
    public FileDevice(File file, String mode, int offset) throws IOException {
        this(file, mode);
        this.offset = offset;
    }

    @Override
    public long getLength() throws IOException {
        return raf.length() - offset;
    }

    @Override
    public void read(long devOffset, ByteBuffer destBuf) throws IOException {
        raf.seek(devOffset + offset);
logger.debug(String.format("offset: %08x (%08x)", devOffset + offset, offset));

        //TODO optimize it also to use ByteBuffer at lower level
        ByteBufferUtils.ByteArray destBA = ByteBufferUtils.toByteArray(destBuf);
        byte[] dest = destBA.toArray();
        raf.read(dest, 0, dest.length);
        destBA.refreshByteBuffer();
    }

    @Override
    public void write(long devOffset, ByteBuffer srcBuf) throws IOException {
        //log.debug("fd.write devOffset=" + devOffset + ", length=" + length);
        raf.seek(devOffset + offset);

        //TODO optimize it also to use ByteBuffer at lower level
        byte[] src = ByteBufferUtils.toArray(srcBuf);
        raf.write(src, 0, src.length);
    }

    @Override
    public void flush() {
        // Nothing to flush
    }

    /**
     * change the length of the underlaying file
     *
     * @param length real file length
     */
    public void setLength(long length) throws IOException {
        if (offset > 0) {
            logger.warn("this device has virtual offset (" + offset + "), so length you sepcified might be different as your expectation.");
        }
        raf.setLength(length);
        if (offset > 0) {
            logger.warn("now real file length is " + length + ", virtual offset is " + offset);
        }
    }

    /**
     * close the underlaying file
     */
    public void close() throws IOException {
        raf.close();
    }

    @Override
    public PartitionTableEntry getPartitionTableEntry() {
        return null;
    }

    @Override
    public int getSectorSize() throws IOException {
        return 512;
    }
}
