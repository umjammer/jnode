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

package org.jnode.fs.jfat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSEntryCreated;
import org.jnode.fs.FSEntryLastAccessed;
import org.jnode.fs.FSFile;
import org.jnode.util.NumberUtils;

public class FatEntry extends FatObject implements FSEntry, FSEntryCreated, FSEntryLastAccessed {

    private static final Logger log = System.getLogger(FatEntry.class.getName());

    private String name;
    private FatRecord record;
    private FatShortDirEntry entry;
    private FatDirectory parent;
    private FatChain chain;

    /**
     * internal constructor
     */
    protected FatEntry(FatFileSystem fs) {
        super(fs);
    }

    public FatEntry(FatFileSystem fs, FatDirectory parent, FatRecord record) {
        this(fs);
        this.name = record.getLongName();
        this.record = record;
        this.entry = record.getShortEntry();
        this.parent = parent;
        this.chain = new FatChain(fs, entry.getStartCluster());
    }

    private void setRoot() {
        this.name = "";
        this.record = null;
        this.entry = null;
        this.parent = null;
    }

    protected final void setRoot32(int startCluster) {
        setRoot();
        this.chain = new FatChain(getFatFileSystem(), startCluster);
    }

    @Override
    public boolean isDirty() {
        return (entry.isDirty() || chain.isDirty());
    }

    public void delete() throws IOException {
        setValid(false);

        entry.delete();
        parent.setFatDirEntry(entry);
        entry.flush();

        Vector<FatLongDirEntry> v = record.getLongEntries();

        for (FatLongDirEntry l : v) {
            l.delete();
            parent.setFatDirEntry(l);
            l.flush();
        }
    }

    public void freeAllClusters() throws IOException {
        getChain().freeAllClusters();
    }

    public void dumpChain(String fileName) throws IOException {
        chain.dump(fileName);
    }

    public FatRecord getRecord() {
        return record;
    }

    @Override
    public String getId() {
        return Integer.toString(entry.getIndex());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) throws IOException {
        this.name = name;
    }

    public String getShortName() {
        return entry.getShortName();
    }

    public boolean isShortName(byte[] shortName) {
        if (shortName.length != 11)
            throw new IllegalArgumentException("illegal shortname length: " + shortName.length);

        return Arrays.equals(shortName, entry.getName());
    }

    public int getIndex() {
        return entry.getIndex();
    }

    @Override
    public long getCreated() throws IOException {
        return entry.getCreated();
    }

    @Override
    public long getLastModified() throws IOException {
        return entry.getLastModified();
    }

    @Override
    public long getLastAccessed() throws IOException {
        return entry.getLastAccessed();
    }

    public void setCreated(long created) throws IOException {
        entry.setCreated(created);
    }

    @Override
    public void setLastModified(long lastModified) throws IOException {
        entry.setLastModified(lastModified);
    }

    public void setLastAccessed(long lastAccessed) throws IOException {
        entry.setLastAccessed(lastAccessed);
    }

    public FatShortDirEntry getEntry() {
        return entry;
    }

    protected void setEntry(FatShortDirEntry value) {
        this.entry = value;
    }

    public FatChain getChain() {
        return chain;
    }

    public int getStartCluster() {
        return getChain().getStartCluster();
    }

    @Override
    public FatDirectory getParent() {
        return parent;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    public boolean isRoot() {
        return false;
    }

    public void flush() throws IOException {
        if (isDirty()) {
            if (chain.isDirty()) {
                entry.setStartCluster(chain.getStartCluster());
                chain.flush();
            }
            //
            if (entry.isDirty()) {
                parent.setFatDirEntry(entry);
                entry.flush();
            }
        }
    }

    @Override
    public FSDirectory getDirectory() {
        throw new UnsupportedOperationException("getDirectory");
    }

    @Override
    public FSFile getFile() {
        throw new UnsupportedOperationException("getFile");
    }

    @Override
    public FSAccessRights getAccessRights() throws IOException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public String getPath() {
        StringBuilder path = new StringBuilder(1024);
        FatDirectory parent = getParent();

        if (!getName().isEmpty())
            path.append(getName());
        else
            path.append("\\");

        while (parent != null) {
            path.insert(0, parent.getName() + "\\");
            parent = parent.getParent();
        }

        return path.toString();
    }

    public String toStringValue() {
        StrWriter out = new StrWriter();

        int hashCode = System.identityHashCode(this);

        try {
            out.println("---------------------------------------");
            out.println("HashCode\t" + NumberUtils.hex(hashCode, 8));
            out.println("IsDirty\t\t" + isDirty());
            out.println("IsValid\t\t" + isValid());
            out.println("---------------------------------------");
            out.println("Name\t\t" + getName());
            out.println("ShortName\t" + getShortName());
            out.println("Path\t\t" + getPath());
            out.println("LastModified\t" + FatUtils.fTime(getLastModified()));
            out.println("isRoot\t\t" + isRoot());
            out.println("isFile\t\t" + isFile());
            out.println("isDirectory\t" + isDirectory());
            out.println("StartCluster\t" + getStartCluster());
            out.println("Chain\t\t" + getChain());
            out.print("---------------------------------------");
        } catch (IOException ex) {
            log.log(Level.DEBUG, "entry error");
            out.print("entry error");
        }

        return out.toString();
    }

    public String toString() {
        StrWriter out = new StrWriter();

        out.println("*******************************************");
        out.println("FatEntry");
        out.println("*******************************************");
        out.println(toStringValue());
        out.print("*******************************************");

        return out.toString();
    }
}
