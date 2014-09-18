package org.bishoph.oxdemo.util;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class ListEntries  {
	
	ArrayList<AbstractMap.SimpleEntry<Integer,  String>> arraylist = null;
	
	public ListEntries() {
		arraylist = new ArrayList<AbstractMap.SimpleEntry<Integer,  String>>();
	}
	
	public void add(int key, String value) {
		AbstractMap.SimpleEntry<Integer,  String> key_value_object = new AbstractMap.SimpleEntry<Integer, String>(key, value);
		arraylist.add(key_value_object);
	}
	
	public int getObjectId(int index) {
		AbstractMap.SimpleEntry<Integer,  String> key_value_object = (SimpleEntry<Integer, String>) arraylist.get(index);
		return key_value_object.getKey();
	}
	
	public void remove(int index) {
		arraylist.remove(index);
	}
	
}
