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
import org.apache.http.util.EntityUtils;

import hudson.model.TaskListener;

/**
 *
 * @author Andreas Gautsch
 */
public class AtcHttpPostHandler extends AHttpPostHandler {
	private String atcVariant;

	public AtcHttpPostHandler(SapConnectionInfo sapConnectionInfo, String sapPackageName, TaskListener listener,
			String atcVariant) {
		super(sapConnectionInfo, sapPackageName, listener);
		this.atcVariant = atcVariant;
	}

	@Override
	protected HttpResponse postRequest(AdtInitialConnectionReponseHeaders adtInitialConnectionResponseHeaders)
			throws MalformedURLException, IOException {
		HttpClient httpclientCheckVariant = new DefaultHttpClient();

		String url = BuildHttpUrl(GetMainUrlTarget());
		HttpPost httppost = new HttpPost(url);
		AddHeaderForHttpPostRequest(httppost, adtInitialConnectionResponseHeaders);

		HttpResponse checkVariantResponse = httpclientCheckVariant.execute(httppost);
		String worklistId = EntityUtils.toString(checkVariantResponse.getEntity(), "UTF-8");

		HttpClient httpclientAtcRun = new DefaultHttpClient();

		String urlAtcRunResult = BuildHttpUrl(GetAtcRunResultUrlTarget(worklistId));
		HttpPost httppostAtcRun = new HttpPost(urlAtcRunResult);
		AddHeaderForHttpPostRequest(httppostAtcRun, adtInitialConnectionResponseHeaders);

		String postMessageAtcRunResult = GetPostMessage();
		httppostAtcRun.setEntity(new ByteArrayEntity(postMessageAtcRunResult.getBytes("UTF8")));

		return httpclientAtcRun.execute(httppostAtcRun);

	}

	@Override
	String GetMainUrlTarget() {
		return "/sap/bc/adt/atc/worklists?checkVariant=" + this.atcVariant;
	}

	@Override
	String GetTokenUrlTarget() {
		return "/sap/bc/adt/atc/worklists";
	}

	String GetAtcRunResultUrlTarget(String worklistId) {
		return "/sap/bc/adt/atc/runs?worklistId=" + worklistId;
	}

	@Override
	String GetPostMessage() throws IOException {
		String postMessage = GetRequestMessageXml();
		postMessage = postMessage.replace("{sapPackageName}", _sapPackageName);

		return postMessage;
	}

	private String GetRequestMessageXml() {
		// TODO load content from file, this does not work with the maven released hpi
		// package
		// File file = new
		// File(AtcHttpPostHandler.class.getClassLoader().getResource("atcRequestMessage.xml").getFile());
		// String postMessage = FileUtils.readFileToString(file, "utf-8");

		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<atc:run xmlns:atc=\"http://www.sap.com/adt/atc\" maximumVerdicts=\"100\">"
				+ "<objectSets xmlns:adtcore=\"http://www.sap.com/adt/core\">" + "<objectSet kind=\"inclusive\">"
				+ "<adtcore:objectReferences>"
				+ "<adtcore:objectReference adtcore:uri=\"/sap/bc/adt/vit/wb/object_type/devck/object_name/{sapPackageName}\"/>"
				+ "</adtcore:objectReferences>" + "</objectSet>" + "</objectSets>" + "</atc:run>";

	}

}
