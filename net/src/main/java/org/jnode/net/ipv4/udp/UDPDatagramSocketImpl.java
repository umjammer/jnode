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

import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.util.AbstractDatagramSocketImpl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author epr
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class UDPDatagramSocketImpl extends AbstractDatagramSocketImpl implements IPv4Constants,
        UDPConstants {
    /**
     * The UDP protocol we're using
     */
    private final UDPProtocol protocol;

    /**
     * Create a new instance
     * 
     * @param protocol the protocol
     */
    public UDPDatagramSocketImpl(UDPProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void doBind(int lport, InetAddress laddr) throws SocketException {
        protocol.bind(this);
    }

    @Override
    protected void doClose() {
        protocol.unbind(this);
    }

    @Override
    protected byte getTTL() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void onReceive(DatagramPacket p, SocketBuffer skBuf) throws IOException {
        final IPv4Header ipHdr = (IPv4Header) skBuf.getNetworkLayerHeader();
        final UDPHeader udpHdr = (UDPHeader) skBuf.getTransportLayerHeader();
        p.setData(skBuf.toByteArray(), 0, skBuf.getSize());
        p.setAddress(ipHdr.getSource().toInetAddress());
        p.setPort(udpHdr.getSrcPort());
    }

    @Override
    protected void send(DatagramPacket p) throws IOException {

        final IPv4Address dstAddress = new IPv4Address(p.getAddress());
        final IPv4Header ipHdr;
        ipHdr = new IPv4Header(getTos(), getTimeToLive(), IPPROTO_UDP, dstAddress, p.getLength() + UDP_HLEN);
        if (!getLocalAddress().isAnyLocalAddress() || (getDevice() != null)) {
            ipHdr.setSource(new IPv4Address(getLocalAddress()));
        }
        final UDPHeader udpHdr;
        final int srcPort = getLocalPort();
        // final int srcPort = p.getPort(); // or getLocalPort???? TODO Fix
        // srcPort issue
        udpHdr = new UDPHeader(srcPort, p.getPort(), p.getLength());

        final SocketBuffer skBuf = new SocketBuffer(p.getData(), p.getOffset(), p.getLength());
        skBuf.setDevice(getDevice());
        protocol.send(ipHdr, udpHdr, skBuf);
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    @Override
    protected void setTTL(byte ttl) throws IOException {
        // TODO Auto-generated method stub
    }
}
