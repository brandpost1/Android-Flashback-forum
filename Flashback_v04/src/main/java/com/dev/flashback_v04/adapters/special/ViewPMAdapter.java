package com.dev.flashback_v04.adapters.special;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.dev.flashback_v04.ImageAdder;
import com.dev.flashback_v04.Post;
import com.dev.flashback_v04.R;
import com.dev.flashback_v04.activities.MainActivity;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Viktor on 2014-03-21.
 */
public class ViewPMAdapter extends BaseAdapter {

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

	private final LayoutInflater mInflater;

	int headers = -1;
	private Post mMessage;
	ArrayList<String[]> mMessageRows;
	private Context mContext;
	private boolean[] showSpoiler = new boolean[100];


	public ViewPMAdapter(Context context)  {
		mContext = context;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMessageRows = new ArrayList<String[]>();
	}

	@Override
	public int getCount() {
		return mMessageRows.size();
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
	public int getItemViewType(int position) {

		if(mMessageRows.get(position)[0].equals("[POSTHEADER]")) {
			return POST_HEADER;
		}
		if(mMessageRows.get(position)[0].equals("[MESSAGE]")) {
			return POST_MESSAGE;
		}
		if(mMessageRows.get(position)[0].equals("[SPOILER]")) {
			return POST_SPOILER;
		}
		if(mMessageRows.get(position)[0].equals("[ANONQUOTEHEADER]")) {
			return POST_QUOTE_ANONHEADER;
		}
		if(mMessageRows.get(position)[0].equals("[QUOTEHEADER]")) {
			return POST_QUOTE_HEADER;
		}
		if(mMessageRows.get(position)[0].equals("[QUOTEMESSAGE]")) {
			return POST_QUOTE_MESSAGE;
		}
		if(mMessageRows.get(position)[0].equals("[QUOTESPOILER]")) {
			return POST_QUOTE_SPOILER;
		}
		if(mMessageRows.get(position)[0].equals("[QUOTEFOOTER]")) {
			return POST_QUOTE_FOOTER;
		}
		if(mMessageRows.get(position)[0].equals("[PHPTAGHEADER]")) {
			return POST_CODE_HEADER;
		}
		if(mMessageRows.get(position)[0].equals("[PHPTAGMESSAGE]")) {
			return POST_CODE_MESSAGE;
		}
		if(mMessageRows.get(position)[0].equals("[PHPTAGFOOTER]")) {
			return POST_CODE_FOOTER;
		}
		return POST_FOOTER;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	@Override
	public int getViewTypeCount() {
		return 12;
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

				view.findViewById(R.id.dropdown_indicator).setVisibility(View.GONE);
				postnr.setVisibility(View.GONE);
				sharePost.setVisibility(View.GONE);
				sharePost.setVisibility(View.GONE);

				String avatarUrl = mMessage.getAvatarUrl();

				if(avatarUrl.isEmpty()) {
					Picasso.with(mContext)
							.load(R.drawable.ic_contact_picture)
							.error(R.drawable.ic_contact_picture)
							.resize(90, 90)
							.centerCrop()
							.placeholder(R.drawable.ic_contact_picture)
							.into(avatar);
				} else {
					Picasso.with(mContext)
							.load(avatarUrl)
							.error(R.drawable.ic_contact_picture)
							.resize(90, 90)
							.centerCrop()
							.placeholder(R.drawable.ic_contact_picture)
							.into(avatar);
				}

				// Don't remember..
				if(mMessageRows.get(position)[2] != null) {
					author.setText(mMessage.getAuthor());
					usertype.setText(mMessage.getMembertype());
					postnr.setText(mMessage.getOrderNr());
					date.setText(mMessage.getDate());
					posts.setText(mMessage.getNumposts());
					regdate.setText(mMessage.getRegdate());


					if(mMessage.getOnline().toLowerCase().equals("online")) {
						avatar.setBackgroundResource(R.drawable.border_avatar_online);
					} else{
						avatar.setBackgroundResource(R.drawable.border_avatar_offline);
					}
				}
				break;
			case POST_MESSAGE:
				message = (TextView)view.findViewById(R.id.post_text);
				message.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
				if(mMessageRows.get(position)[1] != null) {
					Spannable smileymessage = ImageAdder.getSmiledText(mContext, mMessageRows.get(position)[1]);
					message.setText(smileymessage);
					message.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					message.setSingleLine(false);
				}
				break;
			case POST_SPOILER:
				spoiler = (TextView)view.findViewById(R.id.post_spoiler_text_area);
				spoiler.setTag(R.id.SPOILER_MESSAGE, mMessageRows.get(position)[1]);
				if(showSpoiler[position] == false) {
					spoiler.setText("SPOILER - KLICKA FÖR ATT VISA");
				} else {
					String text = (String)spoiler.getTag(R.id.SPOILER_MESSAGE);
					Spannable smileyspoiler = ImageAdder.getSmiledText(mContext, text);
					spoiler.setText(smileyspoiler);
					spoiler.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					spoiler.setSingleLine(false);
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
				code.setText(mMessageRows.get(position)[1]);
				break;
			case POST_QUOTE_ANONHEADER:
				// nothing
				break;
			case POST_QUOTE_HEADER:
				try {
					quotefrom = (TextView)view.findViewById(R.id.post_quote_from_username);
					quotefrom.setText(mMessageRows.get(position)[1]);
				} catch(NullPointerException e) {

				}
				break;
			case POST_QUOTE_MESSAGE:
				quote = (TextView)view.findViewById(R.id.post_quote_message_content);
				if(mMessageRows.get(position)[1] != null) {
					Spannable smileymessage = ImageAdder.getSmiledText(mContext, mMessageRows.get(position)[1]);
					quote.setText(smileymessage);
					quote.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					quote.setSingleLine(false);
				}
				break;
			case POST_QUOTE_SPOILER:
				spoiler = (TextView)view.findViewById(R.id.post_quote_spoiler_text_area);
				spoiler.setTag(R.id.QUOTE_SPOILER_MESSAGE, mMessageRows.get(position)[1]);
				if(showSpoiler[position] == false) {
					spoiler.setText("SPOILER - KLICKA FÖR ATT VISA");
				} else {
					String text = (String)spoiler.getTag(R.id.QUOTE_SPOILER_MESSAGE);
					Spannable smileyspoiler = ImageAdder.getSmiledText(mContext, text);
					spoiler.setText(smileyspoiler);

					spoiler.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					spoiler.setSingleLine(false);
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

				// Hide some buttons
				quotePost.setVisibility(View.GONE);
				plusQuote.setVisibility(View.GONE);
				reportPost.setVisibility(View.GONE);
				break;
		}

		return view;
	}

	public void setData(ArrayList<Post> data) {
		if(data.size() > 0) {
			mMessage = data.get(0);
			String author = "";
			String quote = "";

			for(int j = 0; j < mMessage.getPostRows().size(); j++) {
				mMessageRows.add(mMessage.getPostRows().get(j));
			}

			for(int i = 0; i < mMessageRows.size(); i++) {
				if(mMessageRows.get(i)[0].equals("[POSTHEADER]")) {
					headers++;
					mMessageRows.get(i)[2] = Integer.toString(headers);
					continue;
				}
				if(mMessageRows.get(i)[0].equals("[MESSAGE]")) {
					mMessageRows.get(i)[2] = Integer.toString(headers);
					quote += mMessageRows.get(i)[1];
					continue;
				}
				if(mMessageRows.get(i)[0].equals("[SPOILER]")) {
					mMessageRows.get(i)[2] = Integer.toString(headers);
					quote += "\n[SPOILER]" + mMessageRows.get(i)[1] + "[/SPOILER]\n";
					continue;
				}
				if(mMessageRows.get(i)[0].equals("[ANONQUOTEHEADER]")) {
					mMessageRows.get(i)[2] = Integer.toString(headers);
					continue;
				}
				if(mMessageRows.get(i)[0].equals("[QUOTEHEADER]")) {
					mMessageRows.get(i)[2] = Integer.toString(headers);
					continue;
				}
				if(mMessageRows.get(i)[0].equals("[QUOTEMESSAGE]")) {
					mMessageRows.get(i)[2] = Integer.toString(headers);
					continue;
				}
				if(mMessageRows.get(i)[0].equals("[QUOTESPOILER]")) {
					mMessageRows.get(i)[2] = Integer.toString(headers);
					continue;
				}
				if(mMessageRows.get(i)[0].equals("[QUOTEFOOTER]")) {
					mMessageRows.get(i)[2] = Integer.toString(headers);
					continue;
				}
				if(mMessageRows.get(i)[0].equals("[POSTFOOTER]")) {
					mMessageRows.get(i)[2] = Integer.toString(headers);
					mMessageRows.get(i)[1] = quote;
					quote = "";
					continue;
				}
			}
		}







	}
}
