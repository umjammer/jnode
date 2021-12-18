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

package org.jnode.net.ethernet;

import org.jnode.net.HardwareAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class EthernetAddressTest {

    private static final String TEST_MAC_ADDRESS_STRING = "00-14-22-49-DD-2B";
    private static final String TEST_MAC_ADDRESS_STRING_DISPLAY = "00:14:22:49:DD:2B";
    private static final byte TEST_MAC_ADDRESS_BYTE_0 = (byte) 0x00;
    private static final byte TEST_MAC_ADDRESS_BYTE_1 = (byte) 0x14;
    private static final byte TEST_MAC_ADDRESS_BYTE_2 = (byte) 0x22;
    private static final byte TEST_MAC_ADDRESS_BYTE_3 = (byte) 0x49;
    private static final byte TEST_MAC_ADDRESS_BYTE_4 = (byte) 0xDD;
    private static final byte TEST_MAC_ADDRESS_BYTE_5 = (byte) 0x2B;
    private static final String TEST_MAC_ADDRESS_STRING_WRONG = "00-14-22-49-XX-2B";
    private static final String TEST_MAC_ADDRESS_STRING_WRONG_LENGTH = "00-14-22-49-DD-2B-32";

    @Test
    public void testToString() {
        EthernetAddress mac =
                new EthernetAddress(TEST_MAC_ADDRESS_BYTE_0, TEST_MAC_ADDRESS_BYTE_1,
                        TEST_MAC_ADDRESS_BYTE_2, TEST_MAC_ADDRESS_BYTE_3, TEST_MAC_ADDRESS_BYTE_4,
                        TEST_MAC_ADDRESS_BYTE_5);
        assertEquals(TEST_MAC_ADDRESS_STRING_DISPLAY, mac.toString());
    }

    @Test
    public void testEthernetAddressString() {
        EthernetAddress mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING);
        assertNotNull(mac);
        try {
            mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING_WRONG_LENGTH);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Invalid address"));
        }
        try {
            mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING_WRONG);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Not an ethernet address"));
        }
    }

    @Test
    public void testEthernetAddressByteByteByteByteByteByte() {
        EthernetAddress mac =
                new EthernetAddress(TEST_MAC_ADDRESS_BYTE_0, TEST_MAC_ADDRESS_BYTE_1,
                        TEST_MAC_ADDRESS_BYTE_2, TEST_MAC_ADDRESS_BYTE_3, TEST_MAC_ADDRESS_BYTE_4,
                        TEST_MAC_ADDRESS_BYTE_5);
        assertNotNull(mac);
    }

    @Test
    public void testEqualsEthernetAddress() {
        EthernetAddress mac =
                new EthernetAddress(TEST_MAC_ADDRESS_BYTE_0, TEST_MAC_ADDRESS_BYTE_1,
                        TEST_MAC_ADDRESS_BYTE_2, TEST_MAC_ADDRESS_BYTE_3, TEST_MAC_ADDRESS_BYTE_4,
                        TEST_MAC_ADDRESS_BYTE_5);
        assertTrue(mac.equals(new EthernetAddress(TEST_MAC_ADDRESS_STRING)));
    }

    @Test
    public void testEqualsHardwareAddress() {
        EthernetAddress mac =
                new EthernetAddress(TEST_MAC_ADDRESS_BYTE_0, TEST_MAC_ADDRESS_BYTE_1,
                        TEST_MAC_ADDRESS_BYTE_2, TEST_MAC_ADDRESS_BYTE_3, TEST_MAC_ADDRESS_BYTE_4,
                        TEST_MAC_ADDRESS_BYTE_5);
        HardwareAddress mac2 = new EthernetAddress(TEST_MAC_ADDRESS_STRING);
        assertTrue(mac.equals(mac2));
    }

    @Test
    public void testGetLength() {
        EthernetAddress mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING);
        assertEquals(EthernetConstants.ETH_ALEN, mac.getLength());
    }

    @Test
    public void testGet() {
        EthernetAddress mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING);
        assertEquals(TEST_MAC_ADDRESS_BYTE_0, mac.get(0));
        assertEquals(TEST_MAC_ADDRESS_BYTE_1, mac.get(1));
        assertEquals(TEST_MAC_ADDRESS_BYTE_2, mac.get(2));
        assertEquals(TEST_MAC_ADDRESS_BYTE_3, mac.get(3));
        assertEquals(TEST_MAC_ADDRESS_BYTE_4, mac.get(4));
        assertEquals(TEST_MAC_ADDRESS_BYTE_5, mac.get(5));
    }

    @Test
    public void testIsBroadcast() {
        EthernetAddress mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING);
        assertFalse(mac.isBroadcast());
        EthernetAddress broadcast = new EthernetAddress("FF-FF-FF-FF-FF-FF");
        assertTrue(broadcast.isBroadcast());
    }

    @Test
    public void testGetType() {
        EthernetAddress mac = new EthernetAddress(TEST_MAC_ADDRESS_STRING);
        assertEquals(1, mac.getType());
    }

}
