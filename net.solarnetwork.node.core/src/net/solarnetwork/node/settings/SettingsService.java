/* ==================================================================
 * SettingsService.java - Mar 12, 2012 4:58:14 PM
 * 
 * Copyright 2007-2012 SolarNetwork.net Dev Team
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
 * $Id$
 * ==================================================================
 */

package net.solarnetwork.node.settings;

import java.util.List;
import java.util.Map;

/**
 * Service API for settings.
 * 
 * @author matt
 * @version $Revision$
 */
public interface SettingsService {

	/**
	 * Get a list of all possible non-factory setting providers.
	 * 
	 * @return list of setting providers (never <em>null</em>)
	 */
	List<SettingSpecifierProvider> getProviders();

	/**
	 * Get a list of all possible setting provider factories.
	 * 
	 * @return list of setting provider factories (never <em>null</em>)
	 */
	List<SettingSpecifierProviderFactory> getProviderFactories();

	/**
	 * Get a specific factory for a given UID.
	 * 
	 * @param factoryUID
	 *            the factory UID to get the providers for
	 * 
	 * @return the factory, or <em>null</em> if not available
	 */
	SettingSpecifierProviderFactory getProviderFactory(String factoryUID);

	/**
	 * Add a new factory instance, and return the new instance ID.
	 * 
	 * @param factoryUID
	 *            the factory UID to create the new instance for
	 * @return the new instance ID
	 */
	String addProviderFactoryInstance(String factoryUID);

	/**
	 * Delete an existing factory instance.
	 * 
	 * @param factoryUID
	 *            the factory UID to create the new instance for
	 * @param instanceUID
	 *            the instance UID to create the new instance for
	 */
	void deleteProviderFactoryInstance(String factoryUID, String instanceUID);

	/**
	 * Get all possible setting providers for a specific factory UID,
	 * grouped by instance ID.
	 * 
	 * @param factoryUID
	 *            the factory UID to get the providers for
	 * 
	 * @return mapping of instance IDs to list of setting providers (never
	 *         <em>null</em>)
	 */
	Map<String, List<FactorySettingSpecifierProvider>> getProvidersForFactory(String factoryUID);

	/**
	 * Get the current value of a setting.
	 * 
	 * @param provider
	 *            the provider of the setting
	 * @param setting
	 *            the setting
	 * @return the currernt setting value
	 */
	Object getSettingValue(SettingSpecifierProvider provider, SettingSpecifier setting);

	/**
	 * Update setting values.
	 * 
	 * @param command
	 *            the update command
	 */
	void updateSettings(SettingsCommand command);
}
