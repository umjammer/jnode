/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import org.jnode.driver.block.VirtualDisk;

import vavi.emu.disk.Disk;
import vavi.emu.disk.LogicalDisk;
import vavi.emu.disk.phisical.D88;
import vavi.util.Debug;


/**
 * VirtualDiskFactory.
 * <p>
 * virtual disk detector using "vavi-nio-file-emu".
 * </p>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/12 umjammer initial version <br>
 */
public class VirtualDiskFactory {

    private VirtualDiskFactory() {}

    /** */
    private static final VirtualDiskFactory instance = new VirtualDiskFactory();

    /** */
    public static VirtualDiskFactory getInstance() {
        return instance;
    }

    /** */
    public VirtualDisk createVirtualDiskFactory(Path path) throws IOException {
        Disk disk;
        try {
            disk = Disk.read(path);
Debug.println("disk: " + disk + ", bps: " + disk.getSectorSize() + ", offset: " + disk.getOffset());
            // TODO basically jnode has capability of logical disk detection,
            //  but it's for only solid image or header + solid image.
            //  so image that has other info among disk data (e.g. sector info) like "d88"
            //  is not available currently
            if (disk.getSectorSize() == -1) {
Debug.println("no sector size, try to post read");
                try {
                    LogicalDisk logicalDisk = LogicalDisk.read(path, disk);
Debug.println("logicalDisk: " + logicalDisk + ", bps: " + disk.getSectorSize() + ", offset: " + disk.getOffset());
                } catch (IllegalArgumentException e) { // not found for logicalDisk
Debug.println("no logicalDisk: " + e);
                }
            }
            assert disk.getSectorSize() != -1 : "bytes per sector should be defined";
        } catch (IllegalArgumentException e) { // not found for disk
Debug.println("raw disk?: " + e);
            disk = createNullDisk(); // TODO ???
        }

        return createByPhysical(disk, Files.newByteChannel(path));
    }

    /** */
    private Disk createNullDisk() {
        return new Disk() {
            {
                headerSize = 0;
                bytesPerSector = 512;
            }
            @Override protected String imageTypeText() {return null;}
            @Override public void read(SeekableByteChannel sbc) throws IOException {}
            @Override public void save(Path path) throws IOException {}
            @Override public String imageDescText() { return null; }
            @Override public String filterDesc() { return null; }
            @Override public String filterExt() { return null; }
            @Override public Type getType() { return null; }
        };
    }

    /** */
    private VirtualDisk createByPhysical(Disk disk, SeekableByteChannel sbc) {
        return new VirtualDisk() {
            @Override
            public void write(long offset, ByteBuffer buffer) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setLength(long length) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void read(long offset, ByteBuffer buffer) throws IOException {
Debug.printf(Level.FINER, "offset: %08x, (+o:%08x o:%08x)", offset, disk.getOffset() + offset, disk.getOffset());
                if (offset != 0 && disk instanceof D88) {
                    int[] r = disk.search((int) offset);
                    if (r == null) {
Debug.printf(Level.WARNING, "no such sector of offset: %08x", offset);
                        throw new IOException(String.format("no such sector of offset: %08x", offset));
                    }
                    sbc.read(ByteBuffer.wrap(disk.getSector(r[0], r[1], r[2]).data));
                } else {
                    sbc.position(disk.getOffset() + offset);
                    sbc.read(buffer);
                }
            }

            @Override
            public int getSectorSize() {
                return disk.getSectorSize();
            }

            @Override
            public long getLength() {
                return disk.getLength();
            }

            @Override
            public int getSectors() {
                return disk.getGeometry().sectors;
            }

            @Override
            public int getHeads() {
                return disk.getGeometry().heads;
            }

            @Override
            public void close() throws IOException {
                sbc.close();
            }
        };
    }
}
