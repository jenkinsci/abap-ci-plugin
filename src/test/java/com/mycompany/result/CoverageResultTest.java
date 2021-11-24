package com.mycompany.result;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

/**
 *
 * @author Andreas Gautsch
 */
public class CoverageResultTest {

	@Test
	public void setZerosWithNoExceptions() {
		CoverageResult cut = new CoverageResult(0, 0, 0, 0, 0, 0);

		assertEquals(0, cut.getBranchCoverage(), 0);
		assertEquals(0, cut.getProcedureCoverage(), 0);
		assertEquals(0, cut.getStatementCoverage(), 0);
	}

	@Test
	public void setZerosIfTotalsAreZero() {
		CoverageResult cut = new CoverageResult(1, 0, 2, 0, 3, 0);

		assertEquals(0, cut.getBranchCoverage(), 0);
		assertEquals(0, cut.getProcedureCoverage(), 0);
		assertEquals(0, cut.getStatementCoverage(), 0);
	}

	@Test
	public void setPercentages() {
		CoverageResult cut = new CoverageResult(1, 3, 120, 180, 7, 7);

		assertEquals("33.33", String.format(Locale.ENGLISH, "%.2f",cut.getBranchCoverage()));
		assertEquals("66.67", String.format(Locale.ENGLISH,"%.2f",cut.getProcedureCoverage()));
		assertEquals("100.00", String.format(Locale.ENGLISH, "%.2f", cut.getStatementCoverage()));
	}
}
