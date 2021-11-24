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
 * @author Jacek Wozniczak
 */
public class CoverageHttpPostHandler extends AHttpPostHandler {
	private String coverageResultUri;

	public CoverageHttpPostHandler(SapConnectionInfo sapConnectionInfo, String sapPackageName, String coverageResultUri,
			TaskListener listener) {
		super(sapConnectionInfo, sapPackageName, listener);
		this.coverageResultUri = coverageResultUri;
	}

	@Override
	protected HttpResponse postRequest(AdtInitialConnectionReponseHeaders adtInitialConnectionResponseHeaders)
			throws MalformedURLException, IOException {
		
		HttpClient httpClient = new DefaultHttpClient();
		String url = BuildHttpUrl(GetMainUrlTarget());
		HttpPost httpPost = new HttpPost(url);
		AddHeaderForHttpPostRequest(httpPost, adtInitialConnectionResponseHeaders);
		String postMessage = GetPostMessage();
		httpPost.setEntity(new ByteArrayEntity(postMessage.getBytes("UTF8")));

		return httpClient.execute(httpPost);
	}

	@Override
	String GetMainUrlTarget() {
		return this.coverageResultUri;
	}

	@Override
	String GetTokenUrlTarget() {
		return GetMainUrlTarget();
	}

	@Override
	String GetPostMessage() throws IOException {
		String postMessage = getCoverageMessageXml();
		postMessage = postMessage.replace("{sapPackageName}", _sapPackageName);

		return postMessage;
	}

	private String getCoverageMessageXml() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<cov:query xmlns:cov=\"http://www.sap.com/adt/cov\" xmlns:adtcore=\"http://www.sap.com/adt/core\">"
				+ "<adtcore:objectSets>" + "<objectSet kind=\"inclusive\">" + "<adtcore:objectReferences>"
				+ "<adtcore:objectReference adtcore:uri=\"/sap/bc/adt/vit/wb/object_type/devck/object_name/{sapPackageName}\"/>"
				+ "</adtcore:objectReferences>" + "</objectSet>" + "</adtcore:objectSets>" + "</cov:query>";
	}
}
