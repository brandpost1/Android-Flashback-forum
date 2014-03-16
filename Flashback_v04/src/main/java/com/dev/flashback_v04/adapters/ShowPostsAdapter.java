package com.dev.flashback_v04.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.drawable.Drawable;

import android.os.Bundle;
import android.preference.PreferenceManager;

import android.text.InputType;
import android.text.Spannable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.dev.flashback_v04.*;
import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.asynctasks.PostsParserTask;
import com.dev.flashback_v04.interfaces.OnTaskComplete;
import com.dev.flashback_v04.interfaces.PostsFragCallback;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Viktor on 2013-06-18.
 */
public class ShowPostsAdapter extends BaseAdapter implements OnTaskComplete {

	static final int HEADER_ROW = 0;
	static final int MESSAGE_ROW = 1;
	static final int QUOTE_ROW = 2;
	static final int DIVIDER_ROW = 3;

    static final int POST_HEADER = 0;
    static final int POST_MESSAGE = 1;
    static final int POST_SPOILER = 2;
    static final int POST_QUOTE_HEADER = 3;
    static final int POST_QUOTE_MESSAGE = 4;
    static final int POST_QUOTE_SPOILER = 5;
    static final int POST_QUOTE_FOOTER = 6;
    static final int POST_FOOTER = 7;
    static final int POST_CODE_HEADER = 8;
    static final int POST_CODE_MESSAGE = 9;
    static final int POST_CODE_FOOTER = 10;
    static final int POST_QUOTE_ANONHEADER = 11;


    static String currentPost = "";

	private Context mContext;
	private PostsParserTask mPostsParserTask;
	private LayoutInflater mInflater;
	ArrayList<Post> mPostArrayList;
	String url;

    boolean[] checked = new boolean[10000];
    boolean[] showSpoiler = new boolean[10000];

	int headers = -1;
    static int count = 0;
	ArrayList<String[]> rows;
    ArrayList<String> quoteText;
	ArrayList<Integer> headersAt;
    PostsFragCallback<Bundle> mCallback;

    public static HashMap<String, String[]> mPlusQuotes = new HashMap<String, String[]>();

	public ShowPostsAdapter(Context context, PostsFragCallback callback, String thread_url) {
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		url = thread_url;
		mContext = context;

        mCallback = callback;


		headersAt = new ArrayList<Integer>();
		rows = new ArrayList<String[]>();
        quoteText = new ArrayList<String>();
		mPostArrayList = new ArrayList<Post>();

		mPostsParserTask = new PostsParserTask(this, context);
		mPostsParserTask.execute(url);
	}

