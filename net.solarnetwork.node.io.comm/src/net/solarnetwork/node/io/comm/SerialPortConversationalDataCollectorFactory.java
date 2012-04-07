/* ===================================================================
 * SerialPortConversationalDataCollectorFactory.java
 * 
 * Created Aug 19, 2009 1:00:32 PM
 * 
 * Copyright (c) 2009 Solarnetwork.net Dev Team.
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
 * ===================================================================
 * $Id$
 * ===================================================================
 */

package net.solarnetwork.node.io.comm;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;

import org.springframework.beans.factory.ObjectFactory;

/**
 * {@link GenericObjectFactory} for {@link SerialPortConversationalDataCollector}
 * objects.
 * 
 * <p>Configure the properties of this class, then calls to {@link #getObject()} will
 * return new instances of {@link SerialPortConversationalDataCollector} for each
 * invocation, configured with this object's property values.</p>
 * 
 * @author matt
 * @version $Revision$ $Date$
 * @param <T> the datum type
 */
public class SerialPortConversationalDataCollectorFactory<T>
extends AbstractSerialPortSupportFactory 
implements ObjectFactory<SerialPortConversationalDataCollector<T>> {

	private String commPortAppName = getClass().getName();
	private CommPortIdentifier portId = null;

	/* (non-Javadoc)
	 * @see net.sf.solarnetwork.util.GenericObjectFactory#getObject()
	 */
	public SerialPortConversationalDataCollector<T> getObject() {
		if ( this.portId == null ) {
			this.portId = getCommPortIdentifier();
		}
		try {
			// establish the serial port connection
			SerialPort port = (SerialPort)portId.open(this.commPortAppName, 2000);
			SerialPortConversationalDataCollector<T> obj 
				= new SerialPortConversationalDataCollector<T>(port, getMaxWait());
			setupSerialPortSupport(obj);
			return obj;
		} catch ( PortInUseException e ) {
			throw new RuntimeException(e);
		} catch ( IllegalArgumentException e ) {
			throw new RuntimeException(e);
		}
	}
	
}
