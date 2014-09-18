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
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.bishoph.oxdemo.OXDemo;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class OXLoginAction extends AsyncTask<Object, Object, JSONObject> {

	private HttpClient httpclient = null;
	private HttpContext localcontext = null;
	private OXDemo oxdemo;
	
	public OXLoginAction(OXDemo oxdemo) {
		this.oxdemo = oxdemo;
		if (httpclient == null) {
			 CookieStore cookieStore = new BasicCookieStore();
			 HttpParams params = new BasicHttpParams();
			 HttpProtocolParams.setContentCharset(params, "UTF-8");
			 httpclient = new DefaultHttpClient(params); 
			 localcontext = new BasicHttpContext();
			 localcontext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		}
	}

	public HttpClient getHttpClient()  {
		return httpclient;
	}
	
	public HttpContext getLocalContext() {
		return localcontext;
	}
	
	@Override
	protected JSONObject doInBackground(Object... params) {
		HttpPost httppost = new HttpPost((String) params[0]);
		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			
			nameValuePairs.add(new BasicNameValuePair("name", (String) params[1]));
			nameValuePairs.add(new BasicNameValuePair("password", (String) params[2]));
			nameValuePairs.add(new BasicNameValuePair("client", "OXDemo client"));

			
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			Log.d("OXDemo", "Execute post request "+params[0]);
			
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost, localcontext);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			Log.d("OXDemo", "<<<<<<<\n"+result+"\n\n");
			JSONObject jsonObj = new JSONObject(result);
			return jsonObj;
		} catch (ClientProtocolException e) {
			Log.e("OXDemo", e.getMessage());
		} catch (IOException e) {
			Log.e("OXDemo", e.getMessage());
		} catch (JSONException e) {
			Log.e("OXDemo", e.getMessage());
		}
		return null;
	}

	@Override
    protected void onPostExecute(JSONObject result) {
    	Log.d("OXDemo", "Post request done");
    	oxdemo.getSessionID(result);
    }	

}
