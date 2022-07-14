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

package org.jnode.fs;

import java.util.ServiceLoader;

import org.jnode.driver.Device;

/**
 * Descriptor and entry point for a class of file systems.
 * 
 * @param <T> {@link FileSystem}
 * 
 * @author epr
 * 
 */
public interface FileSystemType<T extends FileSystem<?>> {

    /**
     * Gets the unique name of this file system type.
     *
     * @return name of the file system.
     */
    String getName();

    /**
     * Create a file system from a given device.
     * 
     * @param device {@link Device} contains the file system.
     * @param readOnly set to <tt>true</tt> if the new file system must be read
     *            only.
     * @return a file system
     * @throws FileSystemException if error occurs during creation of the new
     *             file system.
     */
    T create(Device device, boolean readOnly) throws FileSystemException;

    /** */
    String getScheme();

    /** factory */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static <T extends FileSystemType> T lookup(Class<T> clazz) {
        ServiceLoader<FileSystemType> sl = ServiceLoader.load(FileSystemType.class);
        for (FileSystemType fst : sl) {
            if (clazz.isInstance(fst)) {
                return (T) fst;
            }
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    /** factory */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static <T extends FileSystemType> T lookup(String scheme) {
        ServiceLoader<FileSystemType> sl = ServiceLoader.load(FileSystemType.class);
        for (FileSystemType fst : sl) {
            if (fst.getScheme().equals(scheme)) {
                return (T) fst;
            }
        }
        throw new IllegalArgumentException(scheme);
    }
}
