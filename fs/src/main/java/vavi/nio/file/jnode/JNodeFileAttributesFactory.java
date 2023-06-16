/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import org.jnode.fs.FSEntry;

import com.github.fge.filesystem.driver.ExtendedFileSystemDriverBase.ExtendedFileAttributesFactory;


/**
 * JNodeFileAttributesFactory.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/19 umjammer initial version <br>
 */
public final class JNodeFileAttributesFactory extends ExtendedFileAttributesFactory {

    public JNodeFileAttributesFactory() {
        setMetadataClass(FSEntry.class);
        addImplementation("basic", JNodeBasicFileAttributesProvider.class);
    }
}
