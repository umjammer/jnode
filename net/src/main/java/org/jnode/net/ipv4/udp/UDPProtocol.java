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

package org.jnode.net.ipv4.udp;

import java.io.IOException;
import java.lang.System.Logger;
import java.net.DatagramSocket;
import java.net.DatagramSocketImplFactory;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketImplFactory;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

import org.jnode.driver.net.NetworkException;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.ipv4.IPv4Protocol;
import org.jnode.net.ipv4.IPv4Service;
import org.jnode.net.ipv4.icmp.ICMPUtils;
import org.jnode.vm.objects.Statistics;

/**
 * @author epr
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class UDPProtocol implements IPv4Protocol, IPv4Constants {

    /**
     * My logger
     */
    private static final Logger log = System.getLogger(UDPProtocol.class.getName());

    /**
     * The underlying IP service
     */
    private final IPv4Service ipService;

    /**
     * Socket bindings (lport, socket)
     */
    private final HashMap<Integer, UDPDatagramSocketImpl> sockets =
            new HashMap<>();

    /**
     * DatagramSocketImplFactor instance
     */
    private final UDPDatagramSocketImplFactory dsiFactory;

    /**
     * My statistics
     */
    private final UDPStatistics stat = new UDPStatistics();

    /**
     * ICMP utility
     */
    private final ICMPUtils icmp;

    /**
     * for random listener ports
     */
    private static final Integer zero = 0;
    private final Random random = new SecureRandom();

    private static final int startRandom = 1024;
    private static final int stopRandom = (65535 - startRandom);

    /**
     * Create a new instance
     * 
     * @param ipService the ipService
     */
    public UDPProtocol(IPv4Service ipService) throws NetworkException {
        this.ipService = ipService;
        this.icmp = new ICMPUtils(ipService);
        try {
            dsiFactory = new UDPDatagramSocketImplFactory(this);
            DatagramSocket.setDatagramSocketImplFactory(dsiFactory);
        } catch (IOException ex) {
            throw new NetworkException(ex);
        }
    }

    @Override
    public String getName() {
        return "udp";
    }

    @Override
    public int getProtocolID() {
        return IPPROTO_UDP;
    }

    @Override
    public void receive(SocketBuffer skBuf) throws SocketException {

        stat.ipackets.inc();

        final UDPHeader hdr = new UDPHeader(skBuf);
        if (!hdr.isChecksumOk()) {
            stat.badsum.inc();
            return;
        }

        // Set the UDP header in the buffer-field
        skBuf.setTransportLayerHeader(hdr);
        // Remove the UDP header from the head of the buffer
        skBuf.pull(hdr.getLength());
        // Trim the buffer up to the length in the UDP header
        skBuf.trim(hdr.getDataLength());

        // Test the length of the buffer to the data length in the header.
        if (skBuf.getSize() < hdr.getDataLength()) {
            stat.badlen.inc();
            return;
        }

        // Syslog.log(Level.DEBUG, "Found UDP: " + hdr);

        deliver(hdr, skBuf);
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
        // TODO handle ICMP errors in UDP
    }

    /**
     * Gets the SocketImplFactory of this protocol.
     * 
     * @throws SocketException If this protocol is not Socket based.
     */
    @Override
    public SocketImplFactory getSocketImplFactory() throws SocketException {
        throw new SocketException("UDP is packet based");
    }

    /**
     * Gets the DatagramSocketImplFactory of this protocol.
     */
    @Override
    public DatagramSocketImplFactory getDatagramSocketImplFactory() {
        return dsiFactory;
    }

    /**
     * Deliver a given packet to all interested sockets.
     * 
     * @param hdr the socket buffer
     * @param skBuf the socket buffer
     */
    private synchronized void deliver(UDPHeader hdr, SocketBuffer skBuf) throws SocketException {
        final Integer lport = hdr.getDstPort();
        final IPv4Header ipHdr = (IPv4Header) skBuf.getNetworkLayerHeader();
        final UDPDatagramSocketImpl socket = sockets.get(lport);
        if (socket != null) {
            final InetAddress laddr = socket.getLocalAddress();
            if (laddr.isAnyLocalAddress() || laddr.equals(ipHdr.getDestination().toInetAddress())) {
                if (socket.deliverReceived(skBuf)) {
                    return;
                }
            }
        }
        stat.noport.inc();
        if (ipHdr.getDestination().isBroadcast()) {
            stat.noportbcast.inc();
        }
        // Send a port unreachable back
        icmp.sendPortUnreachable(skBuf);
    }

    /**
     * Register a datagram socket
     * 
     * @param socket the socket
     */
    protected synchronized void bind(UDPDatagramSocketImpl socket) throws SocketException {
        Integer lport = socket.getLocalPort();

        if (lport.compareTo(zero) != 0 && sockets.containsKey(lport)) {
            throw new SocketException("Port already bound (" + lport + ')');
        } else {
            int ran;

            while (lport.compareTo(zero) == 0) {
                ran = random.nextInt(stopRandom) + startRandom;

                if (!sockets.containsKey(ran)) {
                    // Should we have one stop condition more??
                    lport = ran;
                    socket.setLocalPort(lport);
                }
            }

            sockets.put(lport, socket);
        }
    }

    /**
     * Unregister a datagram socket
     * 
     * @param socket the socket
     */
    protected synchronized void unbind(UDPDatagramSocketImpl socket) {
        final Integer lport = socket.getLocalPort();
        if (sockets.get(lport) == socket) {
            sockets.remove(lport);
        }
    }

    /**
     * Send an UDP packet
     *
     * @param skBuf the socket buffer
     */
    protected void send(IPv4Header ipHdr, UDPHeader udpHdr, SocketBuffer skBuf)
        throws SocketException {
        skBuf.setTransportLayerHeader(udpHdr);
        udpHdr.prefixTo(skBuf);
        ipService.transmit(ipHdr, skBuf);
        stat.opackets.inc();
    }

    @Override
    public Statistics getStatistics() {
        return stat;
    }
}
