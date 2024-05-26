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

package org.jnode.net.ipv4.bootp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.net.HardwareAddress;
import org.jnode.net.ipv4.IPv4Address;

/**
 * System independent base class.
 * Implementations should override doConfigure.
 *
 * @author markhale
 */
public class AbstractBOOTPClient {

    /**
     * My logger
     */
    private static final Logger log = System.getLogger(AbstractBOOTPClient.class.getName());

    private static final int RECEIVE_TIMEOUT = 10 * 1000; // 10 seconds
    public static final int SERVER_PORT = 67;
    public static final int CLIENT_PORT = 68;

    protected MulticastSocket socket;

    /**
     * Configure the given device using BOOTP
     *
     * @param deviceName network interface
     */
    protected final void configureDevice(final String deviceName, final HardwareAddress hwAddress)
        throws IOException {
        // Open a socket
        socket = new MulticastSocket(CLIENT_PORT);
        try {
            // Prepare the socket
            socket.setBroadcast(true);
            socket.setNetworkInterface(NetworkInterface.getByName(deviceName));
            socket.setSoTimeout(RECEIVE_TIMEOUT);

            // Create the BOOTP header
            final Inet4Address myIP = null; // any address
            final int transactionID = (int) (System.currentTimeMillis() & 0xFFFF_FFFFL);
            BOOTPHeader hdr =
                    new BOOTPHeader(BOOTPHeader.BOOTREQUEST, transactionID, 0, myIP, hwAddress);

            // Send the packet
            final DatagramPacket packet = createRequestPacket(hdr);
            packet.setAddress(IPv4Address.BROADCAST_ADDRESS);
            packet.setPort(SERVER_PORT);
            socket.send(packet);

            boolean configured;
            do {
                // Wait for a response
                socket.receive(packet);

                // Process the response
                configured = processResponse(transactionID, packet);
            } while (!configured);

        } finally {
            socket.close();
        }
        socket = null;
    }

    /**
     * Create a BOOTP request packet
     */
    protected DatagramPacket createRequestPacket(BOOTPHeader hdr) throws IOException {
        return new BOOTPMessage(hdr).toDatagramPacket();
    }

    /**
     * Process a BOOTP response
     *
     * @param packet the packet
     * @return true if the device has been configured, false otherwise
     */
    protected boolean processResponse(int transactionID, DatagramPacket packet) throws IOException {
        final BOOTPHeader hdr = new BOOTPHeader(packet);
        if (hdr.getOpcode() != BOOTPHeader.BOOTREPLY) {
            // Not a response
            return false;
        }
        if (hdr.getTransactionID() != transactionID) {
            // Not for me
            return false;
        }

        doConfigure(hdr);

        return true;
    }

    /**
     * Performs the actual configuration of a network device based on the
     * settings in a BOOTP header.
     */
    protected void doConfigure(BOOTPHeader hdr) throws IOException {
        log.log(Level.INFO, "Got Client IP address  : " + hdr.getClientIPAddress());
        log.log(Level.INFO, "Got Your IP address    : " + hdr.getYourIPAddress());
        log.log(Level.INFO, "Got Server IP address  : " + hdr.getServerIPAddress());
        log.log(Level.INFO, "Got Gateway IP address : " + hdr.getGatewayIPAddress());
    }
}
