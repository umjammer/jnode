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

package org.jnode.fs.nfs.nfs2;

import java.io.IOException;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.net.nfs.nfs2.FileAttribute;
import org.jnode.net.nfs.nfs2.NFS2Client;
import org.jnode.net.nfs.nfs2.NFS2Exception;
import org.jnode.net.nfs.nfs2.Time;

/**
 * @author Andrei Dore
 */
public class NFS2Entry extends NFS2Object implements FSEntry {

    private final NFS2Directory parent;

    private NFS2Directory directory;

    private NFS2File file;

    private final byte[] fileHandle;

    private final FileAttribute fileAttribute;

    private final String name;

    @SuppressWarnings("unused")
    private final NFS2AccessRights accessRights;

    NFS2Entry(NFS2FileSystem fileSystem, NFS2Directory parent, String name, byte[] fileHandle,
              FileAttribute fileAttribute) {
        super(fileSystem);

        this.parent = parent;
        this.name = name;
        this.fileAttribute = fileAttribute;
        this.fileHandle = fileHandle;

        if (fileAttribute.getType() == FileAttribute.DIRECTORY) {
            directory = new NFS2Directory(this);
        } else if (fileAttribute.getType() == FileAttribute.FILE) {
            file = new NFS2File(this);
        }
        accessRights = new NFS2AccessRights(fileSystem, this);
    }

    @Override
    public FSDirectory getParent() {
        return parent;
    }

    @Override
    public String getId() {
        return getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FSAccessRights getAccessRights() throws IOException {
        return null;
    }

    @Override
    public FSDirectory getDirectory() throws IOException {
        if (!isDirectory()) {
            throw new IOException(getName() + " is not a directory");
        }
        return directory;
    }

    @Override
    public FSFile getFile() throws IOException {
        if (!isFile()) {
            throw new IOException(getName() + " is not a file");
        }
        return file;
    }

    public long getLastChanged() throws IOException {
        return fileAttribute.getLastStatusChanged().toJavaMillis();
    }

    @Override
    public long getLastModified() throws IOException {
        return fileAttribute.getLastModified().toJavaMillis();
    }

    public long getLastAccessed() throws IOException {
        return fileAttribute.getLastAccessed().toJavaMillis();
    }

    @Override
    public boolean isDirectory() {
        if (fileAttribute.getType() == FileAttribute.DIRECTORY) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isDirty() throws IOException {
        return false;
    }

    @Override
    public boolean isFile() {
        return fileAttribute.getType() == FileAttribute.FILE;
    }

    public void setLastChanged(long lastChanged) throws IOException {
        // TODO: The setAttribute API appears to have no way to do this.
    }

    @Override
    public void setLastModified(long lastModified) throws IOException {
        NFS2Client client = getNFS2Client();
        try {
            client.setAttribute(getFileHandle(), -1, -1, -1, -1,
                new Time(-1, -1), new Time(lastModified));
        } catch (NFS2Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public void setLastAccessed(long lastAccessed) throws IOException {
        NFS2Client client = getNFS2Client();
        try {
            client.setAttribute(getFileHandle(), -1, -1, -1, -1,
                new Time(lastAccessed), new Time(-1, -1));
        } catch (NFS2Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void setName(String newName) throws IOException {
        NFS2Client client = getNFS2Client();
        NFS2Directory parentDirectory = (NFS2Directory) getParent();
        try {
            client.renameFile(
                parentDirectory.getNFS2Entry().getFileHandle(), name,
                parentDirectory.getNFS2Entry().getFileHandle(), newName);
        } catch (NFS2Exception e) {
            throw new IOException("Can not rename ." + e.getMessage(), e);
        }
    }

    public FileAttribute getFileAttribute() {
        return fileAttribute;
    }

    public byte[] getFileHandle() {
        return fileHandle;
    }
}
