/*
 *
 * Date: 20140822
 * Author: Martin Kauss
 * Company: Open-Xchange
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bishoph.oxdemo.util;

import java.io.IOException;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.bishoph.oxdemo.OXDemo;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class GetTaskList extends AsyncTask<Object, Object, JSONObject> {

	private OXDemo oxdemo;
	private HttpClient httpclient;
	private HttpContext localcontext;

	public GetTaskList(OXDemo oxdemo, HttpClient httpclient, HttpContext localcontext) {
		this.oxdemo = oxdemo;
		this.httpclient = httpclient;
		this.localcontext = localcontext;
	}

	@Override
	protected JSONObject doInBackground(Object... params) {
		try {
			String uri = (String)params[0];
			Log.v("OXDemo", "Attempting to get task list "+uri);
		    HttpGet httpget = new HttpGet(uri);  
		    httpget.setHeader("Accept", "application/json, text/javascript");
		    HttpResponse response = httpclient.execute(httpget, localcontext);
		    HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity, HTTP.UTF_8);
			Log.d("OXDemo", "<<<<<<<\n"+result+"\n\n");
			JSONObject jsonObj = new JSONObject(result);
			return jsonObj;
		} catch (JSONException e) {
			Log.e("OXDemo", e.getMessage());
		} catch (ClientProtocolException e) {
			Log.e("OXDemo", e.getMessage());
		} catch (IOException e) {
			Log.e("OXDemo", e.getMessage());
		} 
		return null;
	}

	@Override
    protected void onPostExecute(JSONObject result) {
    	Log.d("OXDemo", "Post request done");
    	oxdemo.processJSONResult(result, 2);
    }
	
}

