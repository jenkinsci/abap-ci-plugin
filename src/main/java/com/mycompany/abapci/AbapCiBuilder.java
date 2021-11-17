package com.mycompany.abapci;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.List;

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
import com.mycompany.result.AtcCheckResult;
import com.mycompany.result.AtcCheckResultParser;
import com.mycompany.result.UnitTestCheckResult;
import com.mycompany.result.UnittestResultParser;

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
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;

public class AbapCiBuilder extends Builder implements SimpleBuildStep {

	private String abapPackagename;
	private boolean useJenkinsProjectname;
	private boolean runUnitTests;
	private boolean runAtcChecks;
	private String atcVariant;
	private boolean treatWarningAtcChecksAsErrors;
	private String sapSystem;

	@DataBoundConstructor
	public AbapCiBuilder(String abapPackagename, String atcVariant, String sapSystem) {
		this.abapPackagename = abapPackagename;

		if (atcVariant == null || atcVariant.length() == 0) {
			this.atcVariant = "DEFAULT";
		} else {
			this.atcVariant = atcVariant;
		}

		this.setSapSystem(sapSystem);
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

	public String getSapSystem() {
		return sapSystem;
	}

	@DataBoundSetter
	public void setSapSystem(String sapSystem) {
		this.sapSystem = sapSystem;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException, MalformedURLException {

		PrintStream logger = listener.getLogger();
		String sapSystemFromTask = getSapSystem();
		SAPSystem sapSystem = getSapSystemFromConfig(sapSystemFromTask);

		if (sapSystem == null) {
			throw new AbortException("Could not find the configuration for SAP system " + sapSystemFromTask);
		}

		logger.println("Use Jenkins project name as the package name: " + useJenkinsProjectname);

		if (!validateServerConfiguration(sapSystem)) {
			throw new AbortException("Incorrect configuration for SAP system " + sapSystem.getSapServername());
		}

		int numFailedUnitTests = -1;
		int numCriticalAtcChecks = -1;

		SapCredentials sapCredentials = new SapCredentials(sapSystem.getSapUsername(), sapSystem.getSapPassword());
		SapServerInfo sapServerInfo = new SapServerInfo(sapSystem.getSapProtocol(), sapSystem.getSapServername(),
				sapSystem.getSapPort(), sapSystem.getSapMandant());
		SapConnectionInfo sapConnectionInfo = new SapConnectionInfo(sapServerInfo, sapCredentials);

		try {
			logger.println("###########################################");
			logger.println("# SAP system: " + sapSystem.getSapServername());
			logger.println("###########################################");

			if (isRunUnitTests()) {
				logger.println("########## Start ABAP Unit test run for SAP system " + sapSystem.getSapServername()
						+ ":" + sapSystem.getSapPort() + ", package: " + abapPackagename + " ##########");
				logger.println("Run Unit Test flag is: " + isRunUnitTests());

				IHttpPostHandler httpPostHandler = new UnittestHttpPostHandler(sapConnectionInfo, abapPackagename,
						listener);
				HttpResponse response = httpPostHandler.executeWithToken();
				logger.println("Response status code of unit test run: " + response.getStatusLine().getStatusCode());

				if (response.getStatusLine().getStatusCode() == 200) {
					String responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
					UnittestResultParser jsonParser = new UnittestResultParser();
					UnitTestCheckResult unitTestResult = jsonParser.parseXmlForFailedElements(responseContent);

					if (unitTestResult.getMessages().size() > 0) {
						logger.println("---------------------------");
					}

					unitTestResult.getMessages().forEach(item -> {
						String message = (String) item;
						logger.println(message);
					});

					numFailedUnitTests = unitTestResult.getNumberOfFailedTests();

					logger.println("Number of failed unit tests: " + numFailedUnitTests);
				}
			}

			logger.println("Run ATC checks flag is: " + isRunAtcChecks());

			if (isRunAtcChecks()) {
				logger.println("########## Start ATC check run for SAP system " + sapSystem.getSapServername() + ":"
						+ sapSystem.getSapPort() + ", package: " + abapPackagename + " ##########");

				IHttpPostHandler httpPostHandlerAtc = new AtcHttpPostHandler(sapConnectionInfo, abapPackagename,
						listener, this.atcVariant);
				HttpResponse atcResponse = httpPostHandlerAtc.executeWithToken();
				logger.println("Response status code of the ATC run: " + atcResponse.getStatusLine().getStatusCode());

				if (atcResponse.getStatusLine().getStatusCode() == 200) {
					String responseContent = EntityUtils.toString(atcResponse.getEntity(), "UTF-8");
					AtcCheckResultParser jsonParser = new AtcCheckResultParser(this.treatWarningAtcChecksAsErrors);
					AtcCheckResult atcCheckResult = jsonParser.parseXmlForFailedElements(responseContent);
					numCriticalAtcChecks = atcCheckResult.getNumberOfCriticalAtcChecks();
					logger.println("Number of failed ATC checks: " + numCriticalAtcChecks);
				}
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.println("Http call failed, exception message: " + e.getMessage());
			logger.println("Http call failed, exception stacktrace: " + sw.toString());
			throw new InterruptedException();
		}

		if (numFailedUnitTests > 0 && numCriticalAtcChecks > 0) {
			throw new AbortException("~~~~ Failed unit tests and ATC checks ~~~~");
		}

		if (numFailedUnitTests > 0) {
			throw new AbortException("~~~~ Failed unit tests ~~~~");
		}

		if (numCriticalAtcChecks > 0) {
			throw new AbortException("~~~~ Failed ATC checks ~~~~");
		}
	}

	private boolean validateServerConfiguration(SAPSystem sapSystem) {
		if (StringUtils.isEmpty(sapSystem.getSapServername())) {
			return false;
		}
		;

		if (StringUtils.isEmpty(sapSystem.getSapMandant())) {
			return false;
		}

		if (StringUtils.isEmpty(sapSystem.getSapProtocol())) {
			return false;
		}

		if (StringUtils.isEmpty(sapSystem.getSapUsername())) {
			return false;
		}

		if (sapSystem.getSapPort() == 0) {
			return false;
		}

		return true;
	}

	private SAPSystem getSapSystemFromConfig(String sapSystemString) {
		AbapCiGlobalConfiguration globalConfiguration = AbapCiGlobalConfiguration.get();
		String sapSystemFromTask = getSapSystem();
		List<SAPSystem> configuredSAPSystems = globalConfiguration.getSapSystems();

		SAPSystem sapSystem = configuredSAPSystems.stream()
				.filter(system -> sapSystemFromTask.equals(system.getSapServername() + ":" + system.getSapPort()))
				.findAny().orElse(null);

		return sapSystem;
	}

	@Symbol("abapCi")
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

		public FormValidation doCheckAbapPackagename(@QueryParameter String value)
				throws IOException, ServletException {
			if (value.length() == 0) {
				return FormValidation.error("Please set a package name");
			}

			return FormValidation.ok();
		}

		public ListBoxModel doFillSapSystemItems() {
			ListBoxModel model = new ListBoxModel();
			AbapCiGlobalConfiguration globalConfiguration = AbapCiGlobalConfiguration.get();
			List<SAPSystem> sapSystems = globalConfiguration.getSapSystems();

			for (SAPSystem system : sapSystems) {
				model.add(system.getSapProtocol() + "://" + system.getSapServername() + ":" + system.getSapPort(),
						system.getSapServername() + ":" + system.getSapPort());
			}

			return model;
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
