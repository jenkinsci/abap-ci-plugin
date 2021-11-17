package com.mycompany.abapci;

import java.util.ArrayList;
import java.util.List;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;

/**
 * ABAP CI global configuration.
 */
@Extension
public class AbapCiGlobalConfiguration extends GlobalConfiguration {

	/**
	 * @return the singleton instance
	 */
	public static AbapCiGlobalConfiguration get() {
		return GlobalConfiguration.all().get(AbapCiGlobalConfiguration.class);
	}

	private List<SAPSystem> sapSystems = new ArrayList<>();

	public AbapCiGlobalConfiguration() {
		load();
	}

	public List<SAPSystem> getSapSystems() {
		return sapSystems;
	}

	public void setSapSystems(List<SAPSystem> sapSystems) {
		this.sapSystems = sapSystems;
		save();
	}
}
