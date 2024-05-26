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

package org.jnode.fs.spi;

import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import java.util.Set;
import javax.security.auth.Subject;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FileSystem;


/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class UnixFSAccessRights implements FSAccessRights {
    private final FileSystem<?> filesystem;

    private final Subject subject = new Subject();

    private final UserPrincipal owner;
    private final Set<Principal> admins;

    private final Rights ownerRights = new Rights(true, true, true);
    private final Rights groupRights = new Rights();
    private final Rights worldRights = new Rights();

    public UnixFSAccessRights(FileSystem<?> filesystem) {
        if (filesystem == null) {
            throw new NullPointerException("filesystem can't be null");
        }
        this.filesystem = filesystem;

        // TODO manages users & groups in JNode
        owner = () -> "root";
        admins = subject.getPrincipals();
        admins.add(owner);
    }

    private Principal getUser() {
        // TODO manages users & groups in JNode
        // we should find the user from the context
        return owner;
    }

    private Rights getUserRights() {
        Principal user = getUser();

        Rights rights = worldRights;
        if (owner.equals(user)) {
            rights = ownerRights;
        } else if (admins.contains(user)) {
            rights = groupRights;
        }

        return rights;
    }

    @Override
    public boolean canExecute() {
        return getUserRights().isExecute();
    }

    @Override
    public boolean canRead() {
        return getUserRights().isRead();
    }

    @Override
    public boolean canWrite() {
        return getUserRights().isWrite();
    }

    @Override
    public Principal getOwner() {
        return owner;
    }

    @Override
    public boolean setExecutable(boolean enable, boolean ownerOnly) {
        if (!owner.equals(getUser())) {
            return false;
        }

        ownerRights.setExecute(enable);
        if (!ownerOnly) {
            groupRights.setExecute(enable);
            worldRights.setExecute(enable);
        }
        return true;
    }

    @Override
    public boolean setReadable(boolean enable, boolean ownerOnly) {
        if (!owner.equals(getUser())) {
            return false;
        }

        ownerRights.setRead(enable);
        if (!ownerOnly) {
            groupRights.setRead(enable);
            worldRights.setRead(enable);
        }
        return true;
    }

    @Override
    public boolean setWritable(boolean enable, boolean ownerOnly) {
        if (!owner.equals(getUser())) {
            return false;
        }

        ownerRights.setWrite(enable);
        if (!ownerOnly) {
            groupRights.setWrite(enable);
            worldRights.setWrite(enable);
        }
        return true;
    }

    @Override
    public FileSystem<?> getFileSystem() {
        return filesystem;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    private static class Rights {
        private boolean read;
        private boolean write;
        private boolean execute;

        public Rights() {
            this(false, false, false);
        }

        public Rights(boolean read, boolean write, boolean execute) {
            this.read = read;
            this.write = write;
            this.execute = execute;
        }

        public boolean isRead() {
            return read;
        }

        public void setRead(boolean read) {
            this.read = read;
        }

        public boolean isWrite() {
            return write;
        }

        public void setWrite(boolean write) {
            this.write = write;
        }

        public boolean isExecute() {
            return execute;
        }

        public void setExecute(boolean execute) {
            this.execute = execute;
        }
    }
}
