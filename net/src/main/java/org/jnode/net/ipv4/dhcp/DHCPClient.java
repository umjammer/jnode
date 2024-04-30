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
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.NetPermission;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.bootp.BOOTPHeader;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.jnode.net.ipv4.util.ResolverImpl;

/**
 * Console DHCP client.
 *
 * @author markhale
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class DHCPClient extends AbstractDHCPClient {

    private static final Logger log = System.getLogger(DHCPClient.class.getName());

    private Device device;
    private NetDeviceAPI api;

    /**
     * Configure the given device using BOOTP
     *
     * @param device
     */
    public final void configureDevice(final Device device) throws IOException {
        this.device = device;

        // Get the API.
        try {
            api = device.getAPI(NetDeviceAPI.class);
        } catch (ApiNotFoundException ex) {
            throw new NetworkException("Device is not a network device", ex);
        }
        configureDevice(device.getId(), api.getAddress());
    }

    /**
     * Performs the actual configuration of a network device based on the
     * settings in a DHCP message.
     */
    protected void doConfigure(DHCPMessage msg) throws IOException {
        super.doConfigure(msg);

        final IPv4ConfigurationService cfg;
        try {
            ServiceLoader<IPv4ConfigurationService> sl = ServiceLoader.load(IPv4ConfigurationService.NAME);
            cfg = sl.iterator().next();
        } catch (NoSuchElementException ex) {
            throw new NetworkException(ex);
        }
        BOOTPHeader hdr = msg.getHeader();
        cfg.configureDeviceStatic(device, new IPv4Address(hdr
                .getYourIPAddress()), null, false);

        final IPv4Address serverAddr = new IPv4Address(hdr.getServerIPAddress());
        final IPv4Address networkAddress = serverAddr.and(serverAddr.getDefaultSubnetmask());

        if (hdr.getGatewayIPAddress().isAnyLocalAddress()) {
            cfg.addRoute(serverAddr, null, device, false);
            cfg.addRoute(networkAddress, null, device, false);
        } else {
            cfg.addRoute(networkAddress, new IPv4Address(hdr.getGatewayIPAddress()), device, false);
        }

        byte[] routerValue = msg.getOption(DHCPMessage.ROUTER_OPTION);
        if (routerValue != null && routerValue.length >= 4) {
            IPv4Address routerIP = new IPv4Address(routerValue, 0);
            log.log(Level.INFO, "Got Router IP address : " + routerIP);
            cfg.addRoute(IPv4Address.ANY, routerIP, device, false);
        }

        // find the dns servers and add to the resolver
        final byte[] dnsValue = msg.getOption(DHCPMessage.DNS_OPTION);
        if (dnsValue != null) {
            for (int i = 0; i < dnsValue.length; i += 4) {
                final IPv4Address dnsIP = new IPv4Address(dnsValue, i);

                log.log(Level.INFO, "Got Dns IP address    : " + dnsIP);
                try {
                    ResolverImpl.addDnsServer(dnsIP);
                } catch (Throwable ex) {
                    log.log(Level.ERROR, "Failed to configure DNS server");
                    log.log(Level.DEBUG, "Failed to configure DNS server", ex);
                }
            }
        }

        // Find the plugin loader option
        final byte[] pluginLoaderValue = msg.getOption(DHCPMessage.PLUGIN_LOADER_OPTION);
        if (pluginLoaderValue != null) {
            final String pluginLoaderURL = new String(pluginLoaderValue, StandardCharsets.UTF_8);
            log.log(Level.INFO, "Got plugin loader url : " + pluginLoaderURL);
            try {
                // TODO
            } catch (Throwable ex) {
                log.log(Level.ERROR, "Failed to configure plugin loader");
                log.log(Level.DEBUG, "Failed to configure plugin loader", ex);
            }
        }
    }
}
