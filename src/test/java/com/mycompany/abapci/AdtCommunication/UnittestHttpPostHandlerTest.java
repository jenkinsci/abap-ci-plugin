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
package com.mycompany.abapci.AdtCommunication;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Andreas Gautsch
 */
public class UnittestHttpPostHandlerTest {

	@Test
	public void postMessageWithoutCoverage() throws IOException {
		SapConnectionInfo sapConnectionInfo = null;
		UnittestHttpPostHandler httpPostHandler = new UnittestHttpPostHandler(sapConnectionInfo, "SAP_TEST_PACKAGE",
				false, null);
		String postMessage = httpPostHandler.GetPostMessage();
		assertNotNull(postMessage);
		assertTrue(postMessage.contains("/sap/bc/adt/vit/wb/object_type/devck/object_name/SAP_TEST_PACKAGE"));
		assertTrue(postMessage.contains("coverage active=\"false\""));
	}
	
	@Test
	public void postMessageWithCoverage() throws IOException {
		SapConnectionInfo sapConnectionInfo = null;
		UnittestHttpPostHandler httpPostHandler = new UnittestHttpPostHandler(sapConnectionInfo, "SAP_TEST_PACKAGE",
				true, null);
		String postMessage = httpPostHandler.GetPostMessage();
		Assert.assertNotNull(postMessage);
		Assert.assertTrue(postMessage.contains("/sap/bc/adt/vit/wb/object_type/devck/object_name/SAP_TEST_PACKAGE"));
		assertTrue(postMessage.contains("coverage active=\"true\""));
	}
}
