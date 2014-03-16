package com.dev.flashback_v04.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dev.flashback_v04.R;
import com.dev.flashback_v04.XMLParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2013-06-17.
 */
public class ShowCategoriesAdapter extends BaseAdapter {

	Context mContext;
	ArrayList<HashMap<String, String>> mCategories;
	XmlPullParser parser;
	XMLParser mParser;
	LayoutInflater mInflater;




	public ShowCategoriesAdapter(Context context) {

		mContext = context;
		mCategories = new ArrayList<HashMap<String, String>>();
		mParser = new XMLParser(context);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		try {
			parser = mParser.getLocalXML("categories_xml.xml");
			mCategories = (ArrayList)mParser.readCategories(parser);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<HashMap<String, String>> getCategories() {
		return mCategories;
	}

	@Override
	public int getCount() {
		return mCategories.size();
	}

	@Override
	public Object getItem(int i) {
		return null;
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		TextView big;

		if(view == null) {
			view = mInflater.inflate(R.layout.category_item,null);
		}
		big = (TextView)view.findViewById(R.id.textView);
		big.setText(mCategories.get(i).get("Name"));

		return view;
	}

}
