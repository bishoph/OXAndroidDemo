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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.bishoph.oxdemo.OXDemo;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class CreateTaskAction extends AsyncTask<Object, Object, JSONObject> {

	private OXDemo oxdemo;
	private HttpClient httpclient;
	private HttpContext localcontext;
	private int folder_id = -1;

	public CreateTaskAction(OXDemo oxdemo, HttpClient httpclient, HttpContext localcontext, int folder_id) {
		this.oxdemo = oxdemo;
		this.httpclient = httpclient;
		this.localcontext = localcontext;
		this.folder_id = folder_id;
	}

	@Override
	protected JSONObject doInBackground(Object... params) {
		try {
			String uri = (String)params[0];
			String title = (String)params[1];			
			if (title == null && folder_id > 0) {
				Log.v("OXDemo", "Either title or folder_id missing. Done nothing!");
				return null;
			}
			
			Log.v("OXDemo", "Attempting to create task "+title +" on "+uri);
			
			JSONObject jsonobject = new JSONObject();
			jsonobject.put("folder_id", folder_id); 
			jsonobject.put("title", title);
			jsonobject.put("status", 1);
			jsonobject.put("priority", 0);
			jsonobject.put("percent_completed", 0);
			jsonobject.put("recurrence_type", 0);
			jsonobject.put("private_flag", false);
			jsonobject.put("notification", false);						
			
			Log.v("OXDemo", "Body = "+jsonobject.toString());
		    HttpPut httpput = new HttpPut(uri);
		    StringEntity stringentity = new StringEntity(jsonobject.toString(), HTTP.UTF_8);  
		    stringentity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8")); 
		    httpput.setHeader("Accept", "application/json, text/javascript"); 
		    httpput.setHeader("Content-type", "application/json");
		    httpput.setEntity(stringentity);

		    
		    HttpResponse response = httpclient.execute(httpput, localcontext);
		    Log.v("OXDemo", "Created task "+title);
		    HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			Log.d("OXDemo", "<<<<<<<\n"+result+"\n\n");
			JSONObject jsonObj = new JSONObject(result);
			jsonObj.put("title", title);
			return jsonObj;
		} catch (JSONException e) {
			Log.e("OXDemo", e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}


	@Override
    protected void onPostExecute(JSONObject result) {
    	Log.d("OXDemo", "Post request done");
    	oxdemo.processJSONResult(result, 4);
    }

	
}

