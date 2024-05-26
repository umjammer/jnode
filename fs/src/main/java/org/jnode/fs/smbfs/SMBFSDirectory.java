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
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;

import static java.lang.System.getLogger;


/**
 * @author Levente Sántha
 */
public class SMBFSDirectory extends SMBFSEntry implements FSDirectory {

    private static final Logger logger = getLogger(SMBFSDirectory.class.getName());

    private static final long REFRESH_TIMEOUT = 5000;
    private final Map<String, SMBFSEntry> entries = new HashMap<>();

    protected SMBFSDirectory(SMBFSDirectory parent, SmbFile smbFile) {
        super(parent, smbFile);
    }

    @Override
    public SMBFSEntry addDirectory(final String name) throws IOException {
        SmbFile dir = new SmbFile(smbFile, dirName(name));
        dir.mkdir();
        SMBFSDirectory sdir = new SMBFSDirectory(SMBFSDirectory.this, dir);
        entries.put(name, sdir);
        return sdir;
    }

    @Override
    public SMBFSEntry addFile(final String name) throws IOException {
        SmbFile file = new SmbFile(smbFile, name);
        file.createNewFile();
        SMBFSFile sfile = new SMBFSFile(SMBFSDirectory.this, file);
        entries.put(name, sfile);
        return sfile;
    }

    @Override
    public void flush() throws IOException {
        // nothing to do here
    }

    @Override
    public SMBFSEntry getEntry(String name) throws IOException {
        refreshEntries();
        return entries.get(name);
    }

    @Override
    public FSEntry getEntryById(String id) throws IOException {
        return getEntry(id);
    }

    @Override
    public Iterator<? extends SMBFSEntry> iterator() throws IOException {
        refreshEntries();
        return entries.values().iterator();
    }

    @Override
    public void remove(String name) throws IOException {
        SMBFSEntry ent = entries.get(name);
        String fname = ent.isDirectory() ? dirName(name) : name;
        SmbFile file = new SmbFile(smbFile, fname);
        file.delete();
        entries.remove(name);
    }

    private long lastRefresh;

    private void refreshEntries() throws SmbException {
        if (System.currentTimeMillis() - lastRefresh < REFRESH_TIMEOUT)
            return;

        SmbFile[] smb_list;
        try {
            smb_list = smbFile.listFiles();
        } catch (SmbException e) {
            logger.log(Level.DEBUG, e.getMessage(), e);
            throw e;
        }
        entries.clear();

        for (SmbFile f : smb_list) {
            if (f.isDirectory()) {
                String name = getSimpleName(f);
                entries.put(name, new SMBFSDirectory(SMBFSDirectory.this, f));
            } else if (f.isFile()) {
                String name = getSimpleName(f);
                entries.put(name, new SMBFSFile(SMBFSDirectory.this, f));
            }
        }
        lastRefresh = System.currentTimeMillis();
    }

    private String dirName(String name) {
        String dname = name;
        if (!dname.endsWith("/"))
            dname += "/";
        return dname;
    }

    @Override
    public String getId() {
        return getName();
    }
}
