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

/**
 * @author epr
     */
public interface ICMPConstants {

    /* Codes for UNREACH. */
    int ICMP_NET_UNREACH    = 0;  /* Network Unreachable */
    int ICMP_HOST_UNREACH   = 1;  /* Host Unreachable */
    int ICMP_PROT_UNREACH   = 2;  /* Protocol Unreachable */
    int ICMP_PORT_UNREACH   = 3;  /* Port Unreachable */
    int ICMP_FRAG_NEEDED    = 4;  /* Fragmentation Needed/DF set */
    int ICMP_SR_FAILED      = 5;  /* Source Route failed */
    int ICMP_NET_UNKNOWN    = 6;
    int ICMP_HOST_UNKNOWN   = 7;
    int ICMP_HOST_ISOLATED  = 8;
    int ICMP_NET_ANO        = 9;
    int ICMP_HOST_ANO       = 10;
    int ICMP_NET_UNR_TOS    = 11;
    int ICMP_HOST_UNR_TOS   = 12;
    int ICMP_PKT_FILTERED   = 13; /* Packet filtered */
    int ICMP_PREC_VIOLATION = 14; /* Precedence violation */
    int ICMP_PREC_CUTOFF    = 15; /* Precedence cut off */
    int NR_ICMP_UNREACH     = 15; /* instead of hardcoding immediate value */

    /* Codes for REDIRECT. */
    int ICMP_REDIR_NET      = 0; /* Redirect Net */
    int ICMP_REDIR_HOST     = 1; /* Redirect Host */
    int ICMP_REDIR_NETTOS   = 2; /* Redirect Net for TOS */
    int ICMP_REDIR_HOSTTOS  = 3; /* Redirect Host for TOS */

    /* Codes for TIME_EXCEEDED. */
    int ICMP_EXC_TTL        = 0; /* TTL count exceeded */
    int ICMP_EXC_FRAGTIME   = 1; /* Fragment Reass time exceeded */

}
