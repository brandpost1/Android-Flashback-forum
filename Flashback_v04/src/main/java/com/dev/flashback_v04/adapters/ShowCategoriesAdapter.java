package com.dev.flashback_v04.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dev.flashback_v04.Category;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.XMLParser;
import com.dev.flashback_v04.activities.MainActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Viktor on 2013-06-17.
 */
public class ShowCategoriesAdapter extends BaseAdapter {

	Context mContext;
	ArrayList<Category> mCategories;
	XmlPullParser parser;
	XMLParser mParser;
	LayoutInflater mInflater;




	public ShowCategoriesAdapter(Context context) {

		mContext = context;
		mCategories = new ArrayList<Category>();
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

	public ArrayList<Category> getCategories() {
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
		big.setText(mCategories.get(i).getName());

        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.abc_fade_in);
        animation.setDuration(1200);
        view.startAnimation(animation);
		return view;
	}
}
