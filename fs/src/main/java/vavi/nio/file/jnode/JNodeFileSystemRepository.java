/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;

import org.jnode.fs.FileSystem;

import com.github.fge.filesystem.driver.FileSystemDriver;
import com.github.fge.filesystem.provider.FileSystemRepositoryBase;

import vavi.util.Debug;


/**
 * JNodeFileSystemRepository.
 * <p>
 * env
 * <ul>
 * </ul>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/19 umjammer initial version <br>
 */
public final class JNodeFileSystemRepository extends FileSystemRepositoryBase {

    /** */
    public JNodeFileSystemRepository() {
        super("jnode", new JNodeFileSystemFactoryProvider());
    }

    /**
     * @param uri use {@link DuFileSystemProvider#createURI(String)}
     * @throws IllegalArgumentException only "file" scheme is supported, or uri syntax error
     * @throws NoSuchElementException required values are not in env
     * @throws IndexOutOfBoundsException no suitable {@link LogicalVolumeInfo} or {@link FileSystemInfo}
     */
    @Override
    public FileSystemDriver createDriver(final URI uri, final Map<String, ?> env) throws IOException {
        try {
            String[] rawSchemeSpecificParts = uri.getRawSchemeSpecificPart().split("!");
Debug.println(Level.FINE, "part[0]: " + rawSchemeSpecificParts[0]);
            URI filePart = new URI(rawSchemeSpecificParts[0]);
            if (!"file".equals(filePart.getScheme())) {
                // currently only support "file"
                throw new IllegalArgumentException(filePart.toString());
            }
            String file = rawSchemeSpecificParts[0].substring("file:".length());
            // TODO virtual relative directory from rawSchemeSpecificParts[1]

Debug.println(Level.FINE, "file: " + file);

            FileSystem<?> fs = null;
            final JNodeFileStore fileStore = new JNodeFileStore(fs, factoryProvider.getAttributesFactory());
            return new JNodeFileSystemDriver<>(fileStore, factoryProvider, fs, env);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /* ad-hoc hack for ignoring checking opacity */
    protected void checkURI(URI uri) {
        Objects.requireNonNull(uri);
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("uri is not absolute");
        }
        if (!getScheme().equals(uri.getScheme())) {
            throw new IllegalArgumentException("bad scheme");
        }
    }
}
