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

/**
 * @author epr
 */
public interface TCPConstants {

    /** Length of TCP header length in bytes, without any options */
    int TCP_HLEN = 20;

    /** Default Time to Live for TCP packets */
    int TCP_DEFAULT_TTL = 64;

    /** Default timeout for blocking operations (in ms) */
    int TCP_DEFAULT_TIMEOUT = 10000;

    /** Maximum TCP window size */
    int TCP_MAXWIN = 1024;

    /** Default Maximum Segment Size */
    int TCP_DEFAULT_MSS = 536;

    /** The default buffer size */
    int TCP_BUFFER_SIZE = 8 * 1024;

    /** The maximum connect attempts */
    int TCP_MAXCONNECT = 3;

    /** Interval between timer events */
    int TCP_TIMER_PERIOD = 500;

    // TCP flags
    int TCPF_FIN = 0x0001;
    int TCPF_SYN = 0x0002;
    int TCPF_RST = 0x0004;
    int TCPF_PSH = 0x0008;
    int TCPF_ACK = 0x0010;
    int TCPF_URG = 0x0020;

    // TCP Connection states
    int TCPS_CLOSED = 0x0001;
    int TCPS_LISTEN = 0x0002;
    int TCPS_SYN_RECV = 0x0003;
    int TCPS_SYN_SENT = 0x0004;
    int TCPS_ESTABLISHED = 0x0005;
    int TCPS_CLOSE_WAIT = 0x0006;
    int TCPS_LAST_ACK = 0x0007;
    int TCPS_FIN_WAIT_1 = 0x0008;
    int TCPS_FIN_WAIT_2 = 0x0009;
    int TCPS_CLOSING = 0x000a;
    int TCPS_TIME_WAIT = 0x000b;

    String[] TCP_STATE_NAMES = {
        "?", "CLOSED", "LISTEN", "SYN_RECV", "SYN_SENT", "ESTABLISHED", "CLOSE_WAIT",
        "LAST_ACK", "FIN_WAIT_1", "FIN_WAIT_2", "CLOSING", "TIME_WAIT"
    };

}
