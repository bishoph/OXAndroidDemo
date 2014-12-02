/*
 * An example App using the Open-Xchange HTTP API as well as
 * Android voice recognition features to perform simple tasks
 * 
 * Features:
 * 
 *  1.) OX Login (Change URL and credentials in settings!)
 *  2.) Get standard task folder
 *  3.) Voice recognition to create simple tasks
 *  4.) Get List of all tasks from standard task folder
 *  5.) Show all tasks in listview
 *  6.) Delete single task via swipe gesture from listview
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

package org.bishoph.oxdemo;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.bishoph.oxdemo.util.CreateTaskAction;
import org.bishoph.oxdemo.util.CustomArrayAdapter;
import org.bishoph.oxdemo.util.DeleteTask;
import org.bishoph.oxdemo.util.GetTaskList;
import org.bishoph.oxdemo.util.ListEntries;
import org.bishoph.oxdemo.util.OXLoginAction;
import org.bishoph.oxdemo.util.RequestVisibleFolders;
import org.bishoph.oxdemo.util.SwipeDismissListViewTouchListener;
import org.bishoph.oxdemo.util.SwipeDismissListViewTouchListener.DismissCallbacks;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class OXDemo extends Activity implements
		OnSharedPreferenceChangeListener {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	private static final String SESSIONID = "session";
	private static final String DATA = "data";
	private static final String PRIVATEFOLDER = "private";
	private String sessionid = null;

	private String url = null;
	private HttpClient httpclient = null;
	private HttpContext localcontext = null;

	private int folder_id = -1;

	private SharedPreferences prefs;

	private ListView listview = null;
	private ArrayList<String> listviewarray = null;
	private CustomArrayAdapter listviewadapter = null;
	private ListEntries listentries = null;

	float lastx = Float.NaN;
	float lasty = Float.NaN;
	static final int MOVEMENT = 35;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_oxdemo);

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		Button button = (Button) findViewById(R.id.start_button);
		// button.setOnTouchListener(mDelayHideTouchListener);

		// Add OnClickListener to button
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startVoiceRecognitionActivity();
			}
		});

		prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(this);

		listentries = new ListEntries();
		listview = (ListView) findViewById(R.id.listView1);	
		
		listviewarray = new ArrayList<String>();
		listviewarray.add("Loading ... please wait");
	
		listviewadapter = new CustomArrayAdapter(this, R.layout.mysimplelistitem, listviewarray);
		
		listview.setAdapter(listviewadapter);

		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				listview, new DismissCallbacks() {
					public void onDismiss(ListView listView,
							int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							Log.v("OXDemo", "Preparing for deletion "
									+ listviewadapter.getItem(position));
							removeTask(position);							
						}
						listviewadapter.notifyDataSetChanged();
					}

					@Override
					public boolean canDismiss(int position) {
						return true;
					}
				});
		listview.setOnTouchListener(touchListener);
		listview.setOnScrollListener(touchListener.makeScrollListener());

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}



	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			doLogin();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	@SuppressLint("TrulyRandom")
	private String getSimpleRandomString() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(64, random).toString(32);
	}

	private void doLogin() {
		Log.v("OXDemo", "doLogin start");
		try {
			url = prefs.getString("url", "https://www.ox.io/");
			Log.v("OXDemo", "Using URL from preferences = " + url);
			String login = prefs.getString("login", "test@example.com");
			Log.v("OXDemo", "Using login from preferences = " + login);
			String password = prefs.getString("password", null);
			if (!url.endsWith("/")) {
				url = url + "/";
			}

			OXLoginAction oxloginaction = new OXLoginAction(this);
			String uri = url + "ajax/login?action=login&authId="
					+ getSimpleRandomString();

			this.httpclient = oxloginaction.getHttpClient();
			this.localcontext = oxloginaction.getLocalContext();
			oxloginaction.execute(uri, login, password);

			Log.v("OXDemo", "doLogin done");
		} catch (Exception e) {
			Log.e("OXDemo", e.getMessage());
		}

	}

	public void getSessionID(JSONObject jsonobject) {
		Log.v("OXDemo", "Got JSON Object from AsyncTask ... ready for parsing "
				+ jsonobject);
		try {
			String sessionid = jsonobject.getString(SESSIONID);
			Log.v("OXDemo", "Sessionid = " + sessionid);
			this.sessionid = sessionid;
			// We can not continue to work with the OX API
			getTaskStandardFolder();

		} catch (JSONException e) {
			Log.e("OXDemo", e.getMessage());
		}
	}

	public void processJSONResult(JSONObject jsonobject, int action) {
		if (action == 1) {
			Log.v("OXDemo",
					"Got JSON Object from AsyncTask ... ready for parsing "
							+ jsonobject);
			try {
				// {"data":{"private":[["27",true]]},"timestamp":1243419226786}
				folder_id = -1;
				JSONObject data = jsonobject.getJSONObject(DATA);
				JSONArray privatefolder = data.getJSONArray(PRIVATEFOLDER);
				for (int i = 0; i < privatefolder.length(); i++) {
					JSONArray folders = (JSONArray) privatefolder.get(i);
					for (int a = 0; a < folders.length() / 2; a = a + 2) {
						folder_id = folders.getInt(a);
						boolean standard = folders.getBoolean(a + 1);
						if (standard == true) {
							Log.v("OXDemo", "Task standard folder_id = "
									+ folder_id);
							// We can now continue to work with the OX API
							getTaskList();
						}
					}
				}
			} catch (JSONException e) {
				Log.e("OXDemo", e.getMessage());
			} catch (Exception e) {
				Log.e("OXDemo", e.getMessage());
			}
		} else if (action == 2) {
			Log.v("OXDemo",
					"Got JSON Object from AsyncTask ... ready for parsing "
							+ jsonobject);
			try {
				// {"data":[[24,"testtask 1234"]],"timestamp":1408961031592}
				JSONArray data = jsonobject.getJSONArray(DATA);
				if (data.length() > 0) {
					listviewarray.remove(0); // Remove "Loading info"
				}
				for (int i = 0; i < data.length(); i++) {
					JSONArray tasks = (JSONArray) data.get(i);
					for (int a = 0; a < tasks.length() / 2; a = a + 2) {
						int object_id = tasks.getInt(a);
						String task_title = tasks.getString(a + 1);
						listentries.add(object_id, task_title);
						listviewarray.add(task_title);
						Log.v("OXDemo", "Task = " + task_title);
						listviewadapter.notifyDataSetChanged();
					}
				}
			} catch (JSONException e) {
				Log.e("OXDemo", e.getMessage());
			} catch (Exception e) {
				Log.e("OXDemo", e.getMessage());
			}
		} else if (action == 3) {
			Log.v("OXDemo", "Task should have been deleted ;)");
		} else if (action == 4) {
			Log.v("OXDemo",
					"Got JSON Object from AsyncTask ... ready for parsing "
							+ jsonobject);
			try {
				// {"timestamp":1409059418429,"data":{"id":35}}
				JSONObject data = jsonobject.getJSONObject(DATA);
				int object_id = (Integer) data.get("id");
				String task_title = jsonobject.getString("title");
				listentries.add(object_id, task_title);
				listviewarray.add(task_title);
				listviewadapter.notifyDataSetChanged();
			} catch (JSONException e) {
				Log.e("OXDemo", e.getMessage());
			} catch (Exception e) {
				Log.e("OXDemo", e.getMessage());
			}
		} else {
			Log.v("OXDemo", "Action unkown...");
		}

	}

	public void createTask(int folder_id, String title) {
		String uri = url + "ajax/tasks?action=new&timezone=UTC&session="
				+ sessionid;
		CreateTaskAction createtaskaction = new CreateTaskAction(this,
				httpclient, localcontext, folder_id);
		createtaskaction.execute(uri, title);
	}

	public void getTaskStandardFolder() {
		String uri = url
				+ "ajax/folders?action=allVisible&content_type=tasks&columns=1,308&session="
				+ sessionid;
		RequestVisibleFolders requestvisiblefolders = new RequestVisibleFolders(
				this, httpclient, localcontext);
		requestvisiblefolders.execute(uri);
	}

	public void getTaskList() {
		String uri = url + "ajax/tasks?action=all&folder=" + folder_id
				+ "&columns=1,200&session=" + sessionid;
		GetTaskList gettasklist = new GetTaskList(this, httpclient,
				localcontext);
		gettasklist.execute(uri);
	}

	public void removeTask(int listviewposition) {
		Log.v("OXDemo", "removeTask " + listviewposition + " was called");
		int object_id = listentries.getObjectId(listviewposition);
		String uri = url + "ajax/tasks?action=delete&folder=" + folder_id
				+ "&timestamp=" + System.currentTimeMillis() + "&session="
				+ sessionid;
		DeleteTask deletetask = new DeleteTask(this, httpclient, localcontext,
				folder_id);
		deletetask.execute(uri, object_id);
		listentries.remove(listviewposition);
		listviewarray.remove(listviewposition);
		listviewadapter.notifyDataSetChanged();
	}

	/*------------------- Voice Recognition -------------------- */

	public void startVoiceRecognitionActivity() {
		Log.v("OXDemo", "startVoiceRecognitionActivity init...");
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
		if (activities.size() > 0) {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
					"Speech recognition demo");
			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
			Log.v("OXDemo", "...done");
		} else {
			// createTask(folder_id,
			// "Auto creation צהü "+getSimpleRandomString());
			Log.v("OXDemo", "No voice recognition!");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v("OXDemo", "onActivityResult " + resultCode + " // " + RESULT_OK);
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			Log.v("OXDemo", "All text reconition: " + matches);
			if (matches != null && !matches.isEmpty()) {
				String bestResult = matches.get(0).toString();
				if (bestResult != null && bestResult.length() > 0) {
					Log.v("OXDemo", "Best result from text reconition: "
							+ bestResult);
					if (folder_id > -1) {
						createTask(folder_id, bestResult);
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/* ---------- Menu -> Settings ----------- */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.menu_settings) {
			Intent settingsActivity = new Intent(getBaseContext(),
					PreferencesActivity.class);
			startActivity(settingsActivity);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		int length = listviewarray.size() - 1;
		for (int a = 0; a < length; a++) {
			listentries.remove(0);
			listviewarray.remove(0);
		}
		listviewadapter.notifyDataSetChanged();
		doLogin();
	}

}
