package com.mycompany.abapci;

import com.mycompany.abapci.util.IntegerValidator;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * Example of Jenkins global configuration.
 */
@Extension
public class AbapCiGlobalConfiguration extends GlobalConfiguration {

    /**
     * @return the singleton instance
     */
    public static AbapCiGlobalConfiguration get() {
        return GlobalConfiguration.all().get(AbapCiGlobalConfiguration.class);
    }

    private String sapServername;
    private int sapPort;
    private String sapProtocol;
    private String sapMandant;
    private String sapUsername;
    private Secret sapPassword;

    public AbapCiGlobalConfiguration() {
        load();
    }

    public String getSapServername() {
        return sapServername;
    }

    @DataBoundSetter
    public void setSapServername(String servername) {
        this.sapServername = servername;
        save();
    }

    public FormValidation doCheckSapServername(@QueryParameter String value) {

        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify the SAP Servername of the testing system. (eg. vhcalnplci.dummy.nodomain");
        }

        return FormValidation.ok();
    }

    public int getSapPort() {
        return sapPort;
    }

    @DataBoundSetter
    public void setSapPort(int port) {
        this.sapPort = port;
        save();
    }

    public FormValidation doCheckSapPort(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify the used port to connect to the SAP system.");
        }
        if (!IntegerValidator.isInteger(value)) {
            return FormValidation.warning("As SAP port only an integer is allowed");
        }

        return FormValidation.ok();
    }

    public String getSapProtocol() {
        return sapProtocol;
    }

    @DataBoundSetter
    public void setSapProtocol(String protocol) {
        this.sapProtocol = protocol;
        save();
    }

    public FormValidation doCheckSapProtocol(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify the used Protocol (http or https).");
        }

        if (!"http".equals(value) || !"https".equals(value)) {
            return FormValidation.warning("As protocol only http or https is allowed.");
        }

        return FormValidation.ok();

    }

    public String getSapMandant() {
        return sapMandant;
    }

    @DataBoundSetter
    public void setSapMandant(String mandant) {
        this.sapMandant = mandant;
        save();
    }

    public FormValidation doCheckSapMandant(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify the SAP Client where the tests should be run.");
        }

        if (!IntegerValidator.isInteger(value)) {
            return FormValidation.warning("Only an integer is allowed as client number.");
        }

        return FormValidation.ok();
    }

    public String getSapUsername() {
        return sapUsername;
    }

    @DataBoundSetter
    public void setSapUsername(String username) {
        this.sapUsername = username;
        save();
    }

    public FormValidation doCheckSapUsername(@QueryParameter String value) {

        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify the SAP Username of the testing system.");
        }

        return FormValidation.ok();
    }

    public Secret getSapPassword() {
        return sapPassword;
    }

    @DataBoundSetter
    public void setSapPassword(Secret password) {
        this.sapPassword = password;
        save();
    }

    public FormValidation doCheckSapPassword(@QueryParameter String value) {

        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify the SAP Password of the testing system.");
        }

        return FormValidation.ok();
    }
}
