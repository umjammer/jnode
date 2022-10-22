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

package org.jnode.net.wireless;

/**
 * Constants for wireless LAN protocol 802.11
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface WirelessConstants {

    // --- Sizes -----------------------------------------------
    int WLAN_ADDR_LEN = 6;

    int WLAN_CRC_LEN = 4;

    int WLAN_BSSID_LEN = 6;

    int WLAN_BSS_TS_LEN = 8;

    int WLAN_HDR_A3_LEN = 24;

    int WLAN_HDR_A4_LEN = 30;

    int WLAN_SSID_MAXLEN = 32;

    int WLAN_DATA_MAXLEN = 2312;

    int WLAN_A3FR_MAXLEN = (WLAN_HDR_A3_LEN + WLAN_DATA_MAXLEN + WLAN_CRC_LEN);

    int WLAN_A4FR_MAXLEN = (WLAN_HDR_A4_LEN + WLAN_DATA_MAXLEN + WLAN_CRC_LEN);

    int WLAN_BEACON_FR_MAXLEN = (WLAN_HDR_A3_LEN + 334);

    int WLAN_ATIM_FR_MAXLEN = (WLAN_HDR_A3_LEN + 0);

    int WLAN_DISASSOC_FR_MAXLEN = (WLAN_HDR_A3_LEN + 2);

    int WLAN_ASSOCREQ_FR_MAXLEN = (WLAN_HDR_A3_LEN + 48);

    int WLAN_ASSOCRESP_FR_MAXLEN = (WLAN_HDR_A3_LEN + 16);

    int WLAN_REASSOCREQ_FR_MAXLEN = (WLAN_HDR_A3_LEN + 54);

    int WLAN_REASSOCRESP_FR_MAXLEN = (WLAN_HDR_A3_LEN + 16);

    int WLAN_PROBEREQ_FR_MAXLEN = (WLAN_HDR_A3_LEN + 44);

    int WLAN_PROBERESP_FR_MAXLEN = (WLAN_HDR_A3_LEN + 78);

    int WLAN_AUTHEN_FR_MAXLEN = (WLAN_HDR_A3_LEN + 261);

    int WLAN_DEAUTHEN_FR_MAXLEN = (WLAN_HDR_A3_LEN + 2);

    int WLAN_WEP_NKEYS = 4;

    int WLAN_WEP_MAXKEYLEN = 13;

    int WLAN_CHALLENGE_IE_LEN = 130;

    int WLAN_CHALLENGE_LEN = 128;

    int WLAN_WEP_IV_LEN = 4;

    int WLAN_WEP_ICV_LEN = 4;

}
