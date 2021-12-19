/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import com.github.fge.filesystem.provider.FileSystemFactoryProvider;


/**
 * JNodeFileSystemFactoryProvider.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/19 umjammer initial version <br>
 */
public final class JNodeFileSystemFactoryProvider extends FileSystemFactoryProvider {

    public JNodeFileSystemFactoryProvider() {
        setAttributesFactory(new JNodeFileAttributesFactory());
        setOptionsFactory(new JNodeFileSystemOptionsFactory());
    }
}
