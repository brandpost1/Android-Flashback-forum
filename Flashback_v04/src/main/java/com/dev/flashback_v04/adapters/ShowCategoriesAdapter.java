package com.dev.flashback_v04.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
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

	private Context mContext;
	private ArrayList<HashMap<String, String>> mCategories;
	private XmlPullParser parser;
	private XMLParser mParser;
	private LayoutInflater mInflater;
	private float categoryTextSize;

	public ShowCategoriesAdapter(Context context) {

		mContext = context;
		mCategories = new ArrayList<HashMap<String, String>>();
		mParser = new XMLParser(context);
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Get textsize value from preferences
		SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		categoryTextSize = Float.parseFloat(appPrefs.getString("category_fontsize", "22"));

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
		ImageView color;

		if(view == null) {
			view = mInflater.inflate(R.layout.category_item, null);
		}
		big = (TextView)view.findViewById(R.id.category_textview);
		color = (ImageView)view.findViewById(R.id.cat_color);

		String colorstring = mCategories.get(i).get("Color");
		int clr = Color.parseColor(colorstring);
		color.setBackgroundColor(clr);
		big.setTextSize(categoryTextSize);
		big.setText(mCategories.get(i).get("Name"));

		return view;
	}

}
