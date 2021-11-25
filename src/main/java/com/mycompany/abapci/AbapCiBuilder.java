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
import com.mycompany.abapci.AdtCommunication.CoverageHttpPostHandler;
import com.mycompany.abapci.AdtCommunication.IHttpPostHandler;
import com.mycompany.abapci.AdtCommunication.SapConnectionInfo;
import com.mycompany.abapci.AdtCommunication.SapCredentials;
import com.mycompany.abapci.AdtCommunication.SapServerInfo;
import com.mycompany.abapci.AdtCommunication.UnittestHttpPostHandler;
import com.mycompany.result.AtcCheckResult;
import com.mycompany.result.AtcCheckResultParser;
import com.mycompany.result.CoverageResult;
import com.mycompany.result.CoverageResultParser;
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
	private boolean withCoverage;
	private String atcVariant;
	private boolean treatWarningAtcChecksAsErrors;
	private String sapSystemLabel;

	@DataBoundConstructor
	public AbapCiBuilder(String abapPackagename, String atcVariant, String sapSystemLabel) {
		this.abapPackagename = abapPackagename;

		if (atcVariant == null || atcVariant.length() == 0) {
			this.atcVariant = "DEFAULT";
		} else {
			this.atcVariant = atcVariant;
		}

		this.sapSystemLabel = sapSystemLabel;
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

	public boolean isWithCoverage() {
		return withCoverage;
	}

	@DataBoundSetter
	public void setWithCoverage(boolean withCoverage) {
		this.withCoverage = withCoverage;
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

	public String getSapSystemLabel() {
		return sapSystemLabel;
	}

	@DataBoundSetter
	public void setSapSystemLabel(String sapSystemLabel) {
		this.sapSystemLabel = sapSystemLabel;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException, MalformedURLException {

		PrintStream logger = listener.getLogger();
		SAPSystem sapSystem = getSapSystemFromConfig();

		if (sapSystem == null) {
			throw new AbortException("Could not find the configuration for SAP system " + getSapSystemLabel());
		}

		logger.println("Use Jenkins project name as the package name: " + useJenkinsProjectname);

		if (!validateSapSystemConfiguration(sapSystem)) {
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
			logger.println("# SAP system: " + sapSystem.getLabelAndHost());
			logger.println("###########################################");
			logger.println("Run Unit Test flag is: " + isRunUnitTests());
			logger.println("With coverage flag is: " + isWithCoverage());

			if (isRunUnitTests()) {
				logger.println("~~~~ Start ABAP Unit test run for package: " + abapPackagename + " ~~~~");

				IHttpPostHandler httpPostHandler = new UnittestHttpPostHandler(sapConnectionInfo, abapPackagename,
						withCoverage, listener);
				HttpResponse response = httpPostHandler.executeWithToken();
				// logger.println("Response status code of unit test run: " +
				// response.getStatusLine().getStatusCode());

				if (response.getStatusLine().getStatusCode() == 200) {
					String responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
					UnittestResultParser jsonParser = new UnittestResultParser();
					UnitTestCheckResult unitTestResult = jsonParser.parseUnitTestResult(responseContent);

					if (unitTestResult.getMessages().size() > 0) {
						logger.println("---------------------------");
					}

					unitTestResult.getMessages().forEach(item -> {
						String message = (String) item;
						logger.println(message);
					});

					numFailedUnitTests = unitTestResult.getNumberOfFailedTests();
					logger.println("Number of failed unit tests: " + numFailedUnitTests);

					if (isWithCoverage()) {
						if (unitTestResult.hasCoverageResult()) {
							CoverageHttpPostHandler coveragePostHandler = new CoverageHttpPostHandler(sapConnectionInfo,
									abapPackagename, unitTestResult.getCoverageResultUri(), listener);

							HttpResponse coverageResponse = coveragePostHandler.executeWithToken();
							String coverageResponseContent = EntityUtils.toString(coverageResponse.getEntity(),
									"UTF-8");
							CoverageResult coverageResult = new CoverageResultParser().parse(coverageResponseContent);
							logger.println("~~~~ Coverage results ~~~~");
							logger.println("Statement coverage: "
									+ String.format("%.2f", coverageResult.getStatementCoverage()) + "%");
							logger.println("Branch coverage: "
									+ String.format("%.2f", coverageResult.getBranchCoverage()) + "%");
							logger.println("Procedure coverage: "
									+ String.format("%.2f", coverageResult.getProcedureCoverage()) + "%");
							// logger.println("Response status code of coverage results: "
							// + coverageResponse.getStatusLine().getStatusCode());
						} else {
							logger.println("Coverage results not found in the response...");
						}
					}
				}
			}

			logger.println("Run ATC checks flag is: " + isRunAtcChecks());

			if (isRunAtcChecks()) {
				logger.println("~~~~ Start ATC check run for package: " + abapPackagename + " ~~~~");

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

	private boolean validateSapSystemConfiguration(SAPSystem sapSystem) {
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

	private SAPSystem getSapSystemFromConfig() {
		AbapCiGlobalConfiguration globalConfiguration = AbapCiGlobalConfiguration.get();
		String sapSystemLabel = getSapSystemLabel();
		List<SAPSystem> configuredSAPSystems = globalConfiguration.getSapSystems();

		SAPSystem sapSystem = configuredSAPSystems.stream().filter(system -> sapSystemLabel.equals(system.getLabel()))
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

		public ListBoxModel doFillSapSystemLabelItems() {
			ListBoxModel model = new ListBoxModel();
			AbapCiGlobalConfiguration globalConfiguration = AbapCiGlobalConfiguration.get();
			List<SAPSystem> sapSystems = globalConfiguration.getSapSystems();

			for (SAPSystem system : sapSystems) {
				model.add(system.getLabelAndHost(), system.getLabel());
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
