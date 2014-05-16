package com.dev.flashback_v04.adapters.special;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.fragments.special.MySubscriptionsFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Viktor on 2014-03-20.
 */
public class MySubsAdapter extends BaseAdapter {

	private final LayoutInflater mInflater;
	private Context mContext;
	private ArrayList<HashMap<String, String>> mItems;
	private MySubscriptionsFragment.CheckboxListener mCheckboxListener;

	public MySubsAdapter(Context context, MySubscriptionsFragment.CheckboxListener checkboxListener) {
		mCheckboxListener = checkboxListener;
		mContext = context;
		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mItems = new ArrayList<HashMap<String, String>>();
	}

	public void addItem(HashMap<String, String> itemMap) {
		mItems.add(itemMap);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int i) {
		return mItems.get(i);
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	public ArrayList<HashMap<String, String>> getItems() {
		return mItems;
	}

	public void setItems(ArrayList<HashMap<String, String>> items) {
		mItems = items;
	}

	@Override
	public View getView(final int i, View view, ViewGroup viewGroup) {
		TextView threadName;
		TextView author;
		TextView lastpost;
		TextView monitormode;
		ImageView lastPage;
		CheckBox checkBox;

		if(view == null) {
			view = mInflater.inflate(R.layout.subs_item_thread, null);
		}
		threadName = (TextView)view.findViewById(R.id.threadTitle);
		author = (TextView)view.findViewById(R.id.threadAuthor);
		lastpost = (TextView)view.findViewById(R.id.lastPost);
		monitormode = (TextView)view.findViewById(R.id.monitoring);
		lastPage = (ImageView)view.findViewById(R.id.subs_gotolastpage);

		checkBox = (CheckBox)view.findViewById(R.id.sub_checkbox);

		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
				mCheckboxListener.onChange(compoundButton, i, checked, mItems.get(i).get("Checkbox"));
				if(checked) {
					mItems.get(i).put("Checked", "1");
				} else {
					mItems.get(i).put("Checked", "0");
				}
			}
		});

		if(mItems.get(i).get("Checked").equals("1")) {
			checkBox.setChecked(true);
		} else {
			checkBox.setChecked(false);
		}
		if(mItems.get(i).get("BoldTitle").equals("true")) {
			threadName.setTypeface(Typeface.DEFAULT_BOLD);
		} else {
			threadName.setTypeface(Typeface.DEFAULT);
		}

		lastPage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String url = mItems.get(i).get("Link");
				String title = mItems.get(i).get("Title");
				String temp = mItems.get(i).get("LastPage");
				int pageNumber;
				try {
					pageNumber = Integer.parseInt(temp);
				} catch (NumberFormatException e) {
					pageNumber = 1;
				}

				((MainActivity)mContext).openThread(url, pageNumber, title);
			}
		});

		threadName.setText(mItems.get(i).get("Title"));
		author.setText(mItems.get(i).get("User"));
		lastpost.setText(mItems.get(i).get("LastPost"));
		monitormode.setText(mItems.get(i).get("Monitoring"));
		return view;
	}
}
