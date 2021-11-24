package com.mycompany.result;

import static com.adelean.inject.resources.core.InjectResources.resource;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Jacek Wozniczak
 *
 */
public class CoverageResultParserTest {
	private static String xmlResponse;

	@BeforeClass
	public static void loadXmlResponse() {
		xmlResponse = resource().withPath("/com/mycompany/result", "sampleCoverageResult.xml").text();
	}

	@Test
	public void test() throws IOException {
		CoverageResultParser cut = new CoverageResultParser();
		CoverageResult result = cut.parse(xmlResponse);

		assertEquals(50.0, result.getBranchCoverage(), 0);
		assertEquals(50.0, result.getProcedureCoverage(), 0);
		assertEquals(40.0, result.getStatementCoverage(), 0);
	}

}
