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

package org.jnode.fs.ntfs.index;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.fs.ntfs.FileRecord;
import org.jnode.fs.ntfs.attribute.NTFSAttribute;
import org.jnode.util.Queue;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class NTFSIndex {

    private final FileRecord fileRecord;

    /**
     * The name of the index root / allocation attributes to open.
     */
    private final String attributeName;

    private IndexRootAttribute indexRootAttribute;

    private IndexAllocationAttribute indexAllocationAttribute;

    static final Logger log = System.getLogger(NTFSIndex.class.getName());

    /**
     * Initialize this instance.
     *
     * @param fileRecord the file record holding the index streams.
     * @param attributeName the name of the index root / allocation attributes to open.
     */
    public NTFSIndex(FileRecord fileRecord, String attributeName) throws IOException {
        this.fileRecord = fileRecord;
        this.attributeName = attributeName;
    }

    /**
     * Gets the index root attribute.
     */
    public IndexRootAttribute getIndexRootAttribute() {
        if (indexRootAttribute == null) {
            indexRootAttribute = (IndexRootAttribute) 
                    fileRecord.findAttributesByTypeAndName(NTFSAttribute.Types.INDEX_ROOT, attributeName).next();
            log.log(Level.DEBUG, "getIndexRootAttribute: " + indexRootAttribute);
        }
        return indexRootAttribute;
    }

    /**
     * Gets the index allocation attribute, if any.
     */
    public IndexAllocationAttribute getIndexAllocationAttribute() {
        if (indexAllocationAttribute == null) {
            indexAllocationAttribute = (IndexAllocationAttribute) 
                    fileRecord.findAttributesByTypeAndName(NTFSAttribute.Types.INDEX_ALLOCATION, attributeName).next();
        }
        return indexAllocationAttribute;
    }

    /**
     * Searches the index for a value.
     *
     * @param callback the callback to pass each entry in the search to check for a match.
     * @return the matching node, or {@code null} if no match is found.
     */
    public IndexEntry search(IndexSearchCallback callback) {
        Iterator<IndexEntry> rootIterator = getIndexRootAttribute().iterator();

        while (rootIterator.hasNext()) {
            IndexEntry entry = rootIterator.next();

            if (entry.isLastIndexEntryInSubnode()) {
                return searchSubTree(entry, callback);
            } else {
                int compareResult = callback.visitAndCompareEntry(entry);

                if (compareResult == 0) {
                    return entry;
                } else if (compareResult < 0) {
                    return searchSubTree(entry, callback);
                } else {
                    // Maybe in a subsequent node, continue iterating
                }
            }
        }

        // No match
        return null;
    }

    /**
     * Searches a sub-tree of the index for a value.
     *
     * @param topEntry the top entry in this sub-tree of the index.
     * @param callback the callback to pass each entry in the search to check for a match.
     * @return the matching node, or {@code null} if no match is found.
     */
    public IndexEntry searchSubTree(IndexEntry topEntry, IndexSearchCallback callback) {
        if (!topEntry.hasSubNodes()) {
            return null;
        }

        final IndexBlock indexBlock;
        try {
            IndexRoot indexRoot = getIndexRootAttribute().getRoot();
            indexBlock = getIndexAllocationAttribute().getIndexBlock(indexRoot, topEntry.getSubnodeVCN());
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read next index block during search", ex);
        }

        Iterator<IndexEntry> iterator = indexBlock.iterator();

        while (iterator.hasNext()) {
            IndexEntry entry = iterator.next();

            if (entry.isLastIndexEntryInSubnode()) {
                return searchSubTree(entry, callback);
            } else {
                int compareResult = callback.visitAndCompareEntry(entry);

                if (compareResult == 0) {
                    return entry;
                } else if (compareResult < 0) {
                    return searchSubTree(entry, callback);
                } else {
                    // Maybe in a subsequent node, continue iterating
                }
            }
        }

        // No match
        return null;
    }

    public Iterator<IndexEntry> iterator() {
        log.log(Level.DEBUG, "iterator");
        return new FullIndexEntryIterator();
    }

    class FullIndexEntryIterator implements Iterator<IndexEntry> {

        /**
         * List of those IndexEntry's that have a subnode and the subnode has
         * not been visited.
         */
        private final Queue<IndexEntry> subNodeEntries = new Queue<>();

        /** Iterator of current part of the index */
        private Iterator<IndexEntry> currentIterator;

        private IndexEntry nextEntry;

        /**
         * Initialize this instance.
         */
        public FullIndexEntryIterator() {
            log.log(Level.DEBUG, "FullIndexEntryIterator");
            currentIterator = getIndexRootAttribute().iterator();
            log.log(Level.DEBUG, "currentIterator=" + currentIterator);
            readNextEntry();
        }

        @Override
        public boolean hasNext() {
            return (nextEntry != null);
        }

        @Override
        public IndexEntry next() {
            final IndexEntry result = nextEntry;
            if (result == null) {
                throw new NoSuchElementException();
            }
            readNextEntry();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void readNextEntry() {
            while (true) {
                if (currentIterator.hasNext()) {
                    // Read it
                    nextEntry = currentIterator.next();
                    if (nextEntry.hasSubNodes()) {
                        log.log(Level.DEBUG, "next has subnode");
                        subNodeEntries.add(nextEntry);
                    }
                    if (!nextEntry.isLastIndexEntryInSubnode()) {
                        return;
                    }
                }
                nextEntry = null;

                // Do we have subnodes to iterate over?
                if (subNodeEntries.isEmpty()) {
                    // No, we're done
                    log.log(Level.DEBUG, "end of list");
                    return;
                }

                log.log(Level.DEBUG, "hasNext: read next indexblock");
                final IndexEntry entry = subNodeEntries.get();
                final IndexRoot indexRoot = getIndexRootAttribute().getRoot();
                final IndexBlock indexBlock;
                try {
                    indexBlock = getIndexAllocationAttribute().getIndexBlock(indexRoot, entry.getSubnodeVCN());
                } catch (IOException ex) {
                    throw new RuntimeException("Cannot read next index block", ex);
                }
                currentIterator = indexBlock.iterator();
            }
        }
    }
}
