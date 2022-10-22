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

package org.jnode.fs.iso9660;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ISO9660Constants {

    char SEPARATOR1 = '.';
    char SEPARATOR2 = ';';

    String DEFAULT_ENCODING = "US-ASCII";

    interface VolumeDescriptorType {

        int TERMINATOR = 255;

        int BOOTRECORD = 0;

        int PRIMARY_DESCRIPTOR = 1;

        int SUPPLEMENTARY_DESCRIPTOR = 2;

        int PARTITION_DESCRIPTOR = 3;
    }

}
