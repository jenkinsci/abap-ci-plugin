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
package com.mycompany.result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author Andreas Gautsch
 */
public class UnittestResultParser {

	public UnitTestCheckResult parseUnitTestResult(String xml) throws IOException {
		int numFailedUnittests = 0;
		String coverageResultUri = null;
		List<String> messages = new ArrayList<String>();
		UnitTestCheckResult result = new UnitTestCheckResult();

		JSONObject jsonObject = XML.toJSONObject(xml);
		JSONObject aUnitJsonObject = jsonObject.getJSONObject("aunit:runResult");

		if (aUnitJsonObject.has("alerts")) {
			JSONObject alertsJsonObject = aUnitJsonObject.optJSONObject("alerts");

			if (alertsJsonObject != null && !alertsJsonObject.isEmpty()) {
				numFailedUnittests += GetNumberOfFailedTests(alertsJsonObject);
			}
		}

		if (aUnitJsonObject.has("program")) {
			JSONObject programJsonObject = aUnitJsonObject.optJSONObject("program");

			if (programJsonObject != null && !programJsonObject.isEmpty()) {
				String alertContentNested = programJsonObject.optString("alerts");

				if (!StringUtils.isEmpty(alertContentNested)) {
					numFailedUnittests++;
				}

				if (programJsonObject.has("testClasses")) {
					Object testClassesJsonObject = programJsonObject.get("testClasses");

					if (testClassesJsonObject instanceof JSONObject) {
						JSONObject testClassJsonObject = ((JSONObject) testClassesJsonObject)
								.getJSONObject("testClass");
						JSONObject testMethods = testClassJsonObject.getJSONObject("testMethods");
						JSONObject testMethod = testMethods.optJSONObject("testMethod");

						if (testMethod != null && !testMethod.isEmpty()) {
							if (testMethod.has("alerts")) {
								JSONObject testMethodAlerts = testMethod.optJSONObject("alerts");

								if (testMethodAlerts != null && !testMethodAlerts.isEmpty()) {
									numFailedUnittests++;
									messages.addAll(getTestMethodMessages(testMethodAlerts));
								}
							}
						} else {
							JSONArray testMethodArray = testMethods.optJSONArray("testMethod");
							if (testMethodArray != null) {
								for (int numElement = 0; numElement < testMethodArray.length(); numElement++) {
									JSONObject testMethodArrayObject = testMethodArray.getJSONObject(numElement);

									if (testMethodArrayObject != null && !testMethodArrayObject.isEmpty()) {
										if (testMethodArrayObject.has("alerts")) {
											JSONObject testMethodAlerts = testMethodArrayObject.optJSONObject("alerts");

											if (testMethodAlerts != null && !testMethodAlerts.isEmpty()) {
												numFailedUnittests++;
												messages.addAll(getTestMethodMessages(testMethodAlerts));
											}
										}
									}
								}

							}
						}
					}
				}
			}

		}

		if (aUnitJsonObject.has("external")) {
			JSONObject externalJsonObject = aUnitJsonObject.optJSONObject("external");

			if (externalJsonObject != null && !externalJsonObject.isEmpty()) {
				JSONObject coverageJsonObject = externalJsonObject.optJSONObject("coverage");

				if (coverageJsonObject != null && !coverageJsonObject.isEmpty()) {
					coverageResultUri = coverageJsonObject.getString("adtcore:uri");
				}
			}
		}

		result.setNumOfFailedTests(numFailedUnittests);
		result.setCoverageResultUri(coverageResultUri);
		result.appendMessages(messages);
		return result;
	}

	private int GetNumberOfFailedTests(JSONObject alertsJsonObject) {
		JSONObject alertJsonObject = alertsJsonObject.optJSONObject("alert");

		if (alertJsonObject != null && !alertJsonObject.isEmpty()) {
			if (!"tolerable".equals(alertJsonObject.getString("severity"))) {
				return 1;
			}
		}

		return 0;
	}

	private List<String> getTestMethodMessages(JSONObject testMethodAlerts) {
		List<String> result = new ArrayList<String>();

		if (testMethodAlerts.has("alert")) {
			JSONObject testMethodAlert = testMethodAlerts.optJSONObject("alert");

			if (testMethodAlert != null && !testMethodAlert.isEmpty()) {
				result.add(testMethodAlert.getString("title"));

				JSONObject details = testMethodAlert.optJSONObject("details");

				if (details != null && !details.isEmpty()) {
					JSONArray detailEntries = details.optJSONArray("detail");

					if (detailEntries != null && !detailEntries.isEmpty()) {
						detailEntries.forEach(item -> {
							JSONObject entry = (JSONObject) item;

							if (entry.has("details")) {
								JSONObject innerDetails = entry.optJSONObject("details");
								JSONObject innerDetail = innerDetails.optJSONObject("detail");

								if (innerDetail != null && !innerDetail.isEmpty()) {
									result.add(innerDetail.getString("text"));
								}
							} else {
								result.add(entry.getString("text"));
							}
						});
					}

					if (result.size() > 0) {
						result.add("---------------------------");
					}
				}
			}
		}

		return result;
	}
}
