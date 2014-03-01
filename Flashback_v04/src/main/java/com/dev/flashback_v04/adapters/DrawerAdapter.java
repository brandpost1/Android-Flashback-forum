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
        public DrawerItem(String type, String text, String image, int id, boolean loginreq) {
            this.type = type;
            this.text = text;
            this.image = image;
            this.id = id;
            this.requiresLogin = loginreq;
        }
        public String type = "";
        public String text = "";
        public String image = "";
        public int id;
        public boolean requiresLogin;
    }

    static final int DRAWER_DIVIDER = 0;
    static final int DRAWER_ITEM = 1;

    private LayoutInflater mInflater;
    private ArrayList<DrawerItem> mDrawerStrings;

    private Context mContext;

    public DrawerAdapter(Context context) {

        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mDrawerStrings = new ArrayList<DrawerItem>();
        mDrawerStrings.add(new DrawerItem("Divider", "NAVIGATION"));
        mDrawerStrings.add(new DrawerItem("Item", "Hem","R.drawable.ic_menu_home", 0));
        mDrawerStrings.add(new DrawerItem("Item", "Aktuella ämnen","", 1));
        mDrawerStrings.add(new DrawerItem("Item", "Nya ämnen","", 2));
        mDrawerStrings.add(new DrawerItem("Item", "Nya inlägg","", 3));
        //mDrawerStrings.add(new DrawerItem("Item", "Mina inlägg","", 4, true));
        //mDrawerStrings.add(new DrawerItem("Item", "Citerade inlägg","", 5, true));
        mDrawerStrings.add(new DrawerItem("Item", "(Sök)","", 6));
        mDrawerStrings.add(new DrawerItem("Divider", "KONTO"));
        mDrawerStrings.add(new DrawerItem("Item", "Inställningar","", 7));
        if(!LoginHandler.loggedIn(mContext)) {
            mDrawerStrings.add(new DrawerItem("Item", "Logga in","", 8));
        } else {
            mDrawerStrings.add(new DrawerItem("Item", "Logga ut","", 8));
        }


    }

    @Override
    public int getCount() {
        return mDrawerStrings.size();
    }

    @Override
    public Object getItem(int i) {
        return 0;
    }

    @Override
    public long getItemId(int i) {
        return mDrawerStrings.get(i).id;
    }

    @Override
    public boolean isEnabled(int position) {
        if(mDrawerStrings.get(position).type.equals("Divider") || mDrawerStrings.get(position).requiresLogin == true) {
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
                    itemText = (TextView)view.findViewById(R.id.item_text);
                    if(mDrawerStrings.get(i).requiresLogin) {
                        itemText.setTextColor(mContext.getResources().getColor(R.color.Gray));
                    }
                    itemText.setText(mDrawerStrings.get(i).text);
                    break;
                case DRAWER_DIVIDER:
                    view = mInflater.inflate(R.layout.drawer_divider, null);
                    dividerText = (TextView)view.findViewById(R.id.divider_text);
                    dividerText.setText(mDrawerStrings.get(i).text);
                    break;
            }
        }
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        DrawerItem item = mDrawerStrings.get(position);
        if(item.type.equals("Divider")) {
            return DRAWER_DIVIDER;
        } else {
            return DRAWER_ITEM;
        }
    }
}
