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

package org.jnode.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * This is a stream wrapper for a WritableByteChannel. This stream
 * buffers the data internally. The buffer contents are written to
 * the channel with the flush() method.
 * <p/>
 * Currently throws an IOException if not all bytes can be written.
 */
public class ChannelOutputStream extends OutputStream {

    private final WritableByteChannel channel;
    private final ByteBuffer buffer;

    public ChannelOutputStream(WritableByteChannel c, int bufSize) {
        channel = c;
        buffer = ByteBuffer.allocateDirect(bufSize);
    }

    @Override
    public void write(int b) throws IOException {
        buffer.put((byte) b);
        if (!buffer.hasRemaining())
            flush();
    }

    @Override
    public void write(byte[] b) throws IOException, NullPointerException {
        flush();
        out(ByteBuffer.wrap(b));
    }

    @Override
    public void write(byte[] b, int off, int len)
        throws IOException, NullPointerException, IndexOutOfBoundsException {
        flush();
        out(ByteBuffer.wrap(b, off, len));
    }

    @Override
    public void flush() throws IOException {
        buffer.flip();
        out(buffer);
        buffer.clear();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    private void out(ByteBuffer b) throws IOException {
        int n = b.remaining();
        if (channel.write(b) != n)
            throw new IOException("could not write all bytes");
    }
}
