/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.driver.block;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * BiosDeviceAPI. TODO
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-11-01 nsano initial version <br>
 */
public interface BiosDeviceAPI {

    /**
     * Gets the total length in bytes
     *
     * @return the total length of the device
     * @throws IOException when an error occurs
     */
    long getLength() throws IOException;

    /**
     * Read a block of data
     *
     * @param sector sector no of the device
     * @param dest buffer to read
     * @throws IOException when an error occurs
     */
    void read(long sector, ByteBuffer dest) throws IOException;

    /**
     * Write a block of data
     *
     * @param sector sector no of the device
     * @param src buffer to write
     * @throws IOException when an error occurs
     */
    void write(long sector, ByteBuffer src) throws IOException;

    /**
     * flush data in caches to the block device
     *
     * @throws IOException when an error occurs
     */
    void flush() throws IOException;
}
