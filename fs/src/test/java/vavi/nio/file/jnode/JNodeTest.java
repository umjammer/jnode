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
import org.junit.jupiter.api.DisplayName;
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
    String exfat;
    @Property
    String dmg;

    @BeforeEach
    void before() throws IOException {
        PropsEntity.Util.bind(this);
    }

    @Test
    @DisplayName("by scheme, raw disk")
    void test() throws Exception {
Debug.println("disc: " + exfat + ", " + Files.exists(Paths.get(exfat)));
        URI uri = URI.create("jnode:exfat:file://" + exfat);
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
//        Files.list(fs.getRootDirectories().iterator().next()).forEach(System.err::println);
        Files.walk(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    @DisplayName("by partition, parted disk")
    void test2() throws Exception {
Debug.println("disc: " + dmg + ", " + Files.exists(Paths.get(dmg)));
        URI uri = URI.create("jnode:file://" + dmg);
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
        Files.walk(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    @DisplayName("by partition, raw disk")
    void test3() throws Exception {
Debug.println("disc: " + exfat + ", " + Files.exists(Paths.get(exfat)));
        URI uri = URI.create("jnode:file://" + exfat);
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
        Files.walk(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void test4() throws Exception {
Debug.println("disc: " + dmg + ", " + Files.exists(Paths.get(dmg)));
        URI uri = URI.create("jnode:file://" + dmg);
        FileSystem fs = new JNodeFileSystemProvider().newFileSystem(uri, Collections.emptyMap());
        Files.walk(fs.getRootDirectories().iterator().next()).forEach(p -> {
            try {
                System.err.println(p + ", " + Files.getLastModifiedTime(p));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

/* */
