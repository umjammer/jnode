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

package org.jnode.net.ipv4.layer;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.HardwareAddress;
import org.jnode.net.InvalidLayerException;
import org.jnode.net.LayerAlreadyRegisteredException;
import org.jnode.net.NetworkLayer;
import org.jnode.net.NoSuchProtocolException;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.TransportLayer;
import org.jnode.net.arp.ARPNetworkLayer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4FragmentList;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.ipv4.IPv4Protocol;
import org.jnode.net.ipv4.IPv4ProtocolAddressInfo;
import org.jnode.net.ipv4.IPv4RoutingTable;
import org.jnode.net.ipv4.IPv4Service;
import org.jnode.net.ipv4.icmp.ICMPProtocol;
import org.jnode.net.ipv4.raw.RAWProtocol;
import org.jnode.net.ipv4.tcp.TCPProtocol;
import org.jnode.net.ipv4.udp.UDPProtocol;
import org.jnode.net.ipv4.util.ResolverImpl;
import org.jnode.net.util.NetUtils;
import org.jnode.util.NumberUtils;
import org.jnode.vm.objects.Statistics;

/**
 * @author epr
 */
public class IPv4NetworkLayer implements NetworkLayer, IPv4Constants, IPv4Service {

    /**
     * My logger
     */
    private static final Logger log = System.getLogger(IPv4NetworkLayer.class.getName());

    private final HashMap<Integer, IPv4Protocol> protocols = new HashMap<>();

    /**
     * List of in-complete fragments
     */
    private final HashMap<Object, IPv4FragmentList> fragments = new HashMap<>();

    /**
     * System time of last call to removeDeadFragments
     */
    private long lastFragmentCleanup = 0;

    /**
     * My statistics
     */
    private final IPv4Statistics stat = new IPv4Statistics();

    /**
     * The routing table
     */
    private final IPv4RoutingTable rt = new IPv4RoutingTable();

    /**
     * The sender
     */
    private final IPv4Sender sender;

    /**
     * The ARP network layer
     */
    private ARPNetworkLayer arp;

    /**
     * Initialize a new instance
     */
    public IPv4NetworkLayer() throws NetworkException {
        sender = new IPv4Sender(this);
        registerProtocol(new ICMPProtocol(this));
        registerProtocol(new TCPProtocol(this));
        registerProtocol(new UDPProtocol(this));
        registerProtocol(new RAWProtocol(this));
    }

    /**
     * Gets the name of this type
     */
    @Override
    public String getName() {
        return "ipv4";
    }

    /**
     * Gets the protocol ID this packet-type handles
     */
    @Override
    public int getProtocolID() {
        return EthernetConstants.ETH_P_IP;
    }

    /**
     * Can this packet type process packets received from the given device?
     */
    @Override
    public boolean isAllowedForDevice(Device dev) {
        // For all devices
        return true;
    }

    /**
     * Process a packet that has been received and matches getType()
     * 
     * @param skbuf the socket buffer
     * @param deviceAPI the device api
     * @throws SocketException when network error occurs
     */
    @Override
    public void receive(SocketBuffer skbuf, NetDeviceAPI deviceAPI) throws SocketException {

        // Update statistics
        stat.ipackets.inc();

        // Get IP header
        final IPv4Header hdr = new IPv4Header(skbuf);
        if (!hdr.isChecksumOk()) {
            stat.badsum.inc();
            return;
        }
        // Set the header object in the buffer-field
        skbuf.setNetworkLayerHeader(hdr);

        // Remove header from skBuf-data
        skbuf.pull(hdr.getLength());
        // Trim the end of the message, to we have a valid length
        skbuf.trim(hdr.getDataLength());

        // Now test if the size of the buffer equals the datalength in the
        // header, if now ignore the packet
        if (skbuf.getSize() < hdr.getDataLength()) {
            stat.badlen.inc();
            return;
        }

        // Update the ARP cache for the source address
        updateARPCache(skbuf.getLinkLayerHeader().getSourceAddress(), hdr.getSourceAddress());

        // Get my IP address
        final IPv4ProtocolAddressInfo myAddrInfo =
                (IPv4ProtocolAddressInfo) deviceAPI.getProtocolAddressInfo(getProtocolID());
        if (myAddrInfo == null) {
            stat.nodevaddr.inc();
        }

        // Should I process this packet, or is it for somebody else?
        final IPv4Address dstAddr = hdr.getDestination();
        final boolean shouldProcess;
        if (myAddrInfo != null) {
            shouldProcess = myAddrInfo.contains(dstAddr);
        } else {
            // I don't have an IP address yet, if the linklayer says
            // it is for me, we'll process it, otherwise we'll drop it.
            shouldProcess = !skbuf.getLinkLayerHeader().getDestinationAddress().isBroadcast();
        }
        if (!shouldProcess) {
            // log.log(Level.DEBUG, "IPPacket not for me, ignoring (dst=" + dstAddr + ")");
            return;
        }

        // Is it a fragment?
        if (hdr.isFragment()) {
            // Yes it is a fragment
            stat.fragments.inc();
            deliverFragment(hdr, skbuf);
        } else {
            // It is a complete packet, find the protocol handler
            // and let it do the rest
            deliver(hdr, skbuf);
        }

        // Do a cleanup of the fragmentlist from time to time
        final long now = System.currentTimeMillis();
        if ((now - lastFragmentCleanup) >= (IP_FRAGTIMEOUT * 2)) {
            removeDeadFragments();
        }
    }

    /**
     * Gets the routing table
     */
    @Override
    public IPv4RoutingTable getRoutingTable() {
        return rt;
    }

