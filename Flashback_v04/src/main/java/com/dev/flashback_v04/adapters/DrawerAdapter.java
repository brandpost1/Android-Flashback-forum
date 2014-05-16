package com.dev.flashback_v04.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dev.flashback_v04.LoginHandler;
import com.dev.flashback_v04.R;

import java.util.ArrayList;

/**
 * Created by Viktor on 2013-11-19.
 */
public class DrawerAdapter extends BaseAdapter {


    class DrawerItem {
        public DrawerItem(String type, String text) {
            this.type = type;
            this.text = text;
            this.id = -1;
        }
        public DrawerItem(String type, String text, String image, int id) {
            this.type = type;
            this.text = text;
            this.image = image;
            this.id = id;
        }
        public String type = "";
        public String text = "";
        public String image = "";
        public int id;
    }

    static final int DRAWER_DIVIDER = 0;
    static final int DRAWER_ITEM = 1;

    private LayoutInflater mInflater;
    private ArrayList<DrawerItem> mList;

    private Context mContext;

    public DrawerAdapter(Context context) {

        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mList = new ArrayList<DrawerItem>();

        // Since refreshing refuses to work properly

        // Not logged in
        if(!LoginHandler.loggedIn(context)) {
            mList.add(new DrawerItem("Divider", "NAVIGATION"));
            mList.add(new DrawerItem("Item", "Hem", "R.drawable.ic_menu_home", 0));
            mList.add(new DrawerItem("Item", "Aktuella ämnen", "", 1));
            mList.add(new DrawerItem("Item", "Nya ämnen", "", 2));
            mList.add(new DrawerItem("Item", "Nya inlägg", "", 3));
            mList.add(new DrawerItem("Item", "Sök", "", 4));
            mList.add(new DrawerItem("Divider", ""));
            mList.add(new DrawerItem("Item", "Inställningar", "", 9));
            mList.add(new DrawerItem("Item", "Logga in", "", 10));
        }
        // When logged in
        if(LoginHandler.loggedIn(context)) {
            mList.add(new DrawerItem("Divider", "NAVIGATION"));
            mList.add(new DrawerItem("Item", "Hem", "R.drawable.ic_menu_home", 0));
            mList.add(new DrawerItem("Item", "Aktuella ämnen", "", 1));
            mList.add(new DrawerItem("Item", "Nya ämnen", "", 2));
            mList.add(new DrawerItem("Item", "Nya inlägg", "", 3));
            mList.add(new DrawerItem("Item", "Sök", "", 4));
            mList.add(new DrawerItem("Divider", "KONTO"));
            mList.add(new DrawerItem("Item", "Mina inlägg", "", 5));
            mList.add(new DrawerItem("Item", "Mina Ämnen", "", 6));
            mList.add(new DrawerItem("Item", "Citerade inlägg", "", 7));
            mList.add(new DrawerItem("Item", "Prenumerationer", "", 11));
            mList.add(new DrawerItem("Item", "PM", "", 8));
            mList.add(new DrawerItem("Divider", "APP"));
            mList.add(new DrawerItem("Item", "Inställningar", "", 9));
            mList.add(new DrawerItem("Item", "Logga ut", "", 10));
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return 0;
    }

    @Override
    public long getItemId(int i) {
        return mList.get(i).id;
    }

    @Override
    public boolean isEnabled(int position) {
        if(mList.get(position).type.equals("Divider")) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        int type = getItemViewType(i);
        TextView itemText;
        TextView dividerText;

        if(view == null) {
            switch (type) {
                case DRAWER_ITEM:
                    view = mInflater.inflate(R.layout.drawer_item, null);
                    break;
                case DRAWER_DIVIDER:
                    view = mInflater.inflate(R.layout.drawer_divider, null);
                    break;
            }
        }
        switch (type) {
            case DRAWER_ITEM:
                itemText = (TextView)view.findViewById(R.id.item_text);
                itemText.setText(mList.get(i).text);
                break;
            case DRAWER_DIVIDER:
                dividerText = (TextView)view.findViewById(R.id.divider_text);
                dividerText.setText(mList.get(i).text);
                break;
        }
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        DrawerItem item = mList.get(position);
        if(item.type.equals("Divider")) {
            return DRAWER_DIVIDER;
        } else {
            return DRAWER_ITEM;
        }
    }
}
