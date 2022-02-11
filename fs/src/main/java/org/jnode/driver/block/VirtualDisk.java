/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.driver.block;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * VirtualDisk.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/09 umjammer initial version <br>
 */
public interface VirtualDisk {

    int getSectorSize();

    int getSectors();

    int getHeads();

    long getLength();

    void setLength(long length);

    void read(long offset, ByteBuffer buffer) throws IOException;

    void write(long offset, ByteBuffer buffer) throws IOException;

    void close() throws IOException;
}

/* */
