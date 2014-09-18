package org.bishoph.oxdemo.util;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

public class CustomArrayAdapter extends ArrayAdapter<String> {
	
	public CustomArrayAdapter(Context context, int resource, List<String> objects) {
		super(context, resource, objects);
	}
	
}
