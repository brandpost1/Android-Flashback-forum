package com.dev.flashback_v04.fragments.special;

import android.app.Activity;
import android.content.Context;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


import com.dev.flashback_v04.Item_CurrentThreads;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.special.CurrentThreadsAdapter;
import com.dev.flashback_v04.interfaces.OnOptionSelectedListener;


/**
 * Created by Viktor on 2013-11-14.
 */
public class CurrentThreadsFragment extends ListFragment {

    private CurrentThreadsAdapter mAdapter;
    OnOptionSelectedListener mListener;
    TextView header;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment_layout, container, false);
        header = (TextView)view.findViewById(R.id.headerleft);
        header.setText("Aktuella ämnen");
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());
        if(mAdapter == null) {
            mAdapter = new CurrentThreadsAdapter(getActivity());
            setListAdapter(mAdapter);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mListener = (OnOptionSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnOptionSelectedListener");
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.thread_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.gotolastpage:
                int position = info.position;
                SharedPreferences sessionPrefs;
                int posts_per_page;
                String total_thread_posts;
                int replies;
                int numpages;
                String threadname;
                String url;

                try {

                    sessionPrefs = (getActivity()).getSharedPreferences("APP_SETTINGS", Context.MODE_PRIVATE);
                    posts_per_page = sessionPrefs.getInt("Thread_Max_Posts_Page", 12);
                    total_thread_posts = ((Item_CurrentThreads)mAdapter.getItem(position)).mReplies;
                    String temp = "";

                    // Stupid strings with format such as "2 345". Spaces need to be removed. replaceAll() did not work.
                    total_thread_posts = total_thread_posts.replace("\u00A0","");

                    replies = Integer.parseInt(total_thread_posts);
                    threadname = ((Item_CurrentThreads)mAdapter.getItem(position)).mHeadline;

                    numpages = (int) Math.ceil(replies / posts_per_page) + 1;
                    url = ((Item_CurrentThreads)mAdapter.getItem(position)).mLink;

                    Bundle args = new Bundle();
                    args.putInt("NumPages", numpages);
                    args.putString("Url", url);
                    args.putString("ThreadName", threadname);

                    mListener.onOptionSelected(item.getItemId(), args);

                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        SharedPreferences sessionPrefs;
        int posts_per_page;
        String total_thread_posts;
        int replies;
        int numpages;
        String threadname;
        String url;

        try {

            sessionPrefs = (getActivity()).getSharedPreferences("APP_SETTINGS", Context.MODE_PRIVATE);
            posts_per_page = sessionPrefs.getInt("Thread_Max_Posts_Page", 12);
            total_thread_posts = ((Item_CurrentThreads)mAdapter.getItem(position)).mReplies;

            total_thread_posts = total_thread_posts.replace("\u00A0","");

            replies = Integer.parseInt(total_thread_posts.replace(" ", "").toString());
            threadname = ((Item_CurrentThreads)mAdapter.getItem(position)).mHeadline;

            numpages = (int) Math.ceil(replies / posts_per_page) + 1;
            url = ((Item_CurrentThreads)mAdapter.getItem(position)).mLink;

            ((MainActivity)getActivity()).openThread(url, numpages, 0, threadname);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
