package com.mycompany.result;

public class AtcCheckResult {
	private int numberOfCriticalAtcChecks;
	private String sapSystemName;

	public boolean isFailed() {
		return getNumberOfCriticalAtcChecks() > 0;
	}

	public String getSapSystemName() {
		return sapSystemName;
	}

	public int getNumberOfCriticalAtcChecks() {
		return numberOfCriticalAtcChecks;
	}

	public void setNumberOfCriticalAtcChecks(int numberOfCriticalAtcChecks) {
		this.numberOfCriticalAtcChecks = numberOfCriticalAtcChecks;
	}
}
