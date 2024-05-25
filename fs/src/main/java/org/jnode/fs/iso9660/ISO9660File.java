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

package org.jnode.fs.iso9660;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.util.ByteBufferUtils;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ISO9660File implements FSFile {

    private final ISO9660Entry entry;

    /**
     * @param entry the entry
     */
    public ISO9660File(ISO9660Entry entry) {
        this.entry = entry;
    }

    @Override
    public long getLength() {
        return entry.getCDFSentry().getDataLength();
    }

    @Override
    public void setLength(long length) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void read(long fileOffset, ByteBuffer destBuf) throws IOException {
        // TODO optimize it also to use ByteBuffer at lower level
        final ByteBufferUtils.ByteArray destBA = ByteBufferUtils.toByteArray(destBuf);
        final byte[] dest = destBA.toArray();
        this.entry.getCDFSentry().readFileData(fileOffset, dest, 0, dest.length);
        destBA.refreshByteBuffer();
    }

    @Override
    public void write(long fileOffset, ByteBuffer src) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void flush() throws IOException {
        // Readonly
        // throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public final FileSystem<?> getFileSystem() {
        return entry.getFileSystem();
    }
}
