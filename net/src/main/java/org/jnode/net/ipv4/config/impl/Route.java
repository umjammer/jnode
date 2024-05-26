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

package org.jnode.net.ipv4.config.impl;

import java.io.IOException;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Route;
import org.jnode.net.ipv4.IPv4RoutingTable;
import org.jnode.net.ipv4.layer.IPv4NetworkLayer;
import org.jnode.net.util.NetUtils;

/**
 * @author epr
 */
final class Route {

    /**
     * Add a route
     * 
     * @param target the target
     * @param gateway the gateway
     * @param device the device
     * @throws NetworkException when an error occurs
     */
    public static void addRoute(IPv4Address target, IPv4Address gateway, Device device)
        throws NetworkException {

        if (device == null) {
            // TODO
        }

        final IPv4NetworkLayer ipNL;
        try {
            ipNL = (IPv4NetworkLayer) NetUtils.getNLM().getNetworkLayer(EthernetConstants.ETH_P_IP);
        } catch (IOException ex) {
            throw new NetworkException("Cannot find IPv4 network layer", ex);
        }
        final IPv4RoutingTable rt = ipNL.getRoutingTable();
        rt.add(new IPv4Route(target, null, gateway, device));
    }

    /**
     * Delete a route
     * 
     * @param target the target
     * @param gateway the gateway
     * @param device the device
     * @throws NetworkException when an error occurs
     */
    public static void delRoute(IPv4Address target, IPv4Address gateway, Device device)
        throws NetworkException {
        final IPv4NetworkLayer ipNL;
        try {
            ipNL = (IPv4NetworkLayer) NetUtils.getNLM().getNetworkLayer(EthernetConstants.ETH_P_IP);
        } catch (IOException ex) {
            throw new NetworkException("Cannot find IPv4 network layer", ex);
        }
        final IPv4RoutingTable rt = ipNL.getRoutingTable();

        for (IPv4Route route : rt.entries()) {
            if (!route.getDestination().equals(target)) {
                continue;
            }
            if (gateway != null) {
                if (!gateway.equals(route.getGateway())) {
                    continue;
                }
            }
            if (device != null) {
                if (device != route.getDevice()) {
                    continue;
                }
            }

            rt.remove(route);
            return;
        }
    }
}
