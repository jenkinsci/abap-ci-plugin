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

import org.apache.http.Header;

/**
 *
 * @author Andreas Gautsch
 */
public class AdtInitialConnectionReponseHeaders {

    private final Header _tokenHeader;
    private final Header[] _cookieHeaders;

    AdtInitialConnectionReponseHeaders(Header tokenHeader, Header[] cookieHeaders) {
        _tokenHeader = tokenHeader; 
        _cookieHeaders = cookieHeaders; 
    }

    String getToken() {
        return _tokenHeader.getValue(); 
    }

    Header[] getCookieHeaders() {
        return _cookieHeaders; 
    }
    
    boolean isValid() 
    {
        return _tokenHeader != null && _cookieHeaders != null; 
    }
    
}
