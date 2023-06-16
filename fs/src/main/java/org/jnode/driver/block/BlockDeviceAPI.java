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
import org.jnode.driver.DeviceAPI;

/**
 * The API for block devices.
 * <p>
 * this api is able to access only solid disk images.
 * @author epr
 */
public interface BlockDeviceAPI extends DeviceAPI {

    /**
     * Gets the total length in bytes
     *
     * @return the total length of the device
     * @throws IOException when an error occurs
     */
    long getLength()
        throws IOException;

    /**
     * Read a block of data
     *
     * @param devOffset offset position of the device
     * @param dest buffer to read
     * @throws IOException when an error occurs
     */
    void read(long devOffset, ByteBuffer dest) throws IOException;

    /**
     * Write a block of data
     *
     * @param devOffset offset position of the device
     * @param src buffer to write
     * @throws IOException when an error occurs
     */
    void write(long devOffset, ByteBuffer src) throws IOException;

    /**
     * flush data in caches to the block device
     *
     * @throws IOException when an error occurs
     */
    void flush() throws IOException;
}
