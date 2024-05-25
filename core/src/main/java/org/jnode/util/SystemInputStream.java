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

package org.jnode.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * The SystemInputStream is a stream wrapped for the system's actual keyboard
 * input stream.  It deals with the problem that a System.in needs to be set
 * before we have configured the keyboard.  It also supports thread localization
 * of the system input, though the CommandInvoker implementations do not use
 * this anymore.
 */
public final class SystemInputStream extends InputStream {

    // FIXME ... remove the thread localization support.  It is now just a misleading
    //  historical relic.

    private static final InputStream EMPTY = new EmptyInputStream();

    private final class ThreadLocalInputStream extends InheritableThreadLocal<InputStream> {
        @Override
        public InputStream get() {
            InputStream o = super.get();
            if (o == EMPTY) {
                set(systemIn);
                o = systemIn;
            }
            return o;
        }

        @Override
        protected InputStream initialValue() {
            return systemIn;
        }
    }

    private static SystemInputStream instance;

    private InputStream systemIn;

    private ThreadLocalInputStream systemInOwnerLocal;

    private final ThreadLocalInputStream localeIn = new ThreadLocalInputStream();

    public InputStream getIn() {
        return getLocalIn();
    }

    public void setIn(InputStream in) {
        if (in != this) {
            localeIn.set(in);
        }
    }

    public void claimSystemIn() {
        // TODO must be protected by the SecurityManager
        setIn(systemIn);
    }

    public void releaseSystemIn() {
        this.systemIn = EMPTY;
    }

    public static SystemInputStream getInstance() {
        // TODO protect me with SecurityManager !
        if (instance == null) {
            instance = new SystemInputStream();
        }
        return instance;
    }

    /**
     * Set the wrapped stream to the supplied parameter.  This method
     * only has any effect if the wrapped stream is unset.
     *
     * @param systemIn the systemIn
     */
    public void initialize(InputStream systemIn) {
        // TODO protect me with SecurityManager !
        // register only the first keyboard
        if (this.systemIn == EMPTY && systemIn != this)  {
            this.systemIn = systemIn;
        }
    }

    private SystemInputStream() {
        this.systemIn = EMPTY; // by default, no keyboard
    }

    @Override
    public void mark(int readLimit) {
        getLocalIn().mark(readLimit);
    }

    @Override
    public boolean markSupported() {
        return getLocalIn().markSupported();
    }

    @Override
    public void reset() throws IOException {
        getLocalIn().reset();
    }

    @Override
    public int available() throws IOException {
        return getLocalIn().available();
    }

    @Override
    public long skip(long numBytes) throws IOException {
        return getLocalIn().skip(numBytes);
    }

    @Override
    public int read() throws IOException {
        return getLocalIn().read();
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    @Override
    public int read(byte[] buf, int offset, int len) throws IOException {
        return getLocalIn().read(buf, offset, len);
    }

    @Override
    public void close() throws IOException {
        getLocalIn().close();
    }

    private InputStream getLocalIn() {
        return localeIn.get();
    }
}
