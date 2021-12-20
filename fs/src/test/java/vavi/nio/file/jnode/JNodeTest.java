/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * JNodeTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/20 umjammer initial version <br>
 */
@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file://${user.dir}/local.properties")
class JNodeTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property
    String diskImage;

    @BeforeEach
    void before() throws IOException {
        PropsEntity.Util.bind(this);
    }

    @Test
    void test() throws Exception {
Debug.println("diskImage: " + diskImage + ", " + Files.exists(Paths.get(diskImage)));
        URI uri = URI.create("jnode:exfat:file://" + diskImage);
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
//        Files.list(fs.getRootDirectories().iterator().next()).forEach(System.err::println);
        Files.list(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

/* */
