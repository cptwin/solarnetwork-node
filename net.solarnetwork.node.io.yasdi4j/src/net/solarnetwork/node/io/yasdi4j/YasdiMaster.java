/* ==================================================================
 * YasdiMaster.java - Mar 7, 2013 9:27:13 AM
 * 
 * Copyright 2007-2013 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.node.io.yasdi4j;

import de.michaeldenk.yasdi4j.YasdiDevice;

/**
 * API for a YASDI master component.
 * 
 * @author matt
 * @version 1.0
 */
public interface YasdiMaster {

	/**
	 * Get a descriptive name for this instance.
	 * 
	 * @return the name
	 */
	String getName();

	/**
	 * Get a unique identifier for this instance.
	 * 
	 * <p>
	 * This should be meaningful to the factory implementation. For example a
	 * serial port based implementation could use the port identifier as the
	 * UID.
	 * </p>
	 * 
	 * @return unique identifier
	 */
	String getUID();

	/**
	 * Get the communication device used by this instance.
	 * 
	 * <p>
	 * For serial connections, this will be the serial port, for example
	 * {@code /dev/ttyS0}. For IP connections, this will be the IP address.
	 * </p>
	 * 
	 * @return the communication ID
	 */
	String getCommDevice();

	/**
	 * Get the {@link YasdiDevice}.
	 * 
	 * @return YasdiDevice
	 */
	YasdiDevice getDevice();

}
