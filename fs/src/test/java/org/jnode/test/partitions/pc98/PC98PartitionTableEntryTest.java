/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.jnode.test.partitions.pc98;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.driver.block.FileDevice;
import org.jnode.driver.block.VirtualDisk;
import org.jnode.driver.block.VirtualDiskDevice;
import org.jnode.fs.FileSystem;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.pc98.PC98PartitionTableType;
import org.jnode.test.fs.DataStructureAsserts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import vavi.nio.file.jnode.VirtualDiskFactory;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * PC98PartitionTableEntryTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/07 umjammer initial version <br>
 */
@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file://${user.dir}/local.properties")
class PC98PartitionTableEntryTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property
    String nhd;

    @BeforeEach
    void before() throws IOException {
        PropsEntity.Util.bind(this);
    }

    @Test
    @DisplayName("nhd direct")
    void test() throws Exception {
        File file = new File(nhd);
        FileDevice device = new FileDevice(file, "r");

        // skip nhd header
        device.addOffset(512);

        byte[] bytes = new byte[1024];
        device.getAPI(FSBlockDeviceAPI.class).read(0, ByteBuffer.wrap(bytes));

        boolean r = new PC98PartitionTableType().supports(bytes, device.getAPI(BlockDeviceAPI.class));
        assertTrue(r);
    }

    @Test
    @DisplayName("nhd spi, FileDevice, pc98:fat16")
    void test2() throws Exception {
        File file = new File(nhd);
        FileDevice device = new FileDevice(file, "r");

        // skip nhd header
        device.addOffset(512);

        FileSystem<?> fs = PartitionTable.getFileSystem(device, 0);
Debug.println("\nfile system: " + fs);

        StringBuilder actual = new StringBuilder();
        DataStructureAsserts.buildStructure(fs.getRootEntry(), actual, "");
Debug.println("\n: " + actual);

        assertNotNull(fs);
    }

    @Test
    @DisplayName("nhd spi, VirtualDiskDevice, pc98:fat16")
    void test3() throws Exception {
        Path path = Paths.get(nhd);
        VirtualDisk virtualDisk = VirtualDiskFactory.getInstance().createVirtualDiskFactory(path);
        VirtualDiskDevice device = new VirtualDiskDevice(virtualDisk);

        FileSystem<?> fs = PartitionTable.getFileSystem(device, 0);
Debug.println("\nfile system: " + fs);

        StringBuilder actual = new StringBuilder();
        DataStructureAsserts.buildStructure(fs.getRootEntry(), actual, "");
Debug.println("\n: " + actual);

        assertNotNull(fs);
    }
}

/* */
