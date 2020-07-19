package com.mycompany.abapci;

import com.mycompany.abapci.AdtCommunication.AtcHttpPostHandler;
import com.mycompany.abapci.AdtCommunication.IHttpPostHandler;
import com.mycompany.abapci.AdtCommunication.SapConnectionInfo;
import com.mycompany.abapci.AdtCommunication.SapCredentials;
import com.mycompany.abapci.AdtCommunication.SapServerInfo;
import com.mycompany.abapci.AdtCommunication.UnittestHttpPostHandler;
import com.mycompany.resultParser.AtcCheckResultParser;
import com.mycompany.resultParser.UnittestResultParser;
import hudson.AbortException;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

public class AbapCiBuilder extends Builder implements SimpleBuildStep {

    private String abapPackagename;
    private boolean useJenkinsProjectname;
    private boolean runUnitTests;
    private boolean runAtcChecks;

    @DataBoundConstructor
    public AbapCiBuilder(String abapPackagename) {
        this.abapPackagename = abapPackagename;
    }

    public String getAbapPackagename() {
        return abapPackagename;
    }

    @DataBoundSetter
    public void setAbapPackagename(String sapPackagename) {
        this.abapPackagename = sapPackagename;
    }

    public boolean getUseJenkinsProjectName() {
        return useJenkinsProjectname;
    }

    @DataBoundSetter
    public void setuseJenkinsProjectName(boolean useJenkinsProjectname) {
        this.useJenkinsProjectname = useJenkinsProjectname;
    }
    
    public boolean isRunUnitTests() {
        return runUnitTests;
    }

    @DataBoundSetter
    public void setRunUnitTests(boolean runUnitTests) {
        this.runUnitTests = runUnitTests;
    }

    public boolean isRunAtcChecks() {
        return runAtcChecks;
    }

    @DataBoundSetter
    public void setRunAtcChecks(boolean runAtcChecks) {
        this.runAtcChecks = runAtcChecks;
    }


    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException, MalformedURLException {

        int numFailedUnittests = -1;
        int numCriticalAtcChecks = -1;

        AbapCiGlobalConfiguration globalConfiguration = AbapCiGlobalConfiguration.get();
        boolean globalConfigurationIsValid = ValidateGlobalConfiguration(globalConfiguration);
        if (globalConfigurationIsValid) {

            listener.getLogger().println("Use jenkins project name as package name: " + useJenkinsProjectname + "!");

            String username = globalConfiguration.getSapUsername();
            Secret password = globalConfiguration.getSapPassword();
            SapCredentials sapCredentials = new SapCredentials(username, password);
            SapServerInfo sapServerInfo = new SapServerInfo(
                    globalConfiguration.getSapProtocol(),
                    globalConfiguration.getSapServername(),
                    globalConfiguration.getSapPort(),
                    globalConfiguration.getSapMandant());
            SapConnectionInfo sapConnectionInfo = new SapConnectionInfo(sapServerInfo, sapCredentials);

            try {
                
                listener.getLogger().println("Run Unit Test flag is: " + isRunUnitTests());
                if (isRunUnitTests()) {
                    listener.getLogger().println("Start ABAP Unit testrun for SAP packagename: " + abapPackagename + "!");

                    IHttpPostHandler httpPostHandler = new UnittestHttpPostHandler(sapConnectionInfo, abapPackagename, listener);
                    HttpResponse response = httpPostHandler.executeWithToken();
                    listener.getLogger().println("Response statuscode of unit testrun: " + response.getStatusLine().getStatusCode());

                    if (response.getStatusLine().getStatusCode() == 200) {
                        String responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
                        listener.getLogger().println("Response content of unit testrun: " + responseContent);
                        UnittestResultParser jsonParser = new UnittestResultParser();
                        numFailedUnittests = jsonParser.parseXmlForFailedElements(responseContent);
                        listener.getLogger().println("Number of failed unittests: " + numFailedUnittests);

                    }
                }

                listener.getLogger().println("Run ATC checks flag is: " + isRunAtcChecks());
                if (isRunAtcChecks()) {
                    listener.getLogger().println("Start ATC checkrun for SAP packagename: " + abapPackagename + "!");

                    IHttpPostHandler httpPostHandlerAtc = new AtcHttpPostHandler(sapConnectionInfo, abapPackagename, listener);
                    HttpResponse atcResponse = httpPostHandlerAtc.executeWithToken();
                    listener.getLogger().println("Response statuscode of atc run: " + atcResponse.getStatusLine().getStatusCode());

                    if (atcResponse.getStatusLine().getStatusCode() == 200) {
                        String responseContent = EntityUtils.toString(atcResponse.getEntity(), "UTF-8");
                        listener.getLogger().println("Response content of ÁTC checks: " + responseContent);
                        AtcCheckResultParser jsonParser = new AtcCheckResultParser();
                        numCriticalAtcChecks = jsonParser.parseXmlForFailedElements(responseContent);
                        listener.getLogger().println("Number of failed ATC checks: " + numCriticalAtcChecks);
                    }
                }
            } catch (RuntimeException rex) {
                throw rex;  
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                listener.getLogger().println("Http Call failed, exception message: " + e.getMessage());
                listener.getLogger().println("Http Call failed, exception stacktrace: " + sw.toString());
                throw new InterruptedException();
            }
            if (numFailedUnittests > 0) {
                throw new AbortException("Failed Unit tests");
            }
            if (numCriticalAtcChecks > 0) {
                throw new AbortException("Failed ATC checks");
            }

        }
    }

    private boolean ValidateGlobalConfiguration(AbapCiGlobalConfiguration globalConfiguration) {
        boolean servernameIsSet = !StringUtils.isEmpty(globalConfiguration.getSapServername());
        //TODO 
        return servernameIsSet;
    }

    @Symbol("abapCi")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please set a package name");
            }

            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "ABAP Continuous Integration Plugin";
        }

    }

}
