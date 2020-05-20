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

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Andreas Gautsch
 */
public class JsonParserTest {

    @Test
    public void SimpleUnittestresultOkTest() throws IOException {
        String testresult = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<aunit:runResult xmlns:aunit=\"http://www.sap.com/adt/aunit\">"
                + "<alerts/>"
                + "<program adtcore:uri=\"/sap/bc/adt/oo/classes/zcl_testclass\" adtcore:type=\"CLAS/OC\" adtcore:name=\"ZCL_TESTCLASS\" adtcore:packageName=\"ZTESTPACKAGE\" xmlns:adtcore=\"http://www.sap.com/adt/core\">"
                + "<alerts/>"
                + "<testClasses/>"
                + "</program>"
                + "</aunit:runResult>";

        UnittestResultParser jsonParser = new UnittestResultParser();
        int failedUnittests = jsonParser.parseXmlForFailedUnittests(testresult);
        Assert.assertEquals(0, failedUnittests);

    }

    @Test
    public void SimpleUnittestresultFailedTest() throws IOException {
        String testresult = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<aunit:runResult xmlns:aunit=\"http://www.sap.com/adt/aunit\">"
                + "<alerts/>"
                + "<program adtcore:uri=\"/sap/bc/adt/oo/classes/zcl_truck_loader\" adtcore:type=\"CLAS/OC\">"
                + "<alerts/>"
                + " <testClasses>"
                + "<testClass adtcore:uri=\"/sap/bc/adt/oo/classes/zcl_truck_loader/includes/testclasses#start=19,6\">"
                + "<alerts/>"
                + "<testMethods>"
                + "<testMethod adtcore:uri=\"/sap/bc/adt/oo/classes/zcl_truck_loader/includes/testclasses#start=35,9\" >"
                + "<alerts>"
                + "<alert kind=\"failedAssertion\" severity=\"critical\">"
                + "<title>"
                + "Critical Assertion Error: 'Big Truck - max product size not reached '</title><details><detail text=\"Expected false\">"
                + "<link rel=\"\"/>"
                + "</detail>"
                + "<detail text=\"Test 'LTCL_TRUCK_LOADER-&gt;LOAD_TRUCK_TEST' in Main Program 'ZCL_TRUCK_LOADER==============CP'.\">"
                + "<link rel=\"\"/>"
                + "</detail>"
                + "</details>"
                + "<stack>"
                + "<stackEntry adtcore:uri=\"/sap/bc/adt/oo/classes/zcl_truck_loader/includes/testclasses#start=39,0\"/>"
                + "</stack>"
                + "</alert>"
                + "</alerts>"
                + "</testMethod>"
                + "</testMethods>"
                + "</testClass>"
                + "</testClasses>"
                + "</program>"
                + "</aunit:runResult>";

        UnittestResultParser jsonParser = new UnittestResultParser();
        int failedUnittests = jsonParser.parseXmlForFailedUnittests(testresult);
        Assert.assertEquals(1, failedUnittests);

    }
}
