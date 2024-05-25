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

package org.jnode.fs.ftpfs;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.enterprisedt.net.ftp.FTPFile;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.ReadOnlyFileSystemException;

import static java.lang.System.getLogger;


/**
 * @author Levente Sántha
 */
public class FTPFSDirectory extends FTPFSEntry implements FSDirectory {

    private static final Logger logger = getLogger(FTPFSDirectory.class.getName());

    private Map<String, FTPFSEntry> entries;

    FTPFSDirectory(FTPFileSystem fileSystem, FTPFile ftpFile) {
        super(fileSystem, ftpFile);
    }

    /**
     * Gets the entry with the given name.
     *
     * @param name the name
     * @throws java.io.IOException when an error occurs
     */
    @Override
    public FTPFSEntry getEntry(String name) throws IOException {
        ensureEntries();
        return entries.get(name);
    }

    @Override
    public FSEntry getEntryById(String id) throws IOException {
        return getEntry(id);
    }

    /**
     * Gets an iterator used to iterate over all the entries of this
     * directory.
     * All elements returned by the iterator must be instanceof FSEntry.
     */
    @Override
    public Iterator<? extends FTPFSEntry> iterator() throws IOException {
        ensureEntries();
        return entries.values().iterator();
    }

    private void ensureEntries() throws IOException {
        try {
            if (entries == null) {
                entries = new HashMap<>();
                FTPFile[] ftpFiles;
                synchronized (fileSystem) {
                    ftpFiles = fileSystem.dirDetails(path());
                }
                for (FTPFile f : ftpFiles) {
                    FTPFSEntry e = f.isDir() ? new FTPFSDirectory(fileSystem, f) : new FTPFSFile(fileSystem, f);
                    e.setParent(FTPFSDirectory.this);
                    entries.put(f.getName(), e);
                }
            }
        } catch (Exception e) {
            logger.log(Level.DEBUG, e.getMessage(), e);
            throw new IOException("Read error", e);
        }
    }

    String path() throws IOException {
        StringBuilder p = new StringBuilder("/");
        FTPFSDirectory root = fileSystem.getRootEntry();
        FTPFSDirectory d = this;
        while (d != root) {
            p.insert(0, d.getName());
            p.insert(0, '/');
            d = d.parent;
        }
        return p.toString();
    }

    /**
     * Add a new (sub-)directory with a given name to this directory.
     *
     * @param name the name
     * @throws java.io.IOException when an error occurs
     */
    @Override
    public FTPFSEntry addDirectory(String name) throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    /**
     * Add a new file with a given name to this directory.
     *
     * @param name the name
     * @throws java.io.IOException when an error occurs
     */
    @Override
    public FTPFSEntry addFile(String name) throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    /**
     * Save all dirty (unsaved) data to the device
     *
     * @throws java.io.IOException when an error occurs
     */
    @Override
    public void flush() throws IOException {
        // nothing to do
    }

    /**
     * Remove the entry with the given name from this directory.
     *
     * @param name the name
     * @throws java.io.IOException when an error occurs
     */
    @Override
    public void remove(String name) throws IOException {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public String getId() {
        return getName();
    }
}
