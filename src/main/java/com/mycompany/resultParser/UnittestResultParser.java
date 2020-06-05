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

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.json.XML;
import java.io.IOException;
import org.json.JSONArray;

/**
 *
 * @author Andreas Gautsch
 */
public class UnittestResultParser {

    public int parseXmlForFailedElements(String xml) throws IOException {
        int numFailedUnittests = 0;

        JSONObject jsonObject = XML.toJSONObject(xml);

        JSONObject aUnitJsonObject = jsonObject.getJSONObject("aunit:runResult");

        if (aUnitJsonObject.has("alerts")) {
            JSONObject alertsJsonObject = aUnitJsonObject.optJSONObject("alerts");
            if (alertsJsonObject != null && !alertsJsonObject.isEmpty()) {
                numFailedUnittests += GetNumberOfFailedTests(alertsJsonObject);
            }

            JSONObject programJsonObject = aUnitJsonObject.optJSONObject("program");
            if (programJsonObject != null && !programJsonObject.isEmpty()) {
                {
                    String alertContentNested = programJsonObject.getString("alerts");
                    if (!StringUtils.isEmpty(alertContentNested)) {
                        numFailedUnittests++;
                    }
                }

                if (programJsonObject.has("testClasses")) {
                    Object testClassesJsonObject = programJsonObject.get("testClasses");

                    if (testClassesJsonObject instanceof JSONObject) {
                        JSONObject testClassJsonObject = ((JSONObject) testClassesJsonObject).getJSONObject("testClass");
                        JSONObject testMethods = testClassJsonObject.getJSONObject("testMethods");
                        JSONObject testMethod = testMethods.optJSONObject("testMethod");
                        if (testMethod != null && !testMethod.isEmpty()) {
                            if (testMethod.has("alerts")) {
                                JSONObject testMethodAlerts = testMethod.optJSONObject("alerts");
                                if (testMethodAlerts != null && !testMethodAlerts.isEmpty()) {
                                    numFailedUnittests++;
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
               return numFailedUnittests;
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

}
