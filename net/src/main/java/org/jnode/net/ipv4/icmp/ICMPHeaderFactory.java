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

package org.jnode.net.ipv4.icmp;

import java.net.SocketException;
import org.jnode.net.SocketBuffer;

public class ICMPHeaderFactory {

    /**
     * Create a type specific ICMP header. The type is read from the first
     * in the skBuf.
     *
     * @param skbuf the skbuf
     * @throws SocketException when an error occurs
     */
    public static ICMPHeader createHeader(SocketBuffer skbuf) throws SocketException {
        final ICMPType type = ICMPType.getType(skbuf.get(0));
        return switch (type) {
            case ICMP_DEST_UNREACH -> new ICMPUnreachableHeader(skbuf);
            case ICMP_TIMESTAMP, ICMP_TIMESTAMPREPLY -> new ICMPTimestampHeader(skbuf);
            case ICMP_ADDRESS, ICMP_ADDRESSREPLY -> new ICMPAddressMaskHeader(skbuf);
            case ICMP_ECHOREPLY, ICMP_ECHO -> new ICMPEchoHeader(skbuf);
            case ICMP_SOURCE_QUENCH, ICMP_REDIRECT, ICMP_TIME_EXCEEDED, ICMP_PARAMETERPROB, ICMP_INFO_REQUEST,
                 ICMP_INFO_REPLY -> throw new SocketException("Not implemented");
        };
    }

}
