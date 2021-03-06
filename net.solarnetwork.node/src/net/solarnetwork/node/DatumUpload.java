/* ===================================================================
 * DatumUpload.java
 * 
 * Created Nov 30, 2009 5:00:30 PM
 * 
 * Copyright 2007-2009 SolarNetwork.net Dev Team
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

package net.solarnetwork.node;

import java.util.Date;

/**
 * API for an "upload" domain object, used when uploading locally
 * persisted Datum objects to a remote service.
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public interface DatumUpload {

	/**
	 * Get the primary key, or <em>null</em> if not a persisted object.
	 * 
	 * @return the id
	 */
	public Long getId();
	
	/**
	 * Get the ID of the datum associated with this upload.
	 * 
	 * @return the datumId
	 */
	public Long getDatumId();
	
	/**
	 * Get the tracking ID issued by the remote upload service when
	 * this datum was successfully uploaded.
	 * 
	 * <p>This normally equates to the primary key generated by the
	 * remote service when the datum is uploaded.</p>
	 * 
	 * @return the trackingId
	 */
	public Long getTrackingId();
	
	/**
	 * Get the destination for the datum to be uploaded to.
	 * 
	 * <p>This is simply a unique identifier for the upload destination,
	 * which could be a full URL or simply a human-readable identifier.
	 * 
	 * @return the destination
	 */
	public String getDestination();
	
	/**
	 * Get the date this object was created, which is normally equal
	 * to the date it was persisted.
	 * 
	 * @return the created date
	 */
	public Date getCreated();

	
}
