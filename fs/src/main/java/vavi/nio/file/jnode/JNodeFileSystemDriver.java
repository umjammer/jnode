/*
 * Copyright (c) 2021 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.jnode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.CopyOption;
import java.nio.file.FileStore;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.util.FileUtils;

import com.github.fge.filesystem.driver.ExtendedFileSystemDriver;
import com.github.fge.filesystem.exceptions.IsDirectoryException;
import com.github.fge.filesystem.provider.FileSystemFactoryProvider;

import static vavi.nio.file.Util.toPathString;


/**
 * ExFatFileSystemDriver.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2021/12/19 umjammer initial version <br>
 */
public final class JNodeFileSystemDriver<T extends FSEntry> extends ExtendedFileSystemDriver<T> {

    private FileSystem<T> fs;

    public JNodeFileSystemDriver(final FileStore fileStore,
            FileSystemFactoryProvider provider,
            FileSystem<T> fs,
            Map<String, ?> env) {
        super(fileStore, provider);
        this.fs = fs;
        setEnv(env);
    }

    private static String toJNodePathString(Path path) throws IOException {
        return toPathString(path).replace(File.separator, "\\").substring(1);
    }

    private static String toJavaPathString(String path) {
        return File.separator + path.replace("\\", File.separator);
    }

    @Override
    protected String getFilenameString(T entry) {
        return toJavaPathString(entry.getName());
    }

    @Override
    protected boolean isFolder(T entry) {
        return entry.isDirectory();
    }

    @Override
    protected boolean exists(T entry) throws IOException {
        try {
            entry.getParent().getEntry(entry.getName());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected T getEntry(Path path)throws IOException {
        T parent = fs.getRootEntry();
        for (int i = 0; i < path.getNameCount(); i++) {
            Path name = path.getName(i);
            try {
                @SuppressWarnings("unchecked")
                T entry = (T) parent.getDirectory().getEntry(name.toString());
                if (i < path.getNameCount() - 1) {
                    parent = entry;
                    continue;
                } else {
                    return entry;
                }
            } catch (IOException e) {
                return null;
            }
        }
        return fs.getRootEntry();
    }

    @Override
    protected InputStream downloadEntry(T entry, Path path, Set<? extends OpenOption> options) throws IOException {
        FSFile file = entry.getFile();
        return new InputStream() {
            @Override
            public int read() throws IOException {
                byte[] b = new byte[1];
                return read(b ,0, 1);
            }
            @Override
            public int read(byte[] b, int ofs, int len) throws IOException {
                ByteBuffer bb = ByteBuffer.wrap(b, ofs, len);
                file.read(0, bb);
                return len;
            }
        };
    }

    @Override
    protected OutputStream uploadEntry(T parentEntry, Path path, Set<? extends OpenOption> options) throws IOException {
        FSFile file = parentEntry.getDirectory().addFile(toJNodePathString(path)).getFile();
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                write(new byte[] { (byte) b } ,0, 1);
            }
            @Override
            public void write(byte[] b, int ofs, int len) throws IOException {
                ByteBuffer bb = ByteBuffer.wrap(b, ofs, len);
                file.write(0, bb);
            }
        };
    }

    @Override
    protected List<T> getDirectoryEntries(T dirEntry, Path dir) throws IOException {
        @SuppressWarnings("unchecked")
        Iterator<T> iterator = (Iterator<T>) dirEntry.getDirectory().iterator();
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
        return StreamSupport.stream(spliterator, false).collect(Collectors.toList()); 
    }

    @Override
    protected T createDirectoryEntry(T parentEntry, Path dir) throws IOException {
        parentEntry.getDirectory().addDirectory(dir.getFileName().toString());
        return getEntry(dir);
    }

    @Override
    protected boolean hasChildren(T dirEntry, Path dir) throws IOException {
        return getDirectoryEntries(dirEntry, dir).size() > 0;
    }

    @Override
    protected void removeEntry(T entry, Path path) throws IOException {
        entry.getParent().remove(path.getFileName().toString());
    }

    @Override
    protected T copyEntry(T sourceEntry, T targetParentEntry, Path source, Path target, Set<CopyOption> options) throws IOException {
        OutputStream out = uploadEntry(sourceEntry, source, null); // TODO options
        InputStream in = downloadEntry(targetParentEntry, target, null); // TODO options
        FileUtils.copy(in, out, new byte[8192], false);
        return getEntry(target);
    }

    @Override
    protected T moveEntry(T sourceEntry, T targetParentEntry, Path source, Path target, boolean targetIsParent) throws IOException {
        copyEntry(sourceEntry, targetParentEntry, source, target, null);
        removeEntry(sourceEntry, source);
        if (targetIsParent) {
            return getEntry(target.resolve(source.getFileName()));
        } else {
            return getEntry(target);
        }
    }

    @Override
    protected T moveFolderEntry(T sourceEntry, T targetParentEntry, Path source, Path target, boolean targetIsParent) throws IOException {
        // TODO java spec. allows empty folder
        throw new IsDirectoryException("source can not be a folder: " + source);
    }

    @Override
    protected T renameEntry(T sourceEntry, T targetParentEntry, Path source, Path target) throws IOException {
        return moveEntry(sourceEntry, targetParentEntry, source, target, false);
    }

    @Override
    public void close() throws IOException {
        fs.close();
    }
}
