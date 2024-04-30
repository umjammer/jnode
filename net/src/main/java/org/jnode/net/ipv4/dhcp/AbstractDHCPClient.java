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

package org.jnode.net.ipv4.dhcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.bootp.AbstractBOOTPClient;
import org.jnode.net.ipv4.bootp.BOOTPHeader;

/**
 * System independent base class.
 * Implementations should override doConfigure.
 *
 * @author markhale
 */
public class AbstractDHCPClient extends AbstractBOOTPClient {

    private static final Logger log = System.getLogger(AbstractDHCPClient.class.getName());

    /**
     * Create a DHCP discovery packet
     */
    protected DatagramPacket createRequestPacket(BOOTPHeader hdr) throws IOException {
        DHCPMessage msg = new DHCPMessage(hdr, DHCPMessage.DHCPDISCOVER);
        return msg.toDatagramPacket();
    }

    protected boolean processResponse(int transactionID, DatagramPacket packet) throws IOException {
        DHCPMessage msg = new DHCPMessage(packet);
        BOOTPHeader hdr = msg.getHeader();
        if (hdr.getOpcode() != BOOTPHeader.BOOTREPLY) {
            // Not a response
            return false;
        }
        if (hdr.getTransactionID() != transactionID) {
            // Not for me
            return false;
        }

        // debug the DHCP message
        if (log.isLoggable(Level.DEBUG)) {
            log.log(Level.DEBUG, "Got Client IP address  : " + hdr.getClientIPAddress());
            log.log(Level.DEBUG, "Got Your IP address    : " + hdr.getYourIPAddress());
            log.log(Level.DEBUG, "Got Server IP address  : " + hdr.getServerIPAddress());
            log.log(Level.DEBUG, "Got Gateway IP address : " + hdr.getGatewayIPAddress());
            for (int n = 1; n < 255; n++) {
                byte[] value = msg.getOption(n);
                if (value != null) {
                    if (value.length == 1) {
                        log.log(Level.DEBUG, "Option " + n + " : " + (int) (value[0]));
                    } else if (value.length == 2) {
                        log.log(Level.DEBUG, "Option " + n + " : " + ((value[0] << 8) | value[1]));
                    } else if (value.length == 4) {
                        log.log(Level.DEBUG, "Option " + n + " : " +
                                InetAddress.getByAddress(value));
                    } else {
                        log.log(Level.DEBUG, "Option " + n + " : " + new String(value));
                    }
                }
            }
        }

        switch (msg.getMessageType()) {
            case DHCPMessage.DHCPOFFER:
                byte[] serverID = msg.getOption(DHCPMessage.SERVER_IDENTIFIER_OPTION);
                byte[] requestedIP = hdr.getYourIPAddress().getAddress();
                hdr = new BOOTPHeader(
                        BOOTPHeader.BOOTREQUEST, transactionID, 0, 
                        hdr.getClientIPAddress(), hdr.getClientHwAddress());
                msg = new DHCPMessage(hdr, DHCPMessage.DHCPREQUEST);
                msg.setOption(DHCPMessage.REQUESTED_IP_ADDRESS_OPTION, requestedIP);
                msg.setOption(DHCPMessage.SERVER_IDENTIFIER_OPTION, serverID);
                packet = msg.toDatagramPacket();
                packet.setAddress(IPv4Address.BROADCAST_ADDRESS);
                packet.setPort(SERVER_PORT);
                socket.send(packet);
                break;
            case DHCPMessage.DHCPACK:
                doConfigure(msg);
                return true;
            case DHCPMessage.DHCPNAK:
                break;
        }
        return false;
    }

    protected void doConfigure(DHCPMessage msg) throws IOException {
        doConfigure(msg.getHeader());
    }
}
