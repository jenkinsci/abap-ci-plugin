/*
 * The MIT License
 *
 * Copyright 2020 Andreas Gautsch.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mycompany.resultParser;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.mycompany.result.AtcCheckResultParser;
import com.mycompany.result.UnitTestCheckResult;
import com.mycompany.result.UnittestResultParser;

/**
 *
 * @author Andreas Gautsch
 */
public class JsonParserTest {

	@Test
	public void SimpleUnittestresultOkTest() throws IOException {
		String testresult = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<aunit:runResult xmlns:aunit=\"http://www.sap.com/adt/aunit\">" + "<alerts/>"
				+ "<program adtcore:uri=\"/sap/bc/adt/oo/classes/zcl_testclass\" adtcore:type=\"CLAS/OC\" adtcore:name=\"ZCL_TESTCLASS\" adtcore:packageName=\"ZTESTPACKAGE\" xmlns:adtcore=\"http://www.sap.com/adt/core\">"
				+ "<alerts/>" + "<testClasses/>" + "</program>" + "</aunit:runResult>";

		UnittestResultParser jsonParser = new UnittestResultParser();
		int failedUnittests = jsonParser.parseXmlForFailedElements(testresult).getNumberOfFailedTests();
		Assert.assertEquals(0, failedUnittests);

	}

	@Test
	public void SimpleTestReadFile() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("sampleUnittestresult.xml").getFile());
		String str = FileUtils.readFileToString(file, "utf-8");

		UnittestResultParser jsonParser = new UnittestResultParser();
		int failedUnittests = jsonParser.parseXmlForFailedElements(str).getNumberOfFailedTests();
		Assert.assertEquals(0, failedUnittests);

	}

	@Test
	public void SimpleTestAtcResultWithErrors() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("sampleAtcResult.xml").getFile());
		String str = FileUtils.readFileToString(file, "utf-8");

		AtcCheckResultParser jsonParser = new AtcCheckResultParser(true);
		int failedAtcChecks = jsonParser.parseXmlForFailedElements(str).getNumberOfCriticalAtcChecks();
		Assert.assertEquals(1, failedAtcChecks);
	}

	@Test
	public void SimpleAtcResultWithAtcToolFailuresTest() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("sampleAtcResultWithAtcToolFailures.xml").getFile());
		String str = FileUtils.readFileToString(file, "utf-8");

		AtcCheckResultParser jsonParser = new AtcCheckResultParser(true);
		int failedAtcChecks = jsonParser.parseXmlForFailedElements(str).getNumberOfCriticalAtcChecks();
		Assert.assertEquals(82, failedAtcChecks);
	}

	@Test
	public void SimpleTestUnittestResultNoTests() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("unittestResultNoTests.xml").getFile());
		String str = FileUtils.readFileToString(file, "utf-8");

		UnittestResultParser jsonParser = new UnittestResultParser();
		int failedUnittests = jsonParser.parseXmlForFailedElements(str).getNumberOfFailedTests();
		Assert.assertEquals(0, failedUnittests);
	}

	@Test
	public void SimpleTestUnittestResultWithFailure() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("unittestResultWithFailure.xml").getFile());
		String str = FileUtils.readFileToString(file, "utf-8");

		UnittestResultParser jsonParser = new UnittestResultParser();
		int failedUnittests = jsonParser.parseXmlForFailedElements(str).getNumberOfFailedTests();
		Assert.assertEquals(1, failedUnittests);
	}

	@Test
	public void TestUnittestResultForNW752WithFailures() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("unitTestResultForNW752.xml").getFile());
		String str = FileUtils.readFileToString(file, "utf-8");

		UnittestResultParser jsonParser = new UnittestResultParser();
		UnitTestCheckResult unitTestResult = jsonParser.parseXmlForFailedElements(str);
		Assert.assertEquals(2, unitTestResult.getNumberOfFailedTests());
		Assert.assertEquals(9, unitTestResult.getMessages().size());
	}

	@Test
	public void atcResultWithErrorsAndWarningsSkipped() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("sampleAtcResultWithAtcToolFailures.xml").getFile());
		String str = FileUtils.readFileToString(file, "utf-8");

		AtcCheckResultParser jsonParser = new AtcCheckResultParser(false);
		int failedAtcChecks = jsonParser.parseXmlForFailedElements(str).getNumberOfCriticalAtcChecks();
		Assert.assertEquals(28, failedAtcChecks);
	}

}