	@Override
	public int getCount() {
		return rows.size();
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
	public int getViewTypeCount() {
		return 12;
	}

    @Override
    public int getItemViewType(int position) {
        if(rows.get(position)[0].equals("[POSTHEADER]")) {
            return POST_HEADER;
        }
        if(rows.get(position)[0].equals("[MESSAGE]")) {
            return POST_MESSAGE;
        }
        if(rows.get(position)[0].equals("[SPOILER]")) {
            return POST_SPOILER;
        }
        if(rows.get(position)[0].equals("[ANONQUOTEHEADER]")) {
            return POST_QUOTE_ANONHEADER;
        }
        if(rows.get(position)[0].equals("[QUOTEHEADER]")) {
            return POST_QUOTE_HEADER;
        }
        if(rows.get(position)[0].equals("[QUOTEMESSAGE]")) {
            return POST_QUOTE_MESSAGE;
        }
        if(rows.get(position)[0].equals("[QUOTESPOILER]")) {
            return POST_QUOTE_SPOILER;
        }
        if(rows.get(position)[0].equals("[QUOTEFOOTER]")) {
            return POST_QUOTE_FOOTER;
        }
        if(rows.get(position)[0].equals("[PHPTAGHEADER]")) {
            return POST_CODE_HEADER;
        }
        if(rows.get(position)[0].equals("[PHPTAGMESSAGE]")) {
            return POST_CODE_MESSAGE;
        }
        if(rows.get(position)[0].equals("[PHPTAGFOOTER]")) {
            return POST_CODE_FOOTER;
        }
        return POST_FOOTER;
    }

	@Override
	public boolean isEnabled(int position) {

		return false;
	}

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        // Header
        TextView author = null;
        TextView usertype = null;
        TextView postnr = null;
        TextView date = null;
        TextView posts = null;
        TextView regdate = null;
        ImageView avatar= null;
        ImageView sharePost = null;
        // Message
        TextView message = null;
        // Quote
        TextView quote = null;
        TextView quotefrom = null;
        Drawable isOnline = null;
        // Spoiler
        TextView spoiler = null;
        // Footer
        ImageView quotePost = null;
        CheckBox plusQuote = null;
        ImageView reportPost = null;
        // Code
        TextView code = null;

        int type = getItemViewType(position);

        if(view == null) {
            switch (type) {
                case POST_HEADER:
                    view = mInflater.inflate(R.layout.post_head, null);
                    break;
                case POST_MESSAGE:
                    view = mInflater.inflate(R.layout.post_message, null);
                    break;
                case POST_SPOILER:
                    view = mInflater.inflate(R.layout.post_spoiler, null);
                    break;
                case POST_QUOTE_ANONHEADER:
                    view = mInflater.inflate(R.layout.post_quote_header_empty, null);
                    break;
                case POST_QUOTE_HEADER:
                    view = mInflater.inflate(R.layout.post_quote_header, null);
                    break;
                case POST_QUOTE_MESSAGE:
                    view = mInflater.inflate(R.layout.post_quote_message, null);
                    break;
                case POST_QUOTE_SPOILER:
                    view = mInflater.inflate(R.layout.post_quote_spoiler, null);
                    break;
                case  POST_QUOTE_FOOTER:
                    view = mInflater.inflate(R.layout.post_quote_footer, null);
                    break;
                case  POST_CODE_HEADER:
                    view = mInflater.inflate(R.layout.post_code_header, null);
                    break;
                case  POST_CODE_MESSAGE:
                    view = mInflater.inflate(R.layout.post_code_message, null);
                    break;
                case  POST_CODE_FOOTER:
                    view = mInflater.inflate(R.layout.post_code_footer, null);
                    break;
                case POST_FOOTER:
                    view = mInflater.inflate(R.layout.post_footer, null);
                    break;
            }
        }

        switch(type) {
            case POST_HEADER:
                author = (TextView)view.findViewById(R.id.user_name);
                usertype = (TextView)view.findViewById(R.id.user_status);
                postnr = (TextView)view.findViewById(R.id.post_nr);
                date = (TextView)view.findViewById(R.id.post_date);
                posts = (TextView)view.findViewById(R.id.user_posts_count);
                regdate = (TextView)view.findViewById(R.id.user_reg_date);
                avatar = (ImageView)view.findViewById(R.id.user_avatar);
                sharePost = (ImageView)view.findViewById(R.id.share_post);

                // Should the "Share"-button show?
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                boolean sharePostEnabled = mPrefs.getBoolean("post_sharebutton", false);
                if(!sharePostEnabled) {
                    sharePost.setVisibility(View.GONE);
                }
                // Set a clicklistener to the sharebutton
                sharePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, mPostArrayList.get(Integer.parseInt(rows.get(position)[2])).getPostUrl());
                        mContext.startActivity(Intent.createChooser(shareIntent, "Dela med"));
                    }
                });

                // Don't remember..
                if(rows.get(position)[2] != null) {
                    author.setText(mPostArrayList.get(Integer.parseInt(rows.get(position)[2])).getAuthor());
                    usertype.setText(mPostArrayList.get(Integer.parseInt(rows.get(position)[2])).getMembertype());
                    postnr.setText(mPostArrayList.get(Integer.parseInt(rows.get(position)[2])).getOrderNr());
                    date.setText(mPostArrayList.get(Integer.parseInt(rows.get(position)[2])).getDate());
                    posts.setText(mPostArrayList.get(Integer.parseInt(rows.get(position)[2])).getNumposts());
                    regdate.setText(mPostArrayList.get(Integer.parseInt(rows.get(position)[2])).getRegdate());



                    if(mPostArrayList.get(Integer.parseInt(rows.get(position)[2])).getOnline().toLowerCase().equals("online")) {
                        avatar.setBackgroundResource(R.drawable.border_avatar_online);
                    } else{
                        avatar.setBackgroundResource(R.drawable.border_avatar_offline);
                    }
                }
                break;
            case POST_MESSAGE:
                message = (TextView)view.findViewById(R.id.post_text);
                message.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                if(rows.get(position)[1] != null) {
                    Spannable smileymessage = ImageAdder.getSmiledText(mContext, rows.get(position)[1]);
                    message.setText(smileymessage);
                }
                break;
            case POST_SPOILER:
                spoiler = (TextView)view.findViewById(R.id.post_spoiler_text_area);
                spoiler.setTag(R.id.SPOILER_MESSAGE, rows.get(position)[1]);
                if(showSpoiler[position] == false) {
                    spoiler.setText("SPOILER - KLICKA FÖR ATT VISA");
                } else {
                    String text = (String)spoiler.getTag(R.id.SPOILER_MESSAGE);
                    Spannable smileyspoiler = ImageAdder.getSmiledText(mContext, text);
                    spoiler.setText(smileyspoiler);
                }
                spoiler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(showSpoiler[position] == false) {
                            String text = (String)view.getTag(R.id.SPOILER_MESSAGE);
                            Spannable smileyspoiler = ImageAdder.getSmiledText(mContext, text);
                            ((TextView)view).setText(smileyspoiler);
                            showSpoiler[position] = true;
                        } else {
                            String text = "SPOILER - KLICKA FÖR ATT VISA";
                            ((TextView)view).setText(text);
                            showSpoiler[position] = false;
                        }
                    }
                });

                break;
            case POST_CODE_MESSAGE:
                code = (TextView)view.findViewById(R.id.post_code_message_content);
                code.setText(rows.get(position)[1]);
                break;
            case POST_QUOTE_ANONHEADER:
                // nothing
                break;
            case POST_QUOTE_HEADER:
                try {
                    quotefrom = (TextView)view.findViewById(R.id.post_quote_from_username);
                    quotefrom.setText(rows.get(position)[1]);
                } catch(NullPointerException e) {

                }
                break;
            case POST_QUOTE_MESSAGE:
                quote = (TextView)view.findViewById(R.id.post_quote_message_content);
                if(rows.get(position)[1] != null) {
                    Spannable smileymessage = ImageAdder.getSmiledText(mContext, rows.get(position)[1]);
                    quote.setText(smileymessage);
                }
                break;
            case POST_QUOTE_SPOILER:
                spoiler = (TextView)view.findViewById(R.id.post_quote_spoiler_text_area);
                spoiler.setTag(R.id.QUOTE_SPOILER_MESSAGE, rows.get(position)[1]);
                if(showSpoiler[position] == false) {
                    spoiler.setText("SPOILER - KLICKA FÖR ATT VISA");
                } else {
                    String text = (String)spoiler.getTag(R.id.QUOTE_SPOILER_MESSAGE);
                    Spannable smileyspoiler = ImageAdder.getSmiledText(mContext, text);
                    spoiler.setText(smileyspoiler);
                }
                spoiler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(showSpoiler[position] == false) {
                            String text = (String)view.getTag(R.id.QUOTE_SPOILER_MESSAGE);
                            Spannable smileyspoiler = ImageAdder.getSmiledText(mContext, text);
                            ((TextView)view).setText(smileyspoiler);
                            showSpoiler[position] = true;
                        } else {
                            String text = "SPOILER - KLICKA FÖR ATT VISA";
                            ((TextView)view).setText(text);
                            showSpoiler[position] = false;
                        }
                    }
                });
                break;
            case  POST_QUOTE_FOOTER:
                // Nothing here atm
                break;
            case POST_FOOTER:
                quotePost = (ImageView)view.findViewById(R.id.quote);
                plusQuote = (CheckBox)view.findViewById(R.id.plusquote);
                reportPost = (ImageView)view.findViewById(R.id.report);

                // Show some Logged-in-only buttons
                if(!LoginHandler.loggedIn(mContext)) {
                    quotePost.setVisibility(View.GONE);
                    plusQuote.setVisibility(View.GONE);
                    reportPost.setVisibility(View.GONE);
                }

                if(rows.get(position)[1] != null) {

                    final String sendnumber = rows.get(position)[4];
                    final String sendauthor = rows.get(position)[3];
                    final String sendquote = rows.get(position)[1]
                        .replace("<br />", "\n")
                        .replace("[IMAGESMILE]", ":)")
                        .replace("[IMAGEYES]", ":yes:")
                        .replace("[IMAGEGRIN]", ":D")
                        .replace("[IMAGENOEXPRESSION]", ":|")
                        .replace("[IMAGESAD]", ":(")
                        .replace("[IMAGEWHOCO5]", ":whoco5:")
                        .replace("[IMAGESKAMSEN]", ":skamsen:")
                        .replace("[IMAGEEVILGRIN39]", ":evilgrin39:")
                        .replace("[IMAGESNEAKY]", ":sneaky:")
                        .replace("[IMAGEWHISTLE]", ":whistle:")
                        .replace("[IMAGEEEK!]", ":eek:")
                        .replace("[IMAGEINNOCENT]", ":innocent:")
                        .replace("[IMAGEANGRY]", ":angry:")
                        .replace("[IMAGEDEVIL]", ":devil:")
                        .replace("[IMAGEEVILMAD]", ":evilmad:")
                        .replace("[IMAGERANT]", ":rant:")
                        .replace("[IMAGETHUMBSUP]", ":thumbsup:")
                        .replace("[IMAGEW00T]", ":w000t:")
                        .replace("[IMAGECRY]", ":'(")
                        .replace("[IMAGEWHINK]", ";)")
                        .replace("[IMAGEOHMY]", ":o")
                        .replace("[IMAGETONGUE]", ":p")
                        .replace("[IMAGESAD44]", ":sad44:")
                        .replace("[IMAGECOOL]", ":cool:")
                        .replace("[IMAGEROLLEYES]", ":rolleyes:")
                        .replace("[IMAGELAUGH]", ":lol:")
                        .replace("[IMAGEUNSURE]", ":unsure:")
                        .replace("[IMAGECONFUSED]", ":confused:")
                        .replace("[IMAGEBOXING]", ":boxing:")
                        .replace("[IMAGEDRUNK]", ":drunk:")
                        .replace("[IMAGENO]", ":no:")
                        .replace("[IMAGESLY]", ":sly:")
                        .replace("[IMAGEBEER]", ":beer:");

                    final String[] arr = {sendauthor, sendquote};

                    quotePost.setTag(R.id.QUOTE_MESSAGE_TAG, rows.get(position)[1]);
                    quotePost.setTag(R.id.QUOTE_AUTHOR_TAG, rows.get(position)[3]);

                    plusQuote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton button, boolean isChecked) {

                            if(isChecked) {
                                checked[position] = true;
                                mPlusQuotes.put(sendnumber, arr);
                            } else {
                                checked[position] = false;
                                mPlusQuotes.remove(sendnumber);
                            }

                        }
                    });

                    plusQuote.setChecked(checked[position]);


                quotePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();

                        bundle.putString("Author", sendauthor);
                        bundle.putString("Quote", sendquote);
                        bundle.putSerializable("PlusQuotes", mPlusQuotes);
                        mCallback.sendQuote(bundle);
                    }
                });
                }
                break;
        }
        return view;
    }

	@Override
	public void updatePosts(ArrayList<Post> posts) {
		mPostArrayList = posts;

        ((MainActivity)mContext).supportInvalidateOptionsMenu();
        String author = "";
        String quote = "";
        for(int i = 0; i < mPostArrayList.size(); i++) {
            for(int j = 0; j < mPostArrayList.get(i).getPostRows().size(); j++) {
                rows.add(mPostArrayList.get(i).getPostRows().get(j));
            }
        }



        for(int i = 0; i < rows.size(); i++) {
            if(rows.get(i)[0].equals("[POSTHEADER]")) {
                headers++;
                rows.get(i)[2] = Integer.toString(headers);
                continue;
            }
            if(rows.get(i)[0].equals("[MESSAGE]")) {
                rows.get(i)[2] = Integer.toString(headers);
                quote += rows.get(i)[1];
                continue;
            }
            if(rows.get(i)[0].equals("[SPOILER]")) {
                rows.get(i)[2] = Integer.toString(headers);
                quote += "\n[SPOILER]" + rows.get(i)[1] + "[/SPOILER]\n";
                continue;
            }
            if(rows.get(i)[0].equals("[ANONQUOTEHEADER]")) {
                rows.get(i)[2] = Integer.toString(headers);
                continue;
            }
            if(rows.get(i)[0].equals("[QUOTEHEADER]")) {
                rows.get(i)[2] = Integer.toString(headers);
                continue;
            }
            if(rows.get(i)[0].equals("[QUOTEMESSAGE]")) {
                rows.get(i)[2] = Integer.toString(headers);
                continue;
            }
            if(rows.get(i)[0].equals("[QUOTESPOILER]")) {
                rows.get(i)[2] = Integer.toString(headers);
                continue;
            }
            if(rows.get(i)[0].equals("[QUOTEFOOTER]")) {
                rows.get(i)[2] = Integer.toString(headers);
                continue;
            }
            if(rows.get(i)[0].equals("[POSTFOOTER]")) {
                rows.get(i)[2] = Integer.toString(headers);
                rows.get(i)[1] = quote;
                quote = "";
                continue;
            }
        }
		notifyDataSetChanged();
	}
}
