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
import java.lang.System.Logger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.net.nfs.nfs2.CreateDirectoryResult;
import org.jnode.net.nfs.nfs2.CreateFileResult;
import org.jnode.net.nfs.nfs2.Entry;
import org.jnode.net.nfs.nfs2.FileAttribute;
import org.jnode.net.nfs.nfs2.ListDirectoryResult;
import org.jnode.net.nfs.nfs2.LookupResult;
import org.jnode.net.nfs.nfs2.NFS2Client;
import org.jnode.net.nfs.nfs2.NFS2Exception;
import org.jnode.net.nfs.nfs2.Time;

import static java.lang.System.getLogger;


/**
 * @author Andrei Dore
 */
public class NFS2Directory extends NFS2Object implements FSDirectory {

    private static final Logger logger = getLogger(NFS2Directory.class.getName());

    private static final Iterator<NFS2Entry> EMPTY_NFSENTRY_ITERATOR = new Iterator<>() {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public NFS2Entry next() {
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

    private static final boolean[] DEFAULT_PERMISSION =
        new boolean[]{true, true, false, true, false, false, true, false, false};

    private final TableEntry tableEntry;

    private final NFS2Entry directoryEntry;

    NFS2Directory(NFS2Entry entry) {
        super((NFS2FileSystem) entry.getFileSystem());
        this.directoryEntry = entry;
        this.tableEntry = new TableEntry();
    }

    /**
     * Gets the entry with the given name.
     *
     * @param name the name
     * @throws java.io.IOException when an error occurs
     */
    @Override
    public NFS2Entry getEntry(String name) throws IOException {
        NFS2Entry entry = tableEntry.getEntry(name);
        if (entry != null) {
            return entry;
        }

        NFS2Client nfsClient = getNFS2Client();
        LookupResult lookupResult;
        try {
            lookupResult = nfsClient.lookup(directoryEntry.getFileHandle(), name);
        } catch (NFS2Exception e) {
            throw new IOException("Can not call the rpc method." + e.getMessage(), e);
        }
        entry = new NFS2Entry(
            (NFS2FileSystem) getFileSystem(), this, name,
            lookupResult.getFileHandle(), lookupResult.getFileAttribute());
        if (!(entry.isDirectory() || entry.isFile())) {
            return null;
        }
        tableEntry.addEntry(entry);
        return entry;
    }

    @Override
    public FSEntry getEntryById(String id) throws IOException {
        return getEntry(id);
    }

    /**
     * Gets an iterator used to iterate over all the entries of this directory.
     * All elements returned by the iterator must be instanceof FSEntry.
     */
    @Override
    public Iterator<? extends NFS2Entry> iterator() throws IOException {
        final NFS2Client nfsClient = getNFS2Client();

        @SuppressWarnings("unused")
        FileAttribute fileAttribute;
        try {
            fileAttribute =
                nfsClient.getAttribute(getNFS2Entry().getFileHandle());

        } catch (NFS2Exception e) {
            throw new IOException("An error occurs when the nfs file system tried to retrieve " +
                        "the file attributes for directory", e);
        }

        // check to see if the directory was modified
        // TODO Check to see if is correct .

//logger.log(Level.DEBUG, "FN" + fileAttribute.getLastModified());
//logger.log(Level.DEBUG, "FO" + getEntry().getFileAttribute().getLastModified());
//logger.log(Level.DEBUG, "size------------------" + tableEntry.size());
//        if (fileAttribute.getLastModified().getSeconds() ==
//                getEntry().getFileAttribute().getLastModified().getSeconds()
//                && tableEntry.size() != 0) {
//            return tableEntry.getEntrySet().iterator();
//        }

        // clear the cache
        tableEntry.clear();

        // fetch the entries
        Set<NFS2Entry> nfsEntrySet = getNFS2EntrySet();

        if (nfsEntrySet.isEmpty()) {
            return EMPTY_NFSENTRY_ITERATOR;
        }

        for (NFS2Entry nfsEntry : nfsEntrySet) {
            tableEntry.addEntry(nfsEntry);
        }
        return nfsEntrySet.iterator();
    }

    private Set<NFS2Entry> getNFS2EntrySet() throws IOException {
        final NFS2Client nfsClient = getNFS2Client();
        Set<NFS2Entry> nfsEntrySet;

        try {
            Set<Entry> entrySet = new LinkedHashSet<>();
            boolean eof = false;
            byte[] cookie = new byte[NFS2Client.COOKIE_SIZE];
            while (!eof) {
                ListDirectoryResult result = nfsClient.listDirectory(
                    directoryEntry.getFileHandle(), cookie, 2048);
                entrySet.addAll(result.getEntryList());
                if (result.isEof()) {
                    eof = true;
                } else {
                    // I guess that the list contains at least one entry.
                    cookie = result.getEntryList().get(
                        result.getEntryList().size() - 1).cookie();
                }
            }

            if (entrySet.isEmpty()) {
                return new HashSet<>();
            }

            nfsEntrySet =
                    new LinkedHashSet<>(entrySet.size());

            for (Entry entry : entrySet) {
                LookupResult lookupResult = nfsClient.lookup(
                    directoryEntry.getFileHandle(), entry.name());

                NFS2Entry nfsEntry = new NFS2Entry(
                    (NFS2FileSystem) getFileSystem(),
                    NFS2Directory.this, entry.name(), lookupResult.getFileHandle(),
                    lookupResult.getFileAttribute());

                if (!(nfsEntry.isDirectory() || nfsEntry.isFile())) {
                    continue;
                }
                nfsEntrySet.add(nfsEntry);
            }

        } catch (NFS2Exception e) {
            throw new IOException("An error occurs when the nfs file system tried to fetch" +
                    " the content of the  directory", e);
        }

        return nfsEntrySet;
    }

    @Override
    public FSEntry addDirectory(String name) throws IOException {

        NFS2Client nfsClient = getNFS2Client();

        CreateDirectoryResult result;
        try {
            result = nfsClient.createDirectory(directoryEntry.getFileHandle(), name,
                DEFAULT_PERMISSION, -1, -1, -1, new Time(-1, -1), new Time(-1, -1));
        } catch (NFS2Exception e) {
            throw new IOException("Can not create the directory " + name + "." + e.getMessage(), e);
        }

        NFS2Entry entry =
            new NFS2Entry((NFS2FileSystem) getFileSystem(), this, name, result.getFileHandle(),
                result.getFileAttribute());
        tableEntry.addEntry(entry);
        return entry;
    }

    @Override
    public FSEntry addFile(String name) throws IOException {
        NFS2Client nfsClient = getNFS2Client();

        CreateFileResult result;
        try {
            result = nfsClient.createFile(directoryEntry.getFileHandle(), name, DEFAULT_PERMISSION,
                -1, -1, -1, new Time(-1, -1), new Time(-1, -1));
        } catch (NFS2Exception e) {
            throw new IOException("Can not create the file " + name + "." + e.getMessage(), e);
        }

        NFS2Entry entry =
            new NFS2Entry((NFS2FileSystem) getFileSystem(), this, name, result.getFileHandle(),
                result.getFileAttribute());

        tableEntry.addEntry(entry);

        return entry;
    }

    @Override
    public void flush() throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    public void remove(String name) throws IOException {
        NFS2Entry entry = getEntry(name);
        if (entry == null) {
            return;
        }

        NFS2Client nfsClient = getNFS2Client();

        if (entry.isDirectory()) {
            try {
                nfsClient.removeDirectory(directoryEntry.getFileHandle(), name);
            } catch (NFS2Exception e) {
                throw new IOException("Can not remove directory " + name + "." + e.getMessage(), e);
            }
        } else {
            try {
                nfsClient.removeFile(directoryEntry.getFileHandle(), name);
            } catch (NFS2Exception e) {
                throw new IOException("Can not remove file " + name + "." + e.getMessage(), e);
            }
        }
        tableEntry.removeEntry(name);
    }

    public NFS2Entry getNFS2Entry() {
        return directoryEntry;
    }
}
