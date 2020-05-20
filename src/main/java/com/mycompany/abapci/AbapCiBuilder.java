package com.mycompany.abapci;

import com.mycompany.abapci.AdtCommunication.IHttpPostHandler;
import com.mycompany.abapci.AdtCommunication.SapConnectionInfo;
import com.mycompany.abapci.AdtCommunication.SapCredentials;
import com.mycompany.abapci.AdtCommunication.SapServerInfo;
import com.mycompany.abapci.AdtCommunication.UnittestHttpPostHandler;
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

    private String sapPackagename;
    private boolean useJenkinsProjectname;

    @DataBoundConstructor
    public AbapCiBuilder(String sapPackagename) {
        this.sapPackagename = sapPackagename;
    }

    public String getSapPackagename() {
        return sapPackagename;
    }

    @DataBoundSetter
    public void setSapPackagename(String sapPackagename) {
        this.sapPackagename = sapPackagename;
    }

    public boolean getUseJenkinsProjectName() {
        return useJenkinsProjectname;
    }

    @DataBoundSetter
    public void setuseJenkinsProjectName(boolean useJenkinsProjectname) {
        this.useJenkinsProjectname = useJenkinsProjectname;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException, MalformedURLException {

        int numFailedUnittests = -1; 
        
        AbapCiGlobalConfiguration globalConfiguration = AbapCiGlobalConfiguration.get();
        boolean globalConfigurationIsValid = ValidateGlobalConfiguration(globalConfiguration);
        if (globalConfigurationIsValid) {

            listener.getLogger().println("Use jenkins project name as package name: " + useJenkinsProjectname + "!");

            listener.getLogger().println("Start ABAP Unit testrun for SAP packagename: " + sapPackagename + "!");

            String username = globalConfiguration.getSapUsername();
            String password = globalConfiguration.getSapPassword();
            SapCredentials sapCredentials = new SapCredentials(username, password);
            SapServerInfo sapServerInfo = new SapServerInfo(
                    globalConfiguration.getSapProtocol(),
                    globalConfiguration.getSapServername(),
                    globalConfiguration.getSapPort(),
                    globalConfiguration.getSapMandant());
            SapConnectionInfo sapConnectionInfo = new SapConnectionInfo(sapServerInfo, sapCredentials);

            try {
                IHttpPostHandler httpPostHandler = new UnittestHttpPostHandler(sapConnectionInfo, sapPackagename);
                HttpResponse response = httpPostHandler.execute();
                listener.getLogger().println("Response statuscode of testrun: " + response.getStatusLine().getStatusCode());

                if (response.getStatusLine().getStatusCode() == 200) {
                    String responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");;
                    listener.getLogger().println("Response content of testrun: " + responseContent);
                    UnittestResultParser jsonParser = new UnittestResultParser();
                    numFailedUnittests = jsonParser.parseXmlForFailedUnittests(responseContent);
                    listener.getLogger().println("Number of failed unittests: " + numFailedUnittests);

                }
                
            } catch (Exception e) {
                StringWriter sw = new StringWriter(); 
                e.printStackTrace(new PrintWriter(sw));
                listener.getLogger().println("Http Call failed, exception message: " + e.getMessage());
                listener.getLogger().println("Http Call failed, exception stacktrace: " + sw.toString());
                throw new InterruptedException();
            }
                                if (numFailedUnittests > 0)
                    {
                       throw new AbortException("Failed Unit Tests"); 
                    }   

        }
    }

    private boolean ValidateGlobalConfiguration(AbapCiGlobalConfiguration globalConfiguration) {
        boolean servernameIsSet = !StringUtils.isEmpty(globalConfiguration.getSapServername());
        //TODO 
        return servernameIsSet;
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingPackageName());
            }

            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.HelloWorldBuilder_DescriptorImpl_DisplayName();
        }

    }

}
