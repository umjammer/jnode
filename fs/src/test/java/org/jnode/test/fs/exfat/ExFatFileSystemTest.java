package org.jnode.test.fs.exfat;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.exfat.ExFatFileSystem;
import org.jnode.fs.exfat.ExFatFileSystemType;
import org.jnode.test.fs.DataStructureAsserts;
import org.jnode.test.fs.FileSystemTestUtils;
import org.junit.jupiter.api.Test;

public class ExFatFileSystemTest {

    private Device device;

    @Test
    public void testReadSmallDisk() throws Exception {

        device = new FileDevice(FileSystemTestUtils.getTestFile("org/jnode/test/fs/exfat/test.exfat"), "r");
        ExFatFileSystemType type = FileSystemType.lookup(ExFatFileSystemType.class);
        ExFatFileSystem fs = type.create(device, true);

        String expectedStructure =
                """
                        vol:Disk Image total:-1 free:-1
                          null;\s
                            ._.Trashes; 4096; f9e90e04b2ae7c188a55c0eb0655f8eb
                            .Trashes;\s
                            .fseventsd;\s
                              fseventsd-uuid; 36; eae15fa70d47f025bbfdc3d58af3dfa4
                              0000000006c7ffbd; 95; 0b9bedacc74534867302e4dcd98fcfcb
                              0000000006c7ffbe; 72; e9e1bbc20e18b28ca4b8f45ce14f21cb
                            test.txt; 179; 73ced839d7039cc88c03ddc225159bd5
                            ._test.txt; 4096; 009b7e7f1db8e0f96e168fd5b0ab583f
                            .DS_Store; 6148; f4ca5ca925aae4c51cf564b7e8fc5ead
                            ._.DS_Store; 4096; 19233eef9b0c16089a3522fb2eefe83f
                        """;

        DataStructureAsserts.assertStructure(fs, expectedStructure);
    }
}
