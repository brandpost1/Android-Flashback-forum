package com.dev.flashback_v04;

import android.content.Context;
import android.util.Xml;

import org.jsoup.nodes.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Viktor on 2013-06-17.
 */
public class XMLParser {

	private Context mContext;
	private ArrayList<Category> categories = null;
	private ArrayList<Forum> mForums = null;
	private Document currentSite = null;

	InputStream in = null;

	/*
	Constructor.
	Context is required to be able to retrieve any local assets from the "assets"-folder.
	 */
	public XMLParser(Context context) {
		mContext = context;

	}

	/*
	Returns an XmlPullParser to use when calling any of the "readXxxx()"-methods.
	 */
	public XmlPullParser getLocalXML(String filename) throws IOException {
		try {
			in = mContext.getAssets().open(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return parser;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		return null;
	}

	/*
	To use: 1. Create an XmlPullParser variable where you intend to call this method.
			2. Call getLocalXML() with the filename of the local xml-file, stored in the "assets"-folder, and assign it to the previously created SmlPullParser variable.
			3. Create an ArrayList<Category> where you intend to call this method and assign this method call to that variable. Also pass the previously created XmlPullParser variable as the argument.
	 */
	public List readCategories(XmlPullParser parser) throws IOException, XmlPullParserException {

			categories = new ArrayList<Category>();


			int eventType = parser.getEventType();

			while(eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:
						String tagName = parser.getName();
						if(tagName.equals("category")) {
							categories.add(new Category());
						} else if(tagName.equals("category_id")) {
							categories.get(categories.size()-1).setID(Integer.parseInt(parser.nextText()));
						} else if(tagName.equals("category_name")) {
							categories.get(categories.size()-1).setName(parser.nextText());
						} else if(tagName.equals("category_link")) {
							categories.get(categories.size()-1).setLink(parser.nextText());
						}

						break;
				}
				eventType = parser.next();

			}

		in.close();
		return categories;
	}

}