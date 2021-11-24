package com.mycompany.result;

/**
 *
 * @author Jacek Wozniczak
 */
public class CoverageResult {
	private double branchCoverage;
	private double statementCoverage;
	private double procedureCoverage;

	public CoverageResult(int executedBranch, int totalBranch, int executedProcedure, int totalProcedure,
			int executedStatement, int totalStatement) {

		this.branchCoverage = executedBranch * 100d / totalBranch;

		if (new Double(this.branchCoverage).isNaN() || Double.isInfinite(this.branchCoverage)) {
			this.branchCoverage = 0;
		}

		this.statementCoverage = executedStatement * 100d / totalStatement;

		if (new Double(this.statementCoverage).isNaN() || Double.isInfinite(this.statementCoverage)) {
			this.statementCoverage = 0;
		}

		this.procedureCoverage = executedProcedure * 100d / totalProcedure;

		if (new Double(this.procedureCoverage).isNaN() || Double.isInfinite(this.procedureCoverage)) {
			this.procedureCoverage = 0;
		}
	}

	public double getBranchCoverage() {
		return branchCoverage;
	}

	public double getStatementCoverage() {
		return statementCoverage;
	}

	public double getProcedureCoverage() {
		return procedureCoverage;
	}

}
