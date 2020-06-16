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

import hudson.Launcher;
import hudson.model.TaskListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author Andreas Gautsch
 */
public abstract class AHttpPostHandler implements IHttpPostHandler {

    private final SapConnectionInfo _sapConnectionInfo;
    protected final String _sapPackageName;
    protected final TaskListener _listener; 

    public AHttpPostHandler(SapConnectionInfo sapConnectionInfo, String packageName, TaskListener listener) {
        _sapConnectionInfo = sapConnectionInfo;
        _sapPackageName = packageName;
        _listener = listener; 
    }

    abstract HttpResponse postRequest(AdtInitialConnectionReponseHeaders adtInitialConnectionResponseHeaders) throws MalformedURLException, IOException;

    abstract String GetTokenUrlTarget(); 
    abstract String GetMainUrlTarget();

    abstract String GetPostMessage() throws IOException;

    @Override
    public HttpResponse executeWithToken() throws MalformedURLException, IOException, HttpCsrfTokenOrCookieCouldNotBeRetrievedException {

        AdtInitialConnectionReponseHeaders adtInitialConnectionResponseHeaders = GetToken();

        if (adtInitialConnectionResponseHeaders.isValid()) {
            return postRequest(adtInitialConnectionResponseHeaders);
        } else {
            throw new HttpCsrfTokenOrCookieCouldNotBeRetrievedException("csrf-token or cookie not valid");
        }
    }

    @Override
    public HttpResponse execute() throws MalformedURLException, IOException {
        return postRequest(null);
    }

    private AdtInitialConnectionReponseHeaders GetToken() throws UnsupportedEncodingException, IOException {

        HttpClient httpclient = new DefaultHttpClient();


        String url = BuildHttpUrl(GetTokenUrlTarget());
        
        _listener.getLogger().println("ConnectionResponseHeader Token Url: " + url);
        HttpGet httpget = new HttpGet(url);

        httpget.setHeader("Authorization", "Basic " + EncodeCredentials());
        httpget.setHeader("x-csrf-token", "fetch");
        HttpResponse res = httpclient.execute(httpget);
        _listener.getLogger().println("ConnectionResponseHeader Token StatusCode: " + res.getStatusLine().getStatusCode());
        Header tokenHeader = res.getFirstHeader("x-csrf-token");
        Header[] cookieHeaders = res.getHeaders("set-Cookie");

        return new AdtInitialConnectionReponseHeaders(tokenHeader, cookieHeaders);
    }

    private String EncodeCredentials() throws UnsupportedEncodingException {
        return Base64.getEncoder().encodeToString((_sapConnectionInfo.GetSapCredentials().GetUsername() + ":" + _sapConnectionInfo.GetSapCredentials().GetPassword().getPlainText()).getBytes("UTF-8"));
    }

    protected String BuildHttpUrl(String path) {

        String appendCharacter = path.contains("?") ?  "&" : "?"; 
        return _sapConnectionInfo.GetSapServerInfo().GetProtocol() + "://" + _sapConnectionInfo.GetSapServerInfo().GetSapServer() + ":" + _sapConnectionInfo.GetSapServerInfo().GetSapPort() + path + appendCharacter + "sap-client=" + _sapConnectionInfo.GetSapServerInfo().GetMandant() + "&sap-language=EN";
    }

    protected void AddHeaderForHttpPostRequest(HttpPost httppost, AdtInitialConnectionReponseHeaders adtInitialConnectionResponseHeaders) throws UnsupportedEncodingException {
        httppost.addHeader("Authorization", "Basic " + EncodeCredentials());
        httppost.addHeader("Content-Type", "application/xml");
        if (adtInitialConnectionResponseHeaders != null) {
            httppost.addHeader("X-CSRF-Token", adtInitialConnectionResponseHeaders.getToken());
            for (Header header : adtInitialConnectionResponseHeaders.getCookieHeaders()) {
                httppost.addHeader("Cookie", header.getValue());
            }
        }
    }

}
