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

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;

/**
 * @author epr
 */
public class MappedBlockDeviceSupport extends Device implements BlockDeviceAPI {

    private final Device parent;
    private final BlockDeviceAPI parentApi;
    private final long offset;
    private final long length;

    /**
     * Create a new MappedBlockDevice
     *
     * @param parent the device
     * @param offset the offset
     * @param length the length
     * @throws IOException when an error occurs
     */
    public MappedBlockDeviceSupport(Device parent, long offset, long length) throws IOException {
        super("mapped-" + parent.getId());
        this.parent = parent;
        try {
            this.parentApi = parent.getAPI(BlockDeviceAPI.class);
        } catch (ApiNotFoundException e) {
            throw new IOException("BlockDeviceAPI not found on device", e);
        }
        this.offset = offset;
        this.length = length;
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset < 0");
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("length < 0");
        }
        if (offset + length > parentApi.getLength()) {
            throw new IndexOutOfBoundsException("offset(" + offset + ") + length(" + length +
                    ") > parent.length(" + parentApi.getLength() + ")");
        }
        registerAPI(BlockDeviceAPI.class, this);
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public void read(long devOffset, ByteBuffer dest) throws IOException {
        checkBounds(devOffset, dest);
//        parentApi.read(offset + devOffset, dest, destOffset, length);
        parentApi.read(offset + devOffset, dest);
    }

    @Override
    public void write(long devOffset, ByteBuffer src) throws IOException {
        checkBounds(devOffset, src);
//        parentApi.write(offset + devOffset, src, srcOffset, length);
        parentApi.write(offset + devOffset, src);
    }

    @Override
    public void flush() throws IOException {
        parentApi.flush();
    }

    /**
     * @return long
     */
    public long getOffset() {
        return offset;
    }

    /**
     * @return Device
     */
    public Device getParent() {
        return parent;
    }

    protected void checkBounds(long devOffset, ByteBuffer buf)
        throws IOException {

        int remaining = buf.remaining();
        if (devOffset < 0) {
            throw new IOException("Out of mapping: offset < 0");
        }
        if (remaining < 0) {
            throw new IOException("Out of mapping: remaining < 0");
        }
        if (devOffset + remaining > this.length) {
            throw new IOException("Out of mapping: devOffset(" + devOffset + ") + remaining(" + remaining +
                ") > this.length(" + this.length + ")");
        }
    }
}
