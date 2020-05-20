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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author Andreas Gautsch
 */
public abstract class AHttpPostHandler implements IHttpPostHandler {

    private final SapConnectionInfo _sapConnectionInfo;
    private final String _sapPackageName; 

    public AHttpPostHandler(SapConnectionInfo sapConnectionInfo, String packageName) {
        _sapConnectionInfo = sapConnectionInfo;
        _sapPackageName = packageName; 
    }

    @Override
    public HttpResponse execute() throws MalformedURLException, IOException, HttpCsrfTokenOrCookieCouldNotBeRetrievedException {

        AdtInitialConnectionReponseHeaders adtInitialConnectionResponseHeaders = GetToken();

        if (adtInitialConnectionResponseHeaders.isValid()) 
        {
        return PostRequest(adtInitialConnectionResponseHeaders);
        }
        else 
        {
            throw new HttpCsrfTokenOrCookieCouldNotBeRetrievedException("csrf-token or cookie not valid"); 
        }
    }

    private AdtInitialConnectionReponseHeaders GetToken() throws UnsupportedEncodingException, IOException {
        HttpClient httpclient = new DefaultHttpClient();

        String url = BuildHttpUrl("/sap/bc/adt/abapunit");
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("Authorization", "Basic " + EncodeCredentials());
        httpget.setHeader("x-csrf-token", "fetch");
        HttpResponse res = httpclient.execute(httpget);
        Header tokenHeader = res.getFirstHeader("x-csrf-token");
        Header[] cookieHeaders = res.getHeaders("set-Cookie");

        return new AdtInitialConnectionReponseHeaders(tokenHeader, cookieHeaders);
    }

    private String EncodeCredentials() throws UnsupportedEncodingException {
        return Base64.getEncoder().encodeToString((_sapConnectionInfo.GetSapCredentials().GetUsername() + ":" + _sapConnectionInfo.GetSapCredentials().GetPassword()).getBytes("UTF-8"));
    }

    private HttpResponse PostRequest(AdtInitialConnectionReponseHeaders adtInitialConnectionResponseHeaders) throws MalformedURLException, IOException {
        HttpClient httpclient = new DefaultHttpClient();

        String url = BuildHttpUrl(GetUrlTarget());

        HttpPost httppost = new HttpPost(url);
        httppost.addHeader("Authorization", "Basic " + EncodeCredentials());
        httppost.addHeader("X-CSRF-Token", adtInitialConnectionResponseHeaders.getToken());
        httppost.addHeader("Content-Type", "application/xml");
        for (Header header : adtInitialConnectionResponseHeaders.getCookieHeaders()) {
            httppost.addHeader("Cookie", header.getValue());
        }
        String postMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><aunit:runConfiguration xmlns:aunit=\"http://www.sap.com/adt/aunit\"><external><coverage active=\"false\"/></external><adtcore:objectSets xmlns:adtcore=\"http://www.sap.com/adt/core\"><objectSet kind=\"inclusive\"><adtcore:objectReferences><adtcore:objectReference adtcore:uri=\"/sap/bc/adt/vit/wb/object_type/devck/object_name/"+  _sapPackageName +"\"/></adtcore:objectReferences></objectSet></adtcore:objectSets></aunit:runConfiguration>";
        httppost.setEntity(new ByteArrayEntity(
                postMessage.toString().getBytes("UTF8")));

        return httpclient.execute(httppost);
    }

    private String BuildHttpUrl(String path) {

        return _sapConnectionInfo.GetSapServerInfo().GetProtocol()  + "://" + _sapConnectionInfo.GetSapServerInfo().GetSapServer() + ":" + _sapConnectionInfo.GetSapServerInfo().GetSapPort() + path + "?sap-client=" + _sapConnectionInfo.GetSapServerInfo().GetMandant() + "&sap-language=EN";
    }
    
    abstract String GetUrlTarget();  

}