    /**
     * Deliver a packet to the corresponding protocol
     * 
     * @param hdr the ipv4 header
     * @param skBuf the socket buffer
     */
    private void deliver(IPv4Header hdr, SocketBuffer skBuf) throws SocketException {
        final IPv4Protocol protocol;
        try {
            protocol = getProtocol(hdr.getProtocol());
            protocol.receive(skBuf);
        } catch (NoSuchProtocolException ex) {
            log.log(Level.DEBUG, "Found unknown IP src=" + hdr.getSource() + ", dst=" + hdr.getDestination() +
                    ", prot=0x" + NumberUtils.hex(hdr.getProtocol(), 2));
        }
    }

    /**
     * Process the delivery of a fragment
     * 
     * @param hdr the ipv4 header
     * @param skbuf the socket buffer
     * @throws NetworkException when network error occurs
     */
    private void deliverFragment(IPv4Header hdr, SocketBuffer skbuf) throws SocketException {
        final Object key = hdr.getFragmentListKey();
        final IPv4FragmentList flist = fragments.get(key);
        if (flist == null) {
            // This is a fragment for a new list
            fragments.put(key, new IPv4FragmentList(skbuf));
        } else {
            if (flist.isAlive()) {
                flist.add(skbuf);
                if (flist.isComplete()) {
                    // The fragmentlist is now complete, deliver it
                    final SocketBuffer pbuf = flist.getPacket();
                    final IPv4Header phdr = (IPv4Header) pbuf.getNetworkLayerHeader();
                    stat.reassembled.inc();
                    deliver(phdr, pbuf);
                } 
            } else {
                // Timeout of fragmentlist, destroy it
                fragments.remove(key);
            }
        }
    }

    /**
     * Remove all dead fragments from the fragment list
     */
    private void removeDeadFragments() {
        final ArrayList<Object> deadFragmentKeys = new ArrayList<>();
        // First collect all dead fragment keys
        // Do not remove the directly, since that will create an error
        // in the iterator.
        for (IPv4FragmentList f : fragments.values()) {
            if (!f.isAlive()) {
                deadFragmentKeys.add(f.getKey());
            }
        }
        if (!deadFragmentKeys.isEmpty()) {
            // Now remove all dead fragments
            for (Object key : deadFragmentKeys) {
                fragments.remove(key);
            }
            // We're done
            log.log(Level.DEBUG, "Removed " + deadFragmentKeys.size() + " dead fragments");
        }
        // Update our last invocation timestamp
        lastFragmentCleanup = System.currentTimeMillis();
    }

    /**
     * Gets the protocol for a given ID
     * 
     * @param protocolID the protocol id
     * @throws NoSuchProtocolException No protocol with the given ID was found.
     */
    @Override
    public IPv4Protocol getProtocol(int protocolID) throws NoSuchProtocolException {
        final IPv4Protocol protocol;
        protocol = protocols.get(protocolID);
        if (protocol == null) {
            throw new NoSuchProtocolException("with ID " + protocolID);
        }
        return protocol;
    }

    /**
     * Register a protocol
     * 
     * @param protocol the protocol
     */
    protected void registerProtocol(IPv4Protocol protocol) {
        protocols.put(protocol.getProtocolID(), protocol);
    }

    /**
     * Unregister a protocol
     * 
     * @param protocol the protocol
     */
    protected void unregisterProtocol(IPv4Protocol protocol) {
        protocols.remove(protocol.getProtocolID());
    }

    /**
     * Register a transport-layer as possible destination of packets received by
     * this network-layer
     * 
     * @param layer the transport layer
     */
    @Override
    public void registerTransportLayer(TransportLayer layer)
        throws LayerAlreadyRegisteredException, InvalidLayerException {
        if (layer instanceof IPv4Protocol) {
            registerProtocol((IPv4Protocol) layer);
        } else {
            throw new InvalidLayerException("No IPv4Protocol");
        }
    }

    /**
     * Unregister a transport-layer
     * 
     * @param layer the transport layer
     */
    @Override
    public void unregisterTransportLayer(TransportLayer layer) {
        if (layer instanceof IPv4Protocol) {
            unregisterProtocol((IPv4Protocol) layer);
        }
    }

    /**
     * Gets all registered transport-layers
     */
    @Override
    public Collection<TransportLayer> getTransportLayers() {
        return new ArrayList<>(protocols.values());
    }

    /**
     * Gets a registered transport-layer by its protocol ID.
     * 
     * @param protocolID the protocol id
     */
    @Override
    public TransportLayer getTransportLayer(int protocolID) throws NoSuchProtocolException {
        return getProtocol(protocolID);
    }

    @Override
    public Statistics getStatistics() {
        return stat;
    }

    @Override
    public void transmit(IPv4Header hdr, SocketBuffer skBuf) throws SocketException {
        sender.transmit(hdr, skBuf);
    }

    /**
     * Gets the protocol addresses for a given name, or null if not found.
     * 
     * @param hostname the host name
     * @return the addresses or {@code null}
     */
    @Override
    public ProtocolAddress[] getHostByName(String hostname) {
        try {
            return ResolverImpl.getInstance().getByName(hostname);
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    private void updateARPCache(HardwareAddress hwAddr, ProtocolAddress pAddr) {
        if (arp == null) {
            try {
                arp = (ARPNetworkLayer) NetUtils.getNLM().getNetworkLayer(EthernetConstants.ETH_P_ARP);
            } catch (NoSuchProtocolException ex) {
                log.log(Level.ERROR, "Cannot find ARP layer", ex);
            } catch (IOException ex) {
                log.log(Level.ERROR, "Cannot network layer manager", ex);
            }
        }
        if (arp != null) {
            arp.getCache().set(hwAddr, pAddr, true);
        }
    }
}
