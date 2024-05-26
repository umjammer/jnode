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

package org.jnode.net;

import java.net.DatagramSocketImplFactory;
import java.net.SocketException;
import java.net.SocketImplFactory;

import org.jnode.vm.objects.Statistics;

/**
 * OSI transport layers must implement this interface.
 * 
 * @author epr
 */
public interface TransportLayer {

    /**
     * Gets the name of this type
     */
    String getName();

    /**
     * Gets the protocol ID this layer handles
     */
    int getProtocolID();

    /**
     * Gets the statistics of this protocol
     */
    Statistics getStatistics();

    /**
     * Process a packet that has been received and matches getType()
     * @param skBuf the socket buffer
     * @throws SocketException when an error occurs
     */
    void receive(SocketBuffer skBuf) throws SocketException;

    /**
     * Gets the SocketImplFactory of this protocol.
     * @throws SocketException If this protocol is not Socket based.
     */
    SocketImplFactory getSocketImplFactory() throws SocketException;

    /**
     * Gets the DatagramSocketImplFactory of this protocol.
     * @throws SocketException If this protocol is not DatagramSocket based.
     */
    DatagramSocketImplFactory getDatagramSocketImplFactory() throws SocketException;
}
