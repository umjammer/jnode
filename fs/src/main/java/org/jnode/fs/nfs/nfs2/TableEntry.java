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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TableEntry {
    private final Map<String, EntryInfo> entryMap;

    public TableEntry() {
        entryMap = new HashMap<>();
    }

    public synchronized NFS2Entry getEntry(String name) {
        EntryInfo entryInfo = entryMap.get(name);
        if (entryInfo == null) {
            return null;
        }
        if (entryInfo.expirationTime() < System.currentTimeMillis()) {
            entryMap.remove(name);
            return null;
        }
        return entryInfo.entry();
    }

    public synchronized void addEntry(NFS2Entry entry) {
        entryMap.put(entry.getName(), new EntryInfo(entry, System.currentTimeMillis() + 30000));
    }

    public synchronized void removeEntry(String name) {
        entryMap.remove(name);
    }

    public synchronized Set<NFS2Entry> getEntrySet() {
        if (entryMap.isEmpty()) {
            return new HashSet<>();
        }

        Set<NFS2Entry> entrySet = new HashSet<>(entryMap.size());
        for (String name : entryMap.keySet()) {
            NFS2Entry entry = getEntry(name);
            if (entry != null) {
                entrySet.add(entry);
            }
        }
        return entrySet;
    }

    public synchronized void clear() {
        entryMap.clear();
    }

    public synchronized int size() {

        return entryMap.size();
    }

    private record EntryInfo(NFS2Entry entry, long expirationTime) {

    }
}
