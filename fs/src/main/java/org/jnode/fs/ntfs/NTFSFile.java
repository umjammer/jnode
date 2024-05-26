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

package org.jnode.fs.ntfs;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jnode.fs.FSFile;
import org.jnode.fs.FSFileSlackSpace;
import org.jnode.fs.FSFileStreams;
import org.jnode.fs.FileSystem;
import org.jnode.fs.ntfs.attribute.NTFSAttribute;
import org.jnode.fs.ntfs.index.IndexEntry;
import org.jnode.util.ByteBufferUtils;

import static java.lang.System.getLogger;


/**
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSFile implements FSFile, FSFileSlackSpace, FSFileStreams {

    private static final Logger logger = getLogger(NTFSFile.class.getName());

    /**
     * The associated file record.
     */
    private FileRecord fileRecord;

    /**
     * The file system that contains this file.
     */
    private final NTFSFileSystem fs;

    private IndexEntry indexEntry;

    /**
     * Initialize this instance.
     *
     * @param fs         the file system.
     * @param indexEntry the index entry.
     */
    public NTFSFile(NTFSFileSystem fs, IndexEntry indexEntry) {
        this.fs = fs;
        this.indexEntry = indexEntry;
    }

    /**
     * Initialize this instance.
     *
     * @param fs         the file system.
     * @param fileRecord the file record.
     */
    public NTFSFile(NTFSFileSystem fs, FileRecord fileRecord) {
        this.fs = fs;
        this.fileRecord = fileRecord;
    }

    @Override
    public long getLength() {
        Iterator<NTFSAttribute> attributes =
            getFileRecord().findAttributesByTypeAndName(NTFSAttribute.Types.DATA, null);

        if (!attributes.hasNext() && indexEntry != null) {
            // Fall back to the size stored in the index entry if the data attribute is not present (even possible??)
            FileNameAttribute.Structure fileName = new FileNameAttribute.Structure(
                indexEntry, IndexEntry.CONTENT_OFFSET);
            return fileName.getRealSize();
        }

        return getFileRecord().getAttributeTotalSize(NTFSAttribute.Types.DATA, null);
    }

    @Override
    public void setLength(long length) {
        // TODO Auto-generated method stub
    }

    @Override
    public void read(long fileOffset, ByteBuffer destBuf) throws IOException {
        // TODO optimize it also to use ByteBuffer at lower level
        final ByteBufferUtils.ByteArray destBA = ByteBufferUtils.toByteArray(destBuf);
        final byte[] dest = destBA.toArray();
        getFileRecord().readData(fileOffset, dest, 0, dest.length);
        destBA.refreshByteBuffer();
    }

    @Override
    public void write(long fileOffset, ByteBuffer src) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public FileSystem<?> getFileSystem() {
        return fs;
    }

    /**
     * @return Returns the fileRecord.
     */
    public FileRecord getFileRecord() {
        if (fileRecord == null) {
            try {
                fileRecord = indexEntry.getParentFileRecord().getVolume().getMFT().getIndexedFileRecord(indexEntry);
            } catch (IOException e) {
                logger.log(Level.TRACE, e.getMessage(), e);
            }
        }
        return this.fileRecord;
    }

    /**
     * @param fileRecord The fileRecord to set.
     */
    public void setFileRecord(FileRecord fileRecord) {
        this.fileRecord = fileRecord;
    }

    @Override
    public byte[] getSlackSpace() throws IOException {
        Iterator<NTFSAttribute> dataAttributes = getFileRecord().findAttributesByTypeAndName(
            NTFSAttribute.Types.DATA, null);
        NTFSAttribute attribute = dataAttributes.hasNext() ? dataAttributes.next() : null;

        if (attribute == null || attribute.isResident()) {
            // If the data attribute is missing there is no slack space. If it is resident then another attribute might
            // immediately follow the data. So for now we'll ignore that case
            return new byte[0];
        }

        int clusterSize = ((NTFSFileSystem) getFileSystem()).getNTFSVolume().getClusterSize();

        int slackSpaceSize = clusterSize - (int) (getLength() % clusterSize);

        if (slackSpaceSize == clusterSize) {
            slackSpaceSize = 0;
        }

        byte[] slackSpace = new byte[slackSpaceSize];
        getFileRecord().readData(NTFSAttribute.Types.DATA, null, getLength(), slackSpace, 0, slackSpace.length, false);

        return slackSpace;
    }

    /**
     * Flush any cached data to the disk.
     *
     * @throws IOException when an error occurs
     */
    @Override
    public void flush() throws IOException {
        // TODO implement me
    }

    @Override
    public Map<String, FSFile> getStreams() {
        Set<String> streamNames = new LinkedHashSet<>();

        Iterator<NTFSAttribute> dataAttributes = getFileRecord().findAttributesByType(NTFSAttribute.Types.DATA);

        while (dataAttributes.hasNext()) {
            NTFSAttribute attribute = dataAttributes.next();
            String attributeName = attribute.getAttributeName();

            // The unnamed data attribute is the main file data, so ignore it
            if (attributeName != null) {
                streamNames.add(attributeName);
            }
        }

        Map<String, FSFile> streams = new HashMap<>();
        for (String streamName : streamNames) {
            streams.put(streamName, new StreamFile(streamName));
        }

        return streams;
    }

    /**
     * A file for reading data out of alternate streams.
     */
    public class StreamFile implements FSFile {
        /**
         * The name of the alternate data stream.
         */
        private final String attributeName;

        /**
         * Creates a new stream file.
         *
         * @param attributeName the name of the alternate data stream.
         */
        public StreamFile(String attributeName) {
            this.attributeName = attributeName;
        }

        /**
         * Gets the name of this stream.
         *
         * @return the stream name.
         */
        public String getStreamName() {
            return attributeName;
        }

        /**
         * Gets the associated file record.
         *
         * @return the file record.
         */
        public FileRecord getFileRecord() {
            return NTFSFile.this.getFileRecord();
        }

        @Override
        public long getLength() {
            return NTFSFile.this.getFileRecord().getAttributeTotalSize(NTFSAttribute.Types.DATA, attributeName);
        }

        @Override
        public void setLength(long length) throws IOException {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public void read(long fileOffset, ByteBuffer dest) throws IOException {
            ByteBufferUtils.ByteArray destByteArray = ByteBufferUtils.toByteArray(dest);
            byte[] destBuffer = destByteArray.toArray();

            if (fileOffset + destBuffer.length > getLength()) {
                throw new IOException("Attempt to read past the end of stream, offset: " + fileOffset);
            }

            getFileRecord().readData(NTFSAttribute.Types.DATA, attributeName, fileOffset, destBuffer, 0,
                destBuffer.length, true);
            destByteArray.refreshByteBuffer();
        }

        @Override
        public void write(long fileOffset, ByteBuffer src) throws IOException {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public FileSystem<?> getFileSystem() {
            return NTFSFile.this.getFileSystem();
        }
    }
}
