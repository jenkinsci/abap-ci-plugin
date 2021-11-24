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

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import hudson.model.TaskListener;

/**
 *
 * @author Andreas Gautsch
 */
public class UnittestHttpPostHandler extends AHttpPostHandler {
	private boolean withCoverage;

	public UnittestHttpPostHandler(SapConnectionInfo sapConnectionInfo, String sapPackageName, boolean withCoverage,
			TaskListener listener) {
		super(sapConnectionInfo, sapPackageName, listener);
		this.withCoverage = withCoverage;
	}

	@Override
	protected HttpResponse postRequest(AdtInitialConnectionReponseHeaders adtInitialConnectionResponseHeaders)
			throws MalformedURLException, IOException {
		HttpClient httpclient = new DefaultHttpClient();

		String url = BuildHttpUrl(GetMainUrlTarget());

		HttpPost httppost = new HttpPost(url);
		AddHeaderForHttpPostRequest(httppost, adtInitialConnectionResponseHeaders);
		String postMessage = GetPostMessage();
		httppost.setEntity(new ByteArrayEntity(postMessage.getBytes("UTF8")));

		return httpclient.execute(httppost);
	}

	@Override
	String GetMainUrlTarget() {
		return "/sap/bc/adt/abapunit/testruns";
	}

	@Override
	String GetTokenUrlTarget() {
		return GetMainUrlTarget();
	}

	@Override
	String GetPostMessage() throws IOException {
		// File file = new
		// File(classLoader.getResource("unittestRequestMessage.xml").getFile());
		// String postMessage = FileUtils.readFileToString(file, "utf-8");
		String postMessage = GetRequestMessageXml();
		
		postMessage = postMessage.replace("{sapPackageName}", _sapPackageName).replace("{withCoverage}",
				this.withCoverage ? "true" : "false");

		return postMessage;
	}

	private String GetRequestMessageXml() {
		// TODO load content from file, this does not work with the maven released hpi
		// package
		// ClassLoader classLoader = UnittestHttpPostHandler.class.getClassLoader();
		// File file = new
		// File(classLoader.getResource("unittestRequestMessage.xml").getFile());
		// String postMessage = FileUtils.readFileToString(file, "utf-8");
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<aunit:runConfiguration xmlns:aunit=\"http://www.sap.com/adt/aunit\">"
				+ "<external><coverage active=\"{withCoverage}\"/></external>"
				+ "<adtcore:objectSets xmlns:adtcore=\"http://www.sap.com/adt/core\">"
				+ "<objectSet kind=\"inclusive\"><adtcore:objectReferences><adtcore:objectReference adtcore:uri=\"/sap/bc/adt/vit/wb/object_type/devck/object_name/{sapPackageName}\"/>"
				+ "</adtcore:objectReferences>" + "</objectSet>" + "</adtcore:objectSets>"
				+ "</aunit:runConfiguration>";

	}
}
