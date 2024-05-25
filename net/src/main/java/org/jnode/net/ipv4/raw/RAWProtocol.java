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

package org.jnode.net.ipv4.raw;

import java.net.DatagramSocketImplFactory;
import java.net.SocketException;
import java.net.SocketImplFactory;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4Protocol;
import org.jnode.net.ipv4.IPv4Service;
import org.jnode.vm.objects.Statistics;

/**
 * @author epr
 */
public class RAWProtocol implements IPv4Protocol, IPv4Constants {

    /** My logger */
    private static final Logger log = System.getLogger(RAWProtocol.class.getName());

//    /** The service i'm a part of */
//    private final IPv4Service ipService;

    /** My statistics */
    private final RAWStatistics stat = new RAWStatistics();

    /**
     * Create a new instance
     * 
     * @param ipService the ipService
     */
    public RAWProtocol(IPv4Service ipService) {
        // this.ipService = ipService;
    }

    @Override
    public String getName() {
        return "raw";
    }

    @Override
    public int getProtocolID() {
        return IPPROTO_RAW;
    }

    @Override
    public void receive(SocketBuffer skBuf) throws SocketException {
        log.log(Level.DEBUG, "Received RAW IP packet");
        // TODO Implement RAW protocol reception
    }

    /**
     * Process an ICMP error message that has been received and matches this
     * protocol. The skBuf is position directly after the ICMP header (thus
     * contains the error IP header and error transport layer header). The
     * transportLayerHeader property of skBuf is set to the ICMP message header.
     * 
     * @param skBuf the socket buffer
     * @throws SocketException when an error occurs
     */
    @Override
    public void receiveError(SocketBuffer skBuf) throws SocketException {
        // Ignore errors here
    }

    /**
     * Gets the SocketImplFactory of this protocol.
     * 
     * @throws SocketException If this protocol is not Socket based.
     */
    @Override
    public SocketImplFactory getSocketImplFactory() throws SocketException {
        throw new SocketException("RAW is packet based");
    }

    /**
     * Gets the DatagramSocketImplFactory of this protocol.
     * 
     * @throws SocketException If this protocol is not DatagramSocket based.
     */
    @Override
    public DatagramSocketImplFactory getDatagramSocketImplFactory() throws SocketException {
        throw new SocketException("Not implemented yet");
    }

    @Override
    public Statistics getStatistics() {
        return stat;
    }
}
