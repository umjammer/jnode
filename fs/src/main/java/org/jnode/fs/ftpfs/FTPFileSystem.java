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

package org.jnode.fs.ftpfs;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.text.ParseException;
import java.util.Date;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import org.jnode.fs.FileSystem;

import static java.lang.System.getLogger;


/**
 * @author Levente Sántha
 */
public class FTPFileSystem implements FileSystem<FTPFSDirectory> {

    private static final Logger logger = getLogger(FTPFileSystem.class.getName());

    private final FTPFSDevice device;
    private final FTPFSDirectory root;
    private boolean closed;
    private Thread thread;
    private final FTPClient client;
    private final FTPFileSystemType type;

    FTPFileSystem(final FTPFSDevice device, final FTPFileSystemType type) {
        this.type = type;
        this.client = new FTPClient();
        this.device = device;
        try {
            client.setRemoteHost(device.getHost());
            client.setTimeout(300000);
            client.connect();

            client.login(device.getUser(), device.getPassword());
            thread = new Thread(() -> {
                try {
                    while (!isClosed()) {
                        try {
                            Thread.sleep(100000);
                            nop();
                        } catch (InterruptedException x) {
                            // ignore
                        }
                    }
                } catch (Exception x) {
                    logger.log(Level.ERROR, x.getMessage(), x);
                }
            }, "ftpfs_keepalive");
            thread.start();
            FTPFile f = new FTPFile("/", "/", 0, true, new Date(0));
//            FTPFile f = new FTPFile();
//            f.setName(printWorkingDirectory());
            root = new FTPFSDirectory(this, f);
            closed = false;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public final FTPFileSystemType getType() {
        return type;
    }

    private synchronized void nop() throws Exception {
        client.dir(root.path());
    }

    /**
     * Close this filesystem. After a close, all invocations of method of this filesystem or objects created by this
     * filesystem will throw an IOException.
     *
     * @throws java.io.IOException when an error occurs
     */
    @Override
    public synchronized void close() throws IOException {
        try {
            closed = true;
            thread = null;
            client.quit();
        } catch (Exception e) {
            throw new IOException("Close error");
        }
    }

    /**
     * Gets the device this FS driver operates on.
     */
    public FTPFSDevice getDevice() {
        return device;
    }

    /**
     * Gets the root entry of this filesystem. This is usually a directory, but this is not required.
     */
    @Override
    public FTPFSDirectory getRootEntry() throws IOException {
        return root;
    }

    /**
     * Is this filesystem closed.
     */
    @Override
    public synchronized boolean isClosed() {
        return closed;
    }

    /**
     * Is the filesystem mounted in readonly mode ?
     */
    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public long getFreeSpace() {
        // TODO implement me
        return -1;
    }

    @Override
    public long getTotalSpace() {
        // TODO implement me
        return -1;
    }

    @Override
    public long getUsableSpace() {
        // TODO implement me
        return -1;
    }

    @Override
    public String getVolumeName() throws IOException {
        return "ftp://" + device.getHost();
    }

    FTPFile[] dirDetails(String path) throws IOException, FTPException, ParseException {
        return client.dirDetails(path);
    }

    void chdir(String path) throws IOException, FTPException {
        client.chdir(path);
    }

    byte[] get(String name) throws IOException, FTPException {
        return client.get(name);
    }
}
