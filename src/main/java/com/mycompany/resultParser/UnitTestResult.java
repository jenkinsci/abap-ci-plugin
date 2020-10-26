package com.mycompany.resultParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UnitTestResult {
	private int numOfFailedTests;
	private List<String> messages = new ArrayList<String>();
	
	public void setNumOfFailedTests(int numOfFailedTests) {
		this.numOfFailedTests = numOfFailedTests;
	}

	public void appendMessage(String message) {
		messages.add(message);
	}
	
	public void appendMessages(Collection<String> messages) {
		this.messages.addAll(messages);
	}

	public int getNumOfFailedTests() {
		return numOfFailedTests;
	}

	public List<String> getMessages() {
		return messages;
	}
}
