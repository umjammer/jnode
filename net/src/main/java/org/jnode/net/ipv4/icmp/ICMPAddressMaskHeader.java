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

import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Address;

/**
 * @author epr
 */
public class ICMPAddressMaskHeader extends ICMPExHeader {

    private final IPv4Address subnetMask;

    /**
     * @param type the type
     */
    public ICMPAddressMaskHeader(ICMPType type, int identifier, int seqNumber, IPv4Address subnetMask) {
        super(type, 0, identifier, seqNumber);
        if ((type != ICMPType.ICMP_ADDRESS) && (type != ICMPType.ICMP_ADDRESSREPLY)) {
            throw new IllegalArgumentException("Invalid type " + type);
        }
        this.subnetMask = subnetMask;
    }

    /**
     * @param skbuf the socket buffer
     */
    public ICMPAddressMaskHeader(SocketBuffer skbuf) {
        super(skbuf);
        final ICMPType type = getType();
        if ((type != ICMPType.ICMP_ADDRESS) && (type != ICMPType.ICMP_ADDRESSREPLY)) {
            throw new IllegalArgumentException("Invalid type " + type);
        }
        this.subnetMask = new IPv4Address(skbuf, 8);
    }

    @Override
    protected void doPrefixTo(SocketBuffer skBuf) {
        super.doPrefixTo(skBuf);
        subnetMask.writeTo(skBuf, 8);
    }

    @Override
    public int getLength() {
        return 12;
    }

    /**
     * Gets the subnet mask
     */
    public IPv4Address getSubnetMask() {
        return subnetMask;
    }
}
