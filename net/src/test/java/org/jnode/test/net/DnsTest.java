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

package org.jnode.test.net;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.lang.System.getLogger;


/**
 * @author Martin Hartvig
 */
public class DnsTest {

    private static final Logger logger = getLogger(DnsTest.class.getName());

    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                System.out.println("inetAddress find " + args[0]);

                InetAddress inetAddress = InetAddress.getByName(args[0]);

                System.out.println("inetAddress " + inetAddress.getHostAddress());
            } catch (UnknownHostException e) {
                logger.log(Level.DEBUG, e.getMessage(), e);
                // To change body of catch statement use Options | File Templates.
            }
        } else {
            System.out.println("insert url as arg.");
        }
    }
}
