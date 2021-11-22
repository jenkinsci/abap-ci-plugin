package com.mycompany.abapci;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.mycompany.abapci.util.IntegerValidator;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.Secret;

public class SAPSystem extends AbstractDescribableImpl<SAPSystem> {

	private String label;
	private String sapServername;
	private int sapPort;
	private String sapProtocol;
	private String sapMandant;
	private String sapUsername;
	private Secret sapPassword;

	@DataBoundConstructor
	public SAPSystem(String sapServername, int sapPort, String sapProtocol, String sapMandant, String sapUsername,
			Secret sapPassword, String label) {
		super();
		this.sapServername = sapServername;
		this.sapPort = sapPort;
		this.sapProtocol = sapProtocol;
		this.sapMandant = sapMandant;
		this.sapUsername = sapUsername;
		this.sapPassword = sapPassword;
		this.label = label;
	}

	public String getSapServername() {
		return sapServername;
	}

	public int getSapPort() {
		return sapPort;
	}

	public String getSapProtocol() {
		return sapProtocol;
	}

	public String getSapMandant() {
		return sapMandant;
	}

	public String getSapUsername() {
		return sapUsername;
	}

	public Secret getSapPassword() {
		return sapPassword;
	}

	public String getLabel() {
		return label;
	}
	
	public String getLabelAndHost() {
		return getLabel() + " (" + getSapProtocol() + "://" + getSapServername() + ":" + getSapPort() + ")";
	}

	@Extension
	public static class SAPSystemDescriptor extends Descriptor<SAPSystem> {

		public FormValidation doCheckSapProtocol(@QueryParameter String value) {
			if (StringUtils.isEmpty(value)) {
				return FormValidation.error("Please specify the protocol (http or https).");
			}

			if (!"http".equals(value) || !"https".equals(value)) {
				return FormValidation.error("As protocol only http or https is allowed.");
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckSapUsername(@QueryParameter String value) {
			if (StringUtils.isEmpty(value)) {
				return FormValidation.error("Please specify the SAP username.");
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckSapPassword(@QueryParameter String value) {
			if (StringUtils.isEmpty(value)) {
				return FormValidation.error("Please specify the password.");
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckSapMandant(@QueryParameter String value) {
			if (StringUtils.isEmpty(value)) {
				return FormValidation.error("Please specify the SAP Client where the tests should be run.");
			}

			if (!IntegerValidator.isInteger(value)) {
				return FormValidation.error("Only an integer is allowed as a client number.");
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckSapServername(@QueryParameter String value) {
			if (StringUtils.isEmpty(value)) {
				return FormValidation.error("Please specify the SAP server name (eg. vhcalnplci.dummy.nodomain).");
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckLabel(@QueryParameter String value) {
			if (StringUtils.isEmpty(value)) {
				return FormValidation.error(
						"Please specify a label for referencing this system in steps or pipelines (word characters, no spaces).");
			}
			
			if(!value.matches("\\w*")) {
				return FormValidation.error("The SAP system label contains illegal characters.");
			}

			return FormValidation.ok();
		}

		@NonNull
		@Override
		public String getDisplayName() {
			return "SAP system details";
		}
	}
}