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
import vavi.util.Debug;


/**
 * MyVirtualDisk.
 * <p>
 * virtual disk detector using "vavi-nio-file-emu".
 * </p>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/12 umjammer initial version <br>
 */
public class MyVirtualDisk implements VirtualDisk {

    private Disk disk;

    private SeekableByteChannel sbc;

    public MyVirtualDisk(Path path) throws IOException {
        try {
            disk = Disk.read(path);
        } catch (IllegalArgumentException e) {
Debug.println("raw disk?: " + e);
            disk = new Disk() {
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

        sbc = Files.newByteChannel(path);
    }

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
Debug.printf(Level.FINE, "offset: %08x (%08x)", disk.getOffset() + offset, disk.getOffset());
        sbc.position(disk.getOffset() + offset);
        sbc.read(buffer);
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
}

/* */
