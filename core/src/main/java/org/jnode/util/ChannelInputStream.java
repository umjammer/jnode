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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * This is a stream wrapper for a ReadableByteChannel.
 */
public class ChannelInputStream extends InputStream {

    private final ReadableByteChannel channel;
    private final ByteBuffer buffer;

    public ChannelInputStream(ReadableByteChannel c) {
        channel = c;
        buffer = ByteBuffer.allocateDirect(1);
    }

    @Override
    public int read() throws IOException {
        buffer.clear();
        if (channel.read(buffer) < 1) return -1;
        else return buffer.get() & 0xff;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return channel.read(ByteBuffer.wrap(b));
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return channel.read(ByteBuffer.wrap(b, off, len));
    }

    @Override
    public long skip(long n) throws IOException {
        ByteBuffer b = ByteBuffer.allocateDirect(2048);
        long rem = n;

        while (rem > 2048) {
            int num = channel.read(b);
            if (num < 1) return n - rem;
            rem -= num;
            b.rewind();
        }

        int x = (int) rem;
        b.limit(x);
        return n - rem + channel.read(b);
    }

    @Override
    public void mark(int readLimit) {
        // Do nothing
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
}
