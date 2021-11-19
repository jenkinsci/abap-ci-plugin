package com.mycompany.abapci;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class AbapCiBuilderTest {

	@Rule
	public JenkinsRule jenkins = new JenkinsRule();

	final String packageName = "TESTPROJECT";
	final String atcVariant = "DEFAULT";
	final String sapSystemLabel = "LABEL1";

	@Test
	public void testConfigRoundtrip() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		project.getBuildersList().add(new AbapCiBuilder(packageName, atcVariant, sapSystemLabel));
		project = jenkins.configRoundtrip(project);
		jenkins.assertEqualDataBoundBeans(new AbapCiBuilder(packageName, atcVariant, sapSystemLabel),
				project.getBuildersList().get(0));
	}

	public void testBuild() throws Exception {
		FreeStyleProject project = jenkins.createFreeStyleProject();
		AbapCiBuilder builder = new AbapCiBuilder(packageName, atcVariant, sapSystemLabel);
		project.getBuildersList().add(builder);

		FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
		jenkins.assertLogContains("Hello, " + packageName, build);
	}

	public void testScriptedPipeline() throws Exception {
		String agentLabel = "my-agent";
		jenkins.createOnlineSlave(Label.get(agentLabel));
		WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
		String pipelineScript = "node {\n" + "  abapCi '" + packageName + "'\n" + "}";
		job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
		WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
		String expectedString = "Hello, " + packageName + "!";
		jenkins.assertLogContains(expectedString, completedBuild);
	}

}