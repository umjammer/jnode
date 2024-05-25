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

package org.jnode.net.ipv4.tcp;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.BindException;
import java.net.DatagramSocketImplFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImplFactory;

import org.jnode.driver.net.NetworkException;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.ipv4.IPv4Protocol;
import org.jnode.net.ipv4.IPv4Service;
import org.jnode.vm.objects.Statistics;

/**
 * @author epr
 */
public class TCPProtocol implements IPv4Protocol, IPv4Constants, TCPConstants {

    /**
     * The IP service I'm a part of
     */
    private final IPv4Service ipService;

//    /** The ICMP service */
//    private final ICMPUtils icmp;

    /**
     * My statistics
     */
    private final TCPStatistics stat = new TCPStatistics();

    /**
     * The SocketImpl factory for TCP
     */
    private final TCPSocketImplFactory socketImplFactory;

    /**
     * My control blocks
     */
    private final TCPControlBlockList controlBlocks;

    /**
     * The timer
     */
    private final TCPTimer timer;

    /**
     * My logger
     */
    private static final Logger log = System.getLogger(TCPProtocol.class.getName());

    /**
     * Initialize a new instance
     *
     * @param ipService the ipService
     */
    public TCPProtocol(IPv4Service ipService) throws NetworkException {
        this.ipService = ipService;
        // this.icmp = new ICMPUtils(ipService);
        this.controlBlocks = new TCPControlBlockList(this);
        this.timer = new TCPTimer(controlBlocks);
        try {
            socketImplFactory = new TCPSocketImplFactory(this);
            try {
                Socket.setSocketImplFactory(socketImplFactory);
                ServerSocket.setSocketFactory(socketImplFactory);
                return;
            } catch (SecurityException ex) {
                log.log(Level.ERROR, "No permission for set socket factory.", ex);
            }
        } catch (IOException ex) {
            throw new NetworkException(ex);
        }
        timer.start();
    }

    @Override
    public DatagramSocketImplFactory getDatagramSocketImplFactory() throws SocketException {
        throw new SocketException("TCP is socket based");
    }

    @Override
    public String getName() {
        return "tcp";
    }

    @Override
    public int getProtocolID() {
        return IPPROTO_TCP;
    }

    @Override
    public SocketImplFactory getSocketImplFactory() throws SocketException {
        return socketImplFactory;
    }

    @Override
    public Statistics getStatistics() {
        return stat;
    }

    @Override
    public void receive(SocketBuffer skBuf) throws SocketException {

        // Increment stats
        stat.ipackets.inc();

        // Get the IP header
        final IPv4Header ipHdr = (IPv4Header) skBuf.getNetworkLayerHeader();

        // Read the TCP header
        final TCPHeader hdr = new TCPHeader(skBuf);

        // Set the TCP header in the buffer-field
        skBuf.setTransportLayerHeader(hdr);
        // Remove the TCP header from the head of the buffer
        skBuf.pull(hdr.getLength());
        // Trim the buffer up to the length in the TCP header
        skBuf.trim(hdr.getDataLength());

        if (!hdr.isChecksumOk()) {
            log.log(Level.DEBUG, () -> "Receive: badsum: " + hdr);
            stat.badsum.inc();
        } else {
            log.log(Level.DEBUG, () -> "Receive: " + hdr);

            // Find the corresponding control block
            final TCPControlBlock cb =
                (TCPControlBlock) controlBlocks.lookup(ipHdr.getSource(), hdr.getSrcPort(),
                    ipHdr.getDestination(), hdr.getDstPort(), true);
            if (cb == null) {
                final boolean ack = hdr.isFlagAcknowledgeSet();
                final boolean rst = hdr.isFlagResetSet();

                stat.noport.inc();

                // Port unreachable
                if (ack && rst) {
                    // the source is also unreachable
                    log.log(Level.DEBUG, "Dropping segment due to: connection refused as the source is also unreachable");
                } else {
                    processPortUnreachable(ipHdr, hdr);
                }
            } else {
                // Let the cb handle the reception
                cb.receive(hdr, skBuf);
            }
        }
    }

    @Override
    public void receiveError(SocketBuffer skBuf) throws SocketException {
        // TODO Auto-generated method stub

    }

    /**
     * Process a segment whose destination port is unreachable
     *
     * @param hdr the hdr
     */
    private void processPortUnreachable(IPv4Header ipHdr, TCPHeader hdr) throws SocketException {
        final TCPHeader replyHdr =
            new TCPHeader(hdr.getDstPort(), hdr.getSrcPort(), 0, 0, hdr.getSequenceNr() + 1, 0,
                0);
        replyHdr.setFlags(TCPF_ACK | TCPF_RST);
        final IPv4Header replyIpHdr = new IPv4Header(ipHdr);
        replyIpHdr.swapAddresses();
        send(replyIpHdr, replyHdr, new SocketBuffer());
    }

    /**
     * Create a binding for a local address
     *
     * @param lAddr the local address
     * @param lPort the locl port
     */
    public TCPControlBlock bind(IPv4Address lAddr, int lPort) throws BindException {
        return (TCPControlBlock) controlBlocks.bind(lAddr, lPort);
    }

    /**
     * Send an TCP packet
     *
     * @param skBuf the skBuf
     */
    protected void send(IPv4Header ipHdr, TCPHeader tcpHdr, SocketBuffer skBuf) throws SocketException {
        log.log(Level.DEBUG, () -> "send(ipHdr, " + tcpHdr + ')');
        skBuf.setTransportLayerHeader(tcpHdr);
        tcpHdr.prefixTo(skBuf);
        ipHdr.setDataLength(skBuf.getSize());
        ipService.transmit(ipHdr, skBuf);
        stat.opackets.inc();
    }

    /**
     * Get the current time counter
     */
    protected long getTimeCounter() {
        return timer.getCounter();
    }
}
