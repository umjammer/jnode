/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

package org.jnode.test.fs.hfsplus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.hfsplus.HFSPlusParams;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.HfsPlusFileSystemFormatter;
import org.jnode.fs.hfsplus.HfsPlusFileSystemType;
import org.jnode.fs.hfsplus.SuperBlock;
import org.jnode.test.fs.DataStructureAsserts;
import org.jnode.test.fs.FileSystemTestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HfsPlusFileSystemTest {

    @Test
    public void testReadSmallDisk() throws Exception {

        Device device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/hfsplus/test.hfsplus"), "r");
        HfsPlusFileSystemType type = FileSystemType.lookup(HfsPlusFileSystemType.class);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
                """
                        vol:Kenny total:67108864 free:66035712
                          /;\s
                            southpark.jpeg; 6420; 5a2ec290089ee04a470135f3bda29f94
                            test.txt; 1141; 48b97c1f1defb52c77ce75d55a4b066c
                            \u0000\u0000\u0000\u0000HFS+ Private Data;\s
                        """;

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadDiskWithDirectoryHardLinks() throws Exception {

        Device device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/hfsplus/hard-linked-directories.dmg"), "r");
        HfsPlusFileSystemType type = FileSystemType.lookup(HfsPlusFileSystemType.class);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
                """
                        vol:Hard linked directories total:40960000 free:39428096
                          /;\s
                            .DS_Store; 6148; cbdca44c18b8de8671b413b2023ef664
                            .fseventsd;\s
                              00000000214ea109; 231; 11618d6f301d3672e498609838d23a8c
                              00000000214ea10a; 72; 6f20b722869a82510ca98e99071c4aca
                              fseventsd-uuid; 36; 2b95938d530cb32e96dfc01671095522
                            .HFS+ Private Directory Data\r;\s
                              dir_25;\s
                                file.txt; 38; 23c1bd7263b9abbdbb879e6267d84ff8
                            .journal; 524288; 7c1d0a50a9738dd88572a9cee56c0270
                            .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc
                            .Trashes;\s
                            dir1;\s
                              clone;\s
                                file.txt; 38; 23c1bd7263b9abbdbb879e6267d84ff8
                            dir2;\s
                              clone;\s
                                file.txt; 38; 23c1bd7263b9abbdbb879e6267d84ff8
                            \u0000\u0000\u0000\u0000HFS+ Private Data;\s
                        """;

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadDiskWithFileHardLinks() throws Exception {

        Device device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/hfsplus/hard-linked-files.dmg"), "r");
        HfsPlusFileSystemType type = FileSystemType.lookup(HfsPlusFileSystemType.class);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
                """
                        vol:hard-links total:40960000 free:39436288
                          /;\s
                            .DS_Store; 6148; b5ae7323596898677123c65fcce1be07
                            .fseventsd;\s
                              00000000214eb5f7; 121; 45f770b87c4fb7773466ed4ea7333248
                              00000000214eb5f8; 72; 261dba091a629e61f127ed183b42ae01
                              fseventsd-uuid; 36; f4f9aca6866b93ba4ab04768132dbbf6
                            .HFS+ Private Directory Data\r;\s
                            .journal; 524288; 7d69775e76f5a59e0f8687f792df23dc
                            .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc
                            .Trashes;\s
                            arrest.txt; 1933; bedea6f1277f61a924388fbb58281e4a
                            diapers.txt; 1933; bedea6f1277f61a924388fbb58281e4a
                            \u0000\u0000\u0000\u0000HFS+ Private Data;\s
                              iNode27; 1933; bedea6f1277f61a924388fbb58281e4a
                        """;

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testReadDiskWithCompressedFiles() throws Exception {

        Device device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/hfsplus/compressed.dd"), "r");
        HfsPlusFileSystemType type = FileSystemType.lookup(HfsPlusFileSystemType.class);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
                """
                        vol:Disk Image total:102379520 free:99401728
                          /;\s
                            .DS_Store; 6148; 98cf7ff1fd8e81ba4839043e208fb63e
                            .fseventsd;\s
                              00000000219733ae; 158; f512a16c903a637e27f3e81a10f224a2
                              00000000219733af; 72; fa4d4f58441685c90841126fb5ea35e5
                              fseventsd-uuid; 36; 3495348c1edb3f39b3cd4222024723c0
                            .HFS+ Private Directory Data\r;\s
                            .journal; 524288; ba95a916b83c8478fb22c180893cffff
                            .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc
                            .Trashes;\s
                            compression.html; 81757; 64ae4e5007fdec27518d9073c72a1714
                            compression.html:rsrc; 21007; 6727899de0b20d7dcb92c68ecaa8bfe2
                            small.txt; 6; d15dbfcb847653913855e21370d83af1
                            zlib.txt; 6211; e63c9b3344d96dbd6c5ddd0debde06f0
                            \u0000\u0000\u0000\u0000HFS+ Private Data;\s
                        """;

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testDiskWithIncorrectCompressedFileOnFile() throws Exception {
        // This HFS+ image was created under Linux and it seems like the 'quote.txt' file has the UF_COMPRESSED
        // flag set on it incorrectly
        Device device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/hfsplus/wrong-compressed-flag.dd"), "r");
        HfsPlusFileSystemType type = FileSystemType.lookup(HfsPlusFileSystemType.class);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
                """
                        vol:untitled total:2097152 free:1904640
                          /;\s
                            quote.txt; 165; 357d31c02f4b9161d14182b57769ef7a
                            steve-jobs-holding-iphone.jpg; 107795; 17baa8a85e36a790df697a68362c227d
                            \u0000\u0000\u0000\u0000HFS+ Private Data;\s
                        """;

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testDiskCompressedHardlinks() throws Exception {
        Device device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/hfsplus/compressed-hardlinks.dd"), "r");
        HfsPlusFileSystemType type = FileSystemType.lookup(HfsPlusFileSystemType.class);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
                """
                        vol:Will it work? total:40960000 free:39424000
                          /;\s
                            .fseventsd;\s
                              0000000021b70ddc; 134; c4bd63b946eb863f50b189f2cb253c8c
                              0000000021b70ddd; 72; 3bf09d08a28b8988cec8f4e3c166ee96
                              fseventsd-uuid; 36; 518c962c5c2852fd354b18650e198372
                            .HFS+ Private Directory Data\r;\s
                            .journal; 524288; b324e1aae290bc30297418b2c39cefa3
                            .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc
                            .Trashes;\s
                            coffee-again.txt; 2573; 3a66504af332c4e6d9997e52cce98002
                            coffee.txt; 2573; 3a66504af332c4e6d9997e52cce98002
                            i-own-you.jpg; 24085; a1a91dfb9c2c0db6bec2f55b12a2e97f
                            \u0000\u0000\u0000\u0000HFS+ Private Data;\s
                              iNode24; 2573; 3a66504af332c4e6d9997e52cce98002
                        """;

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testDiskWithLargeCompressedFile() throws Exception {
        Device device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/hfsplus/large-compressed.dmg"), "r");
        HfsPlusFileSystemType type = FileSystemType.lookup(HfsPlusFileSystemType.class);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
                """
                        vol:large compressed file total:40960000 free:39411712
                          /;\s
                            .fseventsd;\s
                              00000000220a3c77; 96; 52a732bebb5103be73aa89617d42a747
                              00000000220a3c78; 72; 54d11397f6d87faf48a42c82aa2df57d
                              fseventsd-uuid; 36; 81d67b0f96aea5c2a1567a28dfa32fb7
                            .HFS+ Private Directory Data\r;\s
                            .journal; 524288; 43c347a01dd468234a75c8f5f126858e
                            .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc
                            .Trashes;\s
                            large-useless-text.txt; 122818; e33db0ee58f4f5413c721b3d99311215
                            large-useless-text.txt:rsrc; 36771; fba9ba66e57abb9e691a6fb62fcd7c17
                            \u0000\u0000\u0000\u0000HFS+ Private Data;\s
                        """;

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    @Disabled("test file missing")
    public void testDiskWithLzvnCompression() throws Exception {
        Device device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/hfsplus/rle-compression.dmg"), "r");
        HfsPlusFileSystemType type = FileSystemType.lookup(HfsPlusFileSystemType.class);
        HfsPlusFileSystem fs = type.create(device, true);

        String expectedStructure =
                """
                        vol:rle-compression total:40960000 free:39092224
                          /;\s
                            .fseventsd;\s
                              000000000039ce79; 109; 151712afa50c634d4e796e06025b6779
                              000000000039ce7a; 71; e659b5b401cb15e01fb9b5dd65b733ef
                              fseventsd-uuid; 36; c2c77c331f977e1b274da06abc7c778a
                            .HFS+ Private Directory Data\r;\s
                            .journal; 524288; b7106768943c0ae7b09e84b5bf75e62f
                            .journal_info_block; 4096; 469270564228a832e83d2ad16e6d8edc
                            .Trashes;\s
                            bash; 628640; f81cce1751382506604e244039bf4724
                            bash:rsrc; 352797; 699818770c06dc378bb2dd13ca159b33
                            \u0000\u0000\u0000\u0000HFS+ Private Data;\s
                        """;

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }

    @Test
    public void testCreate() throws Exception {
        Device device = createTestDisk(false);
        HfsPlusFileSystemType type = FileSystemType.lookup(HfsPlusFileSystemType.class);
        HfsPlusFileSystem fs = type.create(device, false);
        HFSPlusParams params = new HFSPlusParams();
        params.setVolumeName("testdrive");
        params.setBlockSize(HFSPlusParams.OPTIMAL_BLOCK_SIZE);
        params.setJournaled(false);
        params.setJournalSize(HFSPlusParams.DEFAULT_JOURNAL_SIZE);
        fs.create(params);
        SuperBlock vh = fs.getVolumeHeader();
        assertEquals(SuperBlock.HFSPLUS_SUPER_MAGIC, vh.getMagic());
        assertEquals(4096, vh.getBlockSize());

    }

    @Test
    @Disabled("does createTestDisk works properly?")
    public void testRead() throws Exception {
        Device device = createTestDisk(false);
        HfsPlusFileSystemType type = FileSystemType.lookup(HfsPlusFileSystemType.class);
        HfsPlusFileSystem fs = type.create(device, false);
        HFSPlusParams params = new HFSPlusParams();
        params.setVolumeName("testdrive");
        params.setBlockSize(HFSPlusParams.OPTIMAL_BLOCK_SIZE);
        params.setJournaled(false);
        params.setJournalSize(HFSPlusParams.DEFAULT_JOURNAL_SIZE);
        fs.create(params);
        fs.close();
        fs = new HfsPlusFileSystemType().create(device, false);
        fs.read();
        fs.createRootEntry();
        FSDirectory root = fs.getRootEntry().getDirectory();
        assertFalse(root.iterator().hasNext(), "Must be empty");
        root.addDirectory("test");
        fs.flush();
        fs.close();
        fs = new HfsPlusFileSystemType().create(device, false);
        fs.read();
        assertEquals(1, fs.getVolumeHeader().getFolderCount());
        fs.createRootEntry();
        root = fs.getRootEntry().getDirectory();
        assertTrue(root.iterator().hasNext(), "Must contains one directory");
    }

    private Device createTestDisk(boolean formatted) throws IOException {
        Path source = Files.createTempFile("hfsDevice", "tmp");
        byte[] bytes = new byte[10 * 1024 * 1024];
        Random random = new Random(System.currentTimeMillis());
        random.nextBytes(bytes);
        Files.write(source, bytes);
        Device device = new FileDevice(source.toFile(), "rw");
        HfsPlusFileSystemFormatter formatter = new HfsPlusFileSystemFormatter(new HFSPlusParams());
        formatter.format(device);
        return device;
    }
}
