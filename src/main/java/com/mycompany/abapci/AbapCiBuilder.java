package com.mycompany.abapci;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.mycompany.abapci.AdtCommunication.AtcHttpPostHandler;
import com.mycompany.abapci.AdtCommunication.IHttpPostHandler;
import com.mycompany.abapci.AdtCommunication.SapConnectionInfo;
import com.mycompany.abapci.AdtCommunication.SapCredentials;
import com.mycompany.abapci.AdtCommunication.SapServerInfo;
import com.mycompany.abapci.AdtCommunication.UnittestHttpPostHandler;
import com.mycompany.resultParser.AtcCheckResultParser;
import com.mycompany.resultParser.UnitTestResult;
import com.mycompany.resultParser.UnittestResultParser;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.tasks.SimpleBuildStep;

public class AbapCiBuilder extends Builder implements SimpleBuildStep {

	private String abapPackagename;
	private boolean useJenkinsProjectname;
	private boolean runUnitTests;
	private boolean runAtcChecks;
	private String atcVariant;
	private boolean treatWarningAtcChecksAsErrors;

	@DataBoundConstructor
	public AbapCiBuilder(String abapPackagename, String atcVariant) {
		this.abapPackagename = abapPackagename;
		
		if(atcVariant == null || atcVariant.length() == 0) {
			this.atcVariant = "DEFAULT";
		} else {
			this.atcVariant = atcVariant;
		}
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

	public String getAtcVariant() {
		return this.atcVariant;
	}

	@DataBoundSetter
	public void setAtcVariant(String variant) {
		this.atcVariant = variant;
	}
	
	public boolean getTreatWarningAtcChecksAsErrors() {
		return treatWarningAtcChecksAsErrors;
	}
	
	@DataBoundSetter
	public void setTreatWarningAtcChecksAsErrors(boolean treatWarningAtcChecksAsErrors) {
		this.treatWarningAtcChecksAsErrors = treatWarningAtcChecksAsErrors;
	}

	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException, MalformedURLException {

		int numFailedUnitTests = -1;
		int numCriticalAtcChecks = -1;

		AbapCiGlobalConfiguration globalConfiguration = AbapCiGlobalConfiguration.get();
		boolean globalConfigurationIsValid = ValidateGlobalConfiguration(globalConfiguration);

		if (globalConfigurationIsValid) {
			listener.getLogger().println("Use jenkins project name as package name: " + useJenkinsProjectname + "!");

			String username = globalConfiguration.getSapUsername();
			Secret password = globalConfiguration.getSapPassword();
			SapCredentials sapCredentials = new SapCredentials(username, password);
			SapServerInfo sapServerInfo = new SapServerInfo(globalConfiguration.getSapProtocol(),
					globalConfiguration.getSapServername(), globalConfiguration.getSapPort(),
					globalConfiguration.getSapMandant());
			SapConnectionInfo sapConnectionInfo = new SapConnectionInfo(sapServerInfo, sapCredentials);

			try {
				listener.getLogger().println("Run Unit Test flag is: " + isRunUnitTests());

				if (isRunUnitTests()) {
					listener.getLogger()
							.println("########## Start ABAP Unit testrun for SAP packagename: " + abapPackagename + "! ##########");

					IHttpPostHandler httpPostHandler = new UnittestHttpPostHandler(sapConnectionInfo, abapPackagename,
							listener);
					HttpResponse response = httpPostHandler.executeWithToken();
					listener.getLogger().println(
							"Response statuscode of unit testrun: " + response.getStatusLine().getStatusCode());

					if (response.getStatusLine().getStatusCode() == 200) {
						String responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
						// listener.getLogger().println("Response content of unit testrun: " +
						// responseContent);
						UnittestResultParser jsonParser = new UnittestResultParser();
						UnitTestResult unitTestResult = jsonParser.parseXmlForFailedElements(responseContent);

						if (unitTestResult.getMessages().size() > 0) {
							listener.getLogger().println("---------------------------");
						}

						unitTestResult.getMessages().forEach(item -> {
							String message = (String) item;
							listener.getLogger().println(message);
						});

						numFailedUnitTests = unitTestResult.getNumOfFailedTests();

						listener.getLogger().println("Number of failed unittests: " + numFailedUnitTests);

					}
				}

				listener.getLogger().println("Run ATC checks flag is: " + isRunAtcChecks());

				if (isRunAtcChecks()) {
					listener.getLogger().println("########## Start ATC checkrun for SAP packagename: " + abapPackagename + "! ##########");

					IHttpPostHandler httpPostHandlerAtc = new AtcHttpPostHandler(sapConnectionInfo, abapPackagename,
							listener, this.atcVariant);
					HttpResponse atcResponse = httpPostHandlerAtc.executeWithToken();
					listener.getLogger()
							.println("Response statuscode of atc run: " + atcResponse.getStatusLine().getStatusCode());

					if (atcResponse.getStatusLine().getStatusCode() == 200) {
						String responseContent = EntityUtils.toString(atcResponse.getEntity(), "UTF-8");
						//listener.getLogger().println("Response content of Ã�TC checks: " + responseContent);
						AtcCheckResultParser jsonParser = new AtcCheckResultParser(this.treatWarningAtcChecksAsErrors);
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

			if (numFailedUnitTests > 0 && numCriticalAtcChecks > 0) {
				throw new AbortException("Failed unit tests and ATC checks");
			}

			if (numFailedUnitTests > 0) {
				throw new AbortException("Failed unit tests");
			}

			if (numCriticalAtcChecks > 0) {
				throw new AbortException("Failed ATC checks");
			}

		}
	}

	private boolean ValidateGlobalConfiguration(AbapCiGlobalConfiguration globalConfiguration) {
		boolean servernameIsSet = !StringUtils.isEmpty(globalConfiguration.getSapServername());
		// TODO
		return servernameIsSet;
	}

	@Symbol("abapCi")
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

		public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException {
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
