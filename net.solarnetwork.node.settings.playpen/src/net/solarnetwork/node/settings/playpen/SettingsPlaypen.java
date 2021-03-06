/* ==================================================================
 * SettingsPlaypen.java - Nov 2, 2012 4:16:05 PM
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

package net.solarnetwork.node.settings.playpen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicRadioGroupSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicSliderSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.settings.support.BasicToggleSettingSpecifier;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * A test bed experiment for the settings framework.
 * 
 * @author matt
 * @version $Revision$
 */
public class SettingsPlaypen implements SettingSpecifierProvider {

	private static final Object MONITOR = new Object();
	private static MessageSource MESSAGE_SOURCE;

	private static final String DEFAULT_STRING = "simple";
	private static final Integer DEFAULT_INTEGER = 42;
	private static final Double DEFAULT_SLIDE = 5.0;
	private static final String[] DEFAULT_RADIO = new String[] { "One", "Two", "Three" };

	private String string = DEFAULT_STRING;
	private Integer integer = DEFAULT_INTEGER;
	private Boolean toggle = Boolean.FALSE;
	private Double slide = DEFAULT_SLIDE;
	private String radio = DEFAULT_RADIO[0];

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.settings.playpen";
	}

	@Override
	public String getDisplayName() {
		return "Settings Playpen";
	}

	@Override
	public MessageSource getMessageSource() {
		synchronized ( MONITOR ) {
			if ( MESSAGE_SOURCE == null ) {
				ResourceBundleMessageSource source = new ResourceBundleMessageSource();
				source.setBundleClassLoader(SettingsPlaypen.class.getClassLoader());
				source.setBasename(SettingsPlaypen.class.getName());
				MESSAGE_SOURCE = source;
			}
		}
		return MESSAGE_SOURCE;
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> results = new ArrayList<SettingSpecifier>(20);

		SettingsPlaypen defaults = new SettingsPlaypen();

		results.add(new BasicTextFieldSettingSpecifier("string", defaults.getString()));
		results.add(new BasicTextFieldSettingSpecifier("integer", defaults.getInteger().toString()));
		results.add(new BasicToggleSettingSpecifier("toggle", defaults.getToggle()));
		results.add(new BasicSliderSettingSpecifier("slide", defaults.getSlide(), 0.0, 10.0, 0.5));

		BasicRadioGroupSettingSpecifier radioSpec = new BasicRadioGroupSettingSpecifier("radio",
				defaults.getRadio());
		Map<String, String> radioValues = new LinkedHashMap<String, String>(3);
		for ( String s : DEFAULT_RADIO ) {
			radioValues.put(s, s);
		}
		radioSpec.setValueTitles(radioValues);
		results.add(radioSpec);

		return results;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public Integer getInteger() {
		return integer;
	}

	public void setInteger(Integer integer) {
		this.integer = integer;
	}

	public Boolean getToggle() {
		return toggle;
	}

	public void setToggle(Boolean toggle) {
		this.toggle = toggle;
	}

	public Double getSlide() {
		return slide;
	}

	public void setSlide(Double slide) {
		this.slide = slide;
	}

	public String getRadio() {
		return radio;
	}

	public void setRadio(String radio) {
		this.radio = radio;
	}

}
