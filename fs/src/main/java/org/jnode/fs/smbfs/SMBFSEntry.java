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

package org.jnode.fs.smbfs;

import java.io.IOException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSEntry;

/**
 * @author Levente Sántha
 */
public abstract class SMBFSEntry implements FSEntry {
    final SmbFile smbFile;
    final SMBFSDirectory parent;

    protected SMBFSEntry(SMBFSDirectory parent, SmbFile smbFile) {
        this.parent = parent;
        this.smbFile = smbFile;
    }

    @Override
    public FSAccessRights getAccessRights() throws IOException {
        // todo implement it
        return null;
    }

    @Override
    public SMBFSDirectory getDirectory() throws IOException {
        return (SMBFSDirectory) this;
    }

    @Override
    public SMBFSFile getFile() throws IOException {
        return (SMBFSFile) this;
    }

    public long getCreated() throws IOException {
        return smbFile.createTime();
    }

    @Override
    public long getLastModified() throws IOException {
        return smbFile.getLastModified();
    }

    @Override
    public String getName() {
        return getSimpleName(smbFile);
    }

    static String getSimpleName(SmbFile smbFile) {
        String name = smbFile.getName();
        if (name.endsWith("/"))
            name = name.substring(0, name.length() - 1);
        return name;
    }

    @Override
    public SMBFSDirectory getParent() {
        return parent;
    }

    @Override
    public boolean isDirectory() {
        try {
            return smbFile.isDirectory();
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isDirty() throws IOException {
        // todo implement it
        return false;
    }

    @Override
    public boolean isFile() {
        try {
            return smbFile.isFile();
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCreated(long created) throws IOException {
        smbFile.setCreateTime(created);
    }

    @Override
    public void setLastModified(long lastModified) throws IOException {
        smbFile.setLastModified(lastModified);
    }

    @Override
    public void setName(String newName) throws IOException {
        SmbFile f = new SmbFile(parent.smbFile, newName);
        smbFile.renameTo(f);
    }

    @Override
    public SMBFileSystem getFileSystem() {
        // todo implement it
        return null;
    }

    @Override
    public boolean isValid() {
        // todo implement it
        return true;
    }
}
