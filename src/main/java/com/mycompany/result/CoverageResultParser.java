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
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author Jacek Wozniczak
 */
public class CoverageResultParser {

	public CoverageResult parse(String xml) throws IOException {
		JSONObject jsonObject = XML.toJSONObject(xml);
		JSONObject coverageJsonObject = jsonObject.getJSONObject("cov:result");

		int executedBranch = 0;
		int totalBranch = 0;
		int executedStatement = 0;
		int totalStatement = 0;
		int executedProcedure = 0;
		int totalProcedure = 0;

		if (coverageJsonObject.has("nodes")) {
			JSONObject nodesJsonObject = coverageJsonObject.optJSONObject("nodes");

			if (nodesJsonObject != null && !nodesJsonObject.isEmpty()) {
				// first node is for the package
				JSONObject packageResultObject = nodesJsonObject.optJSONObject("node");

				if (packageResultObject != null && packageResultObject.has("coverages")) {
					JSONObject coveragesObject = packageResultObject.optJSONObject("coverages");

					if (coveragesObject != null && !coveragesObject.isEmpty()) {
						JSONArray coverageArray = coveragesObject.optJSONArray("coverage");
						Iterator<Object> it = coverageArray.iterator();

						while (it.hasNext()) {
							JSONObject coverageObject = (JSONObject) it.next();
							switch (coverageObject.optString("type")) {
							case "branch":
								executedBranch = coverageObject.optInt("executed");
								totalBranch = coverageObject.optInt("total");
								break;
							case "procedure":
								executedProcedure = coverageObject.optInt("executed");
								totalProcedure = coverageObject.optInt("total");
								break;
							case "statement":
								executedStatement = coverageObject.optInt("executed");
								totalStatement = coverageObject.optInt("total");
								break;
							default:
								break;
							}
						}
					}
				}
			}
		}

		return new CoverageResult(executedBranch, totalBranch, executedProcedure, totalProcedure, executedStatement,
				totalStatement);
	}
}
