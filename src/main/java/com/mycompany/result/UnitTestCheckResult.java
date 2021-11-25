package com.mycompany.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UnitTestCheckResult {
	private int numberOfFailedTests;
	private String sapSystemName;
	private String coverageResultUri;
	private List<String> messages = new ArrayList<String>();

	public void setNumOfFailedTests(int numOfFailedTests) {
		this.numberOfFailedTests = numOfFailedTests;
	}

	public void appendMessage(String message) {
		messages.add(message);
	}

	public void appendMessages(Collection<String> messages) {
		this.messages.addAll(messages);
	}

	public int getNumberOfFailedTests() {
		return numberOfFailedTests;
	}

	public List<String> getMessages() {
		return messages;
	}

	public String getSapSystemName() {
		return sapSystemName;
	}
	
	public void setSapSystemName(String sapSystemName) {
		this.sapSystemName = sapSystemName;
	}

	public boolean isFailed() {
		return numberOfFailedTests > 0;
	}

	public String getCoverageResultUri() {
		return coverageResultUri;
	}

	public void setCoverageResultUri(String coverageResultUri) {
		this.coverageResultUri = coverageResultUri;
	}
	
	public boolean hasCoverageResult() {
		if(this.coverageResultUri != null && this.coverageResultUri.length() > 0) {
			return true;
		}
		
		return false;
	}
}
