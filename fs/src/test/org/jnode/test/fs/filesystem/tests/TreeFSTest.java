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

package org.jnode.test.fs.filesystem.tests;

import java.io.IOException;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.util.FSUtils;
import org.jnode.test.fs.filesystem.AbstractFSTest;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Fabien DUMINY
 */
public class TreeFSTest extends AbstractFSTest {
    public TreeFSTest(FSTestConfig config) {
        super(config);
    }

    @Test
    public void testFSTree() throws IOException, Exception {
        if (!config.isReadOnly()) {
            setUp();

            FSDirectory rootDir = getFs().getRootEntry().getDirectory();
            FSEntry dir1 = rootDir.addDirectory("dir1");
            assertNotNull(rootDir.getEntry("dir1"), "dir1 not added");

            /*FSEntry dir11=*/
            dir1.getDirectory().addDirectory("dir1.1");
            assertNotNull(dir1.getDirectory().getEntry("dir1.1"), "dir11 not added");

            FSDirectory gotRootDir = getFs().getRootEntry().getDirectory();
            //assertNotNull("rootDir not saved", gotRootDir);
            assertTrue(gotRootDir == rootDir, "same ref (gotRootDir) after remount");

            FSEntry gotDir1 = gotRootDir.getEntry("dir1");
            //assertNotNull("dir1 not saved", gotDir1);
            assertTrue(gotDir1 == dir1, "same ref (gotDir1) after remount");
            assertEquals(dir1.getName(), gotDir1.getName(), "returned bad entry");
        }
    }

    @Test
    public void testFSTreeWithRemountAndShortName() throws Exception {
        doTestFSTreeWithRemount(config, "dir1");
    }

    @Test
    public void testFSTreeWithRemountAndLongName() throws Exception {
        doTestFSTreeWithRemount(config, "This is a Long FileName.extension");
    }

    private void doTestFSTreeWithRemount(FSTestConfig config, String fileName) throws Exception {
        if (!config.isReadOnly()) {
            setUp();

            FSDirectory rootDir = getFs().getRootEntry().getDirectory();
            log.debug("### testFSTreeWithRemount: rootDir=\n" + FSUtils.toString(rootDir, true));

            FSEntry dir1 = rootDir.addDirectory(fileName);
            assertNotNull(rootDir.getEntry(fileName), "'" + fileName + "' not added");

            log.debug("### testFSTreeWithRemount: before remountFS");
            remountFS(config, getFs().isReadOnly());
            log.debug("### testFSTreeWithRemount: after remountFS");

            FSDirectory gotRootDir = getFs().getRootEntry().getDirectory();
            assertNotNull(gotRootDir, "rootDir not saved");
            assertFalse(gotRootDir == rootDir, "same ref (gotRootDir) after remount");
            log.debug("### testFSTreeWithRemount: gotRootDir=\n" + FSUtils.toString(gotRootDir, true));

            FSEntry gotDir1 = gotRootDir.getEntry(fileName);
            log.debug("### testFSTreeWithRemount: after gotRootDir.getEntry");
            assertNotNull(gotDir1, "'" + fileName + "' not saved");
            assertFalse(gotDir1 == dir1, "same ref (gotDir1) after remount");
            assertEquals(dir1.getName(), gotDir1.getName(), "returned bad entry");
        }
    }
}
