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

import java.nio.ByteBuffer;

public class ByteBufferUtils {

    /**
     * This method is the equivalent of System.arraycopy
     * But, instead of 2 arrays, it takes 2 ByteBuffers.
     *
     * @param src the src buffer
     * @param srcStart the src start index
     * @param dest the dest buffer
     * @param destStart the dest start index
     * @param len the length
     */
    public static void copyBuffer(ByteBuffer src, int srcStart,
                                  ByteBuffer dest, int destStart, int len) {
        src.position(srcStart);
        src.limit(srcStart + len);

        dest.position(destStart);
        dest.limit(destStart + len);

        dest.put(src);
    }

    public static ByteArray toByteArray(ByteBuffer buf) {
        return new ByteArray(buf);
    }

    public static byte[] toArray(ByteBuffer buf) {
        byte[] array = new byte[buf.remaining()];
        buf.get(array);
        return array;
    }

    public static class ByteArray {
        private final ByteBuffer buf;
        private final int bufPosition;
        private final int bufLimit;
        private final byte[] array;

        private ByteArray(ByteBuffer buf) {
            this.buf = buf;
            this.bufPosition = buf.position();
            this.bufLimit = buf.limit();
            this.array = ByteBufferUtils.toArray(buf);
        }

        public byte[] toArray() {
            return array;
        }

        public void refreshByteBuffer() {
            buf.position(bufPosition);
            buf.limit(bufLimit);
            buf.put(array);
        }
    }
}
