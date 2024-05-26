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

import java.net.DatagramSocketImplFactory;
import java.net.SocketException;
import java.net.SocketImplFactory;
import java.util.Vector;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.ipv4.IPv4Protocol;
import org.jnode.net.ipv4.IPv4Service;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;
import org.jnode.vm.objects.Statistics;

/**
 * Protocol handler of the ICMP protocol.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ICMPProtocol implements IPv4Protocol, IPv4Constants, ICMPConstants,
    QueueProcessor<SocketBuffer> {

    private static final String IPNAME_ICMP = "icmp";

    /**
     * My logger
     */
    private static final Logger log = System.getLogger(ICMPProtocol.class.getName());

    /**
     * The IP service we're a part of
     */
    private final IPv4Service ipService;

    /**
     * The statistics
     */
    private final ICMPStatistics stat = new ICMPStatistics();

    /**
     * Queue<SocketBuffer> for requests that need a reply
     */
    private final Queue<SocketBuffer> replyRequestQueue = new Queue<>();

    private final QueueProcessorThread<SocketBuffer> replyRequestsThread;

    /**
     * ICMP packet listeners
     */
    private final Vector<ICMPListener> listeners = new Vector<>();

    /**
     * Create a new instance
     *
     * @param ipService the ipService
     */
    public ICMPProtocol(IPv4Service ipService) {
        this.ipService = ipService;
        this.replyRequestsThread =
                new QueueProcessorThread<>("icmp-reply", replyRequestQueue, this);
        replyRequestsThread.start();
    }

    @Override
    public String getName() {
        return IPNAME_ICMP;
    }

    @Override
    public int getProtocolID() {
        return IPPROTO_ICMP;
    }

    @Override
    public void receive(SocketBuffer skBuf) throws SocketException {

        // Update statistics
        stat.ipackets.inc();

        try {
            final ICMPHeader hdr = ICMPHeaderFactory.createHeader(skBuf);
            skBuf.setTransportLayerHeader(hdr);
            skBuf.pull(hdr.getLength());

            if (!hdr.isChecksumOk()) {
                stat.badsum.inc();
                return;
            }

            // TODO Process ICMP messages

            switch (hdr.getType()) {
                case ICMP_ECHO:
                    postReplyRequest(skBuf);
                    break;
                case ICMP_ECHOREPLY:
                    notifyListeners(skBuf);
                    break;
                default:
                    log.log(Level.DEBUG, "GOT ICMP type " + hdr.getType() + ", code " + hdr.getCode());
            }
        } catch (SocketException ex) {
            // TODO fix me
            // Ignore for now
        }
    }

    /**
     * Process an ICMP error message that has been received and matches this
     * protocol. The skBuf is position directly after the ICMP header (thus
     * contains the error IP header and error transport layer header). The
     * transportLayerHeader property of skBuf is set to the ICMP message header.
     *
     * @param skbuf the skbuf
     * @throws SocketException when an error occurs
     */
    @Override
    public void receiveError(SocketBuffer skbuf) throws SocketException {
        // Ignore errors here
    }

    /**
     * Gets the SocketImplFactory of this protocol.
     *
     * @throws SocketException If this protocol is not Socket based.
     */
    @Override
    public SocketImplFactory getSocketImplFactory() throws SocketException {
        throw new SocketException("ICMP is packet based");
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

    /**
     * Send an ICMP packet
     *
     * @param skbuf the skbuf
     */
    protected void send(IPv4Header ipHdr, ICMPHeader icmpHdr, SocketBuffer skbuf)
        throws SocketException {
        stat.opackets.inc();
        skbuf.setTransportLayerHeader(icmpHdr);
        icmpHdr.prefixTo(skbuf);
        ipService.transmit(ipHdr, skbuf);
    }

    /**
     * Send a reply on an ICMP echo header.
     *
     * @param hdr the hdr
     * @param skbuf the skbuf
     */
    private void sendEchoReply(ICMPEchoHeader hdr, SocketBuffer skbuf) throws SocketException {
        final IPv4Header ipHdr = (IPv4Header) skbuf.getNetworkLayerHeader();
        final IPv4Header ipReplyHdr = new IPv4Header(ipHdr);
        ipReplyHdr.swapAddresses();
        ipReplyHdr.setTtl(0xFF);
        send(ipReplyHdr, hdr.createReplyHeader(), new SocketBuffer(skbuf));
    }

    /**
     * @see org.jnode.net.ipv4.IPv4Protocol#getStatistics()
     */
    @Override
    public Statistics getStatistics() {
        return stat;
    }

    /**
     * Post a request that needs a reply in the reply queue.
     *
     * @param skBuf the socket buffer
     */
    private void postReplyRequest(SocketBuffer skBuf) {
        replyRequestQueue.add(skBuf);
    }

    /**
     * Process a request that needs a reply
     *
     * @param skBuf the socket buffer
     */
    private void processReplyRequest(SocketBuffer skBuf) {
        final ICMPHeader hdr = (ICMPHeader) skBuf.getTransportLayerHeader();
        try {
            if (hdr.getType() == ICMPType.ICMP_ECHO) {
                sendEchoReply((ICMPEchoHeader) hdr, skBuf);
            }
        } catch (SocketException ex) {
            log.log(Level.ERROR, "Error in ICMP reply", ex);
        }
    }

    @Override
    public void process(SocketBuffer object) throws Exception {
        processReplyRequest(object);
    }

    /**
     * ICMP packet listeners methods
     */
    private void notifyListeners(SocketBuffer skbuf) {
        for (ICMPListener l : listeners) {
            l.packetReceived(skbuf);
        }
    }

    public void addListener(ICMPListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ICMPListener listener) {
        listeners.remove(listener);
    }
}
