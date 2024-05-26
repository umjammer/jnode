/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Objects;
import java.util.Set;

import org.jnode.fs.FSEntry;

import com.github.fge.filesystem.attributes.provider.BasicFileAttributesProvider;


import static java.lang.System.getLogger;


/**
 * {@link BasicFileAttributes} implementation for JNode.
 */
public final class JNodeBasicFileAttributesProvider extends BasicFileAttributesProvider implements PosixFileAttributes {

    private static final Logger logger = getLogger(JNodeBasicFileAttributesProvider.class.getName());

    private final FSEntry entry;

    public JNodeBasicFileAttributesProvider(FSEntry entry) throws IOException {
        this.entry = Objects.requireNonNull(entry);
    }

    /**
     * Returns the time of last modification.
     * <p>
     * If the file system implementation does not support a time stamp
     * to indicate the time of last modification then this method returns an
     * implementation specific default value, typically a {@code FileTime}
     * representing the epoch (1970-01-01T00:00:00Z).
     *
     * @return a {@code FileTime} representing the time the file was last
     *         modified
     */
    @Override
    public FileTime lastModifiedTime() {
        try {
            return FileTime.fromMillis(entry.getLastModified());
        } catch (IOException e) {
logger.log(Level.ERROR, e.getMessage(), e);
            return FileTime.fromMillis(0);
        }
    }

    /**
     * Tells whether the file is a regular file with opaque content.
     */
    @Override
    public boolean isRegularFile() {
        return entry.isFile();
    }

    /**
     * Tells whether the file is a directory.
     */
    @Override
    public boolean isDirectory() {
        return entry.isDirectory();
    }

    /**
     * Returns the size of the file (in bytes). The size may differ from the
     * actual size on the file system due to compression, support for sparse
     * files, or other reasons. The size of files that are not {@link
     * #isRegularFile regular} files is implementation specific and
     * therefore unspecified.
     *
     * @return the file size, in bytes
     */
    @Override
    public long size() {
        try {
            return entry.getFile().getLength();
        } catch (IOException e) {
logger.log(Level.ERROR, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public UserPrincipal owner() {
        return null;
    }

    @Override
    public GroupPrincipal group() {
        return null;
    }

    @Override
    public Set<PosixFilePermission> permissions() {
        return isDirectory() ? PosixFilePermissions.fromString("rwxr-xr-x") : PosixFilePermissions.fromString("rw-r--r--");
    }
}
