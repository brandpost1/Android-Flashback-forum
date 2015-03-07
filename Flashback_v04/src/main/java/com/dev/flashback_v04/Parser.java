package com.dev.flashback_v04;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.special.CurrentThreadsAdapter;
import com.dev.flashback_v04.interfaces.Callback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viktor on 2013-06-24.
 */
public class Parser {

	private Document currentSite;
	private Context mContext;

    public Parser(Context context) {
		mContext = context;
	}

    private void showErrorMsg(final String errormsg) {

        ((MainActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, errormsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void Connect(String url, Context context) throws NullPointerException, IOException  {
        Map<String, String> cookie = LoginHandler.getSessionCookie(context);

        SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        int timeout;
        try {
            String to = appPrefs.getString("connection_timeout", "5000");
            timeout = Integer.parseInt(to);
        } catch (Exception e) {
            timeout = 5000;
            e.printStackTrace();
        }

        if(haveNetworkConnection(context)) {
            // Check for cookie
            if(!cookie.isEmpty()) {
                // Connect with cookie
                currentSite = LoginHandler.cookieConnect(url, cookie, context, timeout);
            } else {
                // Connect without cookie
                currentSite = LoginHandler.basicConnect(url, timeout);
            }
            if(currentSite == null) {
                throw new NullPointerException("Failed to parse site");
            }
        } else {
            throw new NullPointerException("No network connection.");
        }
    }

    public boolean getPreferences() {
        try {
            Connect("https://www.flashback.org/profile.php?do=editoptions", mContext);
        } catch(NullPointerException e) {
            e.printStackTrace();

            return false;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        //String numPostsPerPage = currentSite.select("form table.tborder tbody fieldset:contains(Antal inlägg att visa per sida) select option[selected=selected]").attr("value");
        String numPostsPerPage = currentSite.select("form select[id=sel_umaxposts] option[selected=selected]").attr("value");
        int NumPostsPage;
        try {
            NumPostsPage = Integer.parseInt(numPostsPerPage);
            if(NumPostsPage == -1)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            NumPostsPage = 12;
            //e.printStackTrace();
        }

        try {
            SharedPreferences sessionPrefs = mContext.getSharedPreferences("APP_SETTINGS", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sessionPrefs.edit();
            editor.putInt("Thread_Max_Posts_Page", NumPostsPage).commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean setUserId() {
        try {
            Connect("https://www.flashback.org/", mContext);
        } catch(NullPointerException e) {
            e.printStackTrace();
            return false;
            //
        } catch (IOException e) {
            e.printStackTrace();
            return false;
            //showErrorMsg("Failed to load number of pages.");
        }
        String userId = "";
        try {
            userId = currentSite.select("div#top-menu ul.top-menu-sub li a[href^=/u]:contains(Min profil)").attr("href").substring(2);
        } catch (Exception e) {
            userId = "INVALID-ID";
            e.printStackTrace();
        }


        try {
            SharedPrefs.savePreference(mContext, "user", "ID", Integer.parseInt(userId));
            //"UserID: " + userId);
            return true;
        } catch(NumberFormatException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Retrieve all of the forums within a category
     * @param url
     * @param mProgressUpdate
     * @return null if OK. Else a String with an error-message.
     */
	public String getCategoryContent(String url, Callback<HashMap<String, String>> mProgressUpdate) {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
			Connect(url, mContext);
		} catch(NullPointerException e) {
			if(e.getMessage() != null) {
				if(e.getMessage().equals("No network connection."))
					return "Error: Det finns ingen internetanslutning.";
			}
			return "Error";
		} catch (IOException e) {
			if(e instanceof SocketTimeoutException) {
				return "Error: Connection timed out.";
			}
			return "Error";
		}

		Elements forums = currentSite.select("tr td.alt1Active");

		String forumName;
		String forumLink;
		String forumInfo;
		String numThreads;

		for(Element f : forums) {
            map = new HashMap<String, String>();
			Elements n;

			forumName = f.select("a").text();
			forumLink = f.select("a[href]").attr("abs:href");
			forumInfo = f.select(".forum-summary").text();
			numThreads = f.select(".forum-summary").text()
					.substring(0, f.select(".forum-summary").text().indexOf(" ämnen"));
			numThreads = numThreads.replaceAll("\\s+","");
			n = f.select(":has(.icon-subforum");
			if(n.size() == 0) {

                // Put Key-value pairs in map
                map.put("ForumName", forumName);
                map.put("ForumLink", forumLink);
                map.put("ForumInfo", forumInfo);
                map.put("NumberOfThreads", numThreads);
                // Return it
                mProgressUpdate.onTaskComplete(map);
			}
		}
        return null;
	}

    /**
     * Get the number of pages in a forum
     * @param url
     * @return
     */
    public int getForumNumPages(String url) {
        try {
            Connect(url, mContext);
        } catch(NullPointerException e) {
            e.printStackTrace();
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMsg("Failed to load number of pages.");
            return 1;
        }

        int number = 1;
        String numpages = "";
        try {
			numpages = currentSite.select(".tborder tbody tr td.alignr.navcontrolbar div.pagenav.nborder.fr table tbody tr td.vbmenu_control.smallfont2.delim").first().text();
        } catch (NullPointerException e) {
            //e.printStackTrace();
        }
        String[] split = numpages.split(" ");
        try {
            number = Integer.parseInt(split[3]);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return number;
    }

    /**
     * Retrieve all of the threads in a forum
     * @param url
     * @param mProgressUpdate
     * @return
     * @throws Exception
     */
	public String getForumContents(String url, Callback mProgressUpdate) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
			Connect(url, mContext);
		} catch(NullPointerException e) {
			if(e.getMessage() != null) {
				if(e.getMessage().equals("No network connection."))
					return "Error: Det finns ingen internetanslutning.";
			}
			return "Error";
		} catch (IOException e) {
			if(e instanceof SocketTimeoutException) {
				return "Error: Connection timed out.";
			}
			return "Error";
		}

		String name;
		String link;
		String views;
		String author;
		String numReplies;
		String pages;
		String lastPage;
        String lastPost;
		String isBold;
        Boolean locked;

		Elements threads = currentSite.select("#threadslist tr:not(:has(td.alt1.threadslist-announcement)):has(td.alt1.td_status):not(.tr_sticky)");
        Elements sticky = currentSite.select("#threadslist tr.tr_sticky");

        for(Element st : sticky) {
            map = new java.util.HashMap<String, String>();
            //name = st.select("td.alt1.td_title a[id^=thread_title]").text();
			name = st.select("td.alt1.td_title div strong a[id^=thread_title]").text();
			if(name.isEmpty()) {
				// Not bold text
				name = st.select("td.alt1.td_title div a[id^=thread_title]").text();
				isBold = "false";
			} else {
				isBold = "true";
			}
            link = st.select("td.alt1.td_title a[id^=thread_title]").attr("abs:href");
            author = st.select("td.alt1.td_title span[onclick^=window.open('/u]").text();
            views = st.select("td.alt2.td_views").text();
            numReplies = st.select("td.alt1.td_replies").text();
            pages = st.select("td.alt1.td_title a.thread-pagenav-lastpage").text();
            lastPost = st.select("td.alt2.td_last_post").text();

            if(st.select("td.alt1.td_status a.clear.icon-thread-lock").isEmpty()) {
                locked = false;
            } else {
                locked = true;
            }

            if(!pages.equals("")) {
                pages = pages.substring(1, pages.length()-1);
            } else {
                pages = "1";
            }
            lastPage = link.concat("p"+pages);

            map.put("ThreadAuthor", author);
            map.put("ThreadName", name);
            map.put("ThreadLink", link);
            map.put("ThreadSticky", "true");
            map.put("ThreadLocked", Boolean.toString(locked));
            map.put("ThreadNumReplies", numReplies);
            map.put("ThreadNumViews", views);
            map.put("LastPost", lastPost);
            map.put("ThreadLink", link);
            map.put("ThreadNumPages", pages);
            map.put("LastPageUrl", lastPage);
			map.put("BoldTitle", isBold);

            mProgressUpdate.onTaskComplete(map);

        }

		for(Element e : threads) {
            map = new java.util.HashMap<String, String>();
            //name = e.select("td.alt1.td_title a[id^=thread_title]").text();
			name = e.select("td.alt1.td_title div strong a[id^=thread_title]").text();
			if(name.isEmpty()) {
				// Not bold text
				name = e.select("td.alt1.td_title div a[id^=thread_title]").text();
				isBold = "false";
			} else {
				isBold = "true";
			}
            link = e.select("td.alt1.td_title a[id^=thread_title]").attr("abs:href");
            author = e.select("td.alt1.td_title span[onclick^=window.open('/u]").text();
            views = e.select("td.alt2.td_views").text();
            numReplies = e.select("td.alt1.td_replies").text();
            pages = e.select("td.alt1.td_title a.thread-pagenav-lastpage").text();
            lastPost = e.select("td.alt2.td_last_post").text();

            if(e.select("td.alt1.td_status a.clear.icon-thread-lock").isEmpty()) {
                locked = false;
            } else {
                locked = true;
            }

            if(!pages.equals("")) {
                pages = pages.substring(1, pages.length()-1);
            } else {
                pages = "1";
            }
            lastPage = link.concat("p"+pages);

            map.put("ThreadAuthor", author);
            map.put("ThreadName", name);
            map.put("ThreadLink", link);
            map.put("ThreadSticky", "false");
            map.put("ThreadLocked", Boolean.toString(locked));
            map.put("ThreadNumReplies", numReplies);
            map.put("ThreadNumViews", views);
            map.put("LastPost", lastPost);
            map.put("ThreadLink", link);
            map.put("ThreadNumPages", pages);
            map.put("LastPageUrl", lastPage);
			map.put("BoldTitle", isBold);

            mProgressUpdate.onTaskComplete(map);

        }
		return null;
	}

	public String getSubforumsAndThreads(String url, Callback forumProgress, Callback threadProgress) {
		HashMap<String, String> map = new HashMap<String, String>();

		try {
			Connect(url, mContext);
		} catch (NullPointerException e) {
			if (e.getMessage() != null) {
				if (e.getMessage().equals("No network connection."))
					return "Error: Det finns ingen internetanslutning.";
			}
			return "Error";
		} catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				return "Error: Connection timed out.";
			}
			return "Error";
		}

		// Get any forums first

		Elements forums = currentSite.select("tr td.alt1Active");

		String forumName;
		String forumLink;
		String forumInfo;
		String numThreads;

		for(Element f : forums) {
			map = new HashMap<String, String>();
			Elements n;

			forumName = f.select("a").text();
			forumLink = f.select("a[href]").attr("abs:href");
			forumInfo = f.select(".forum-summary").text();
			numThreads = f.select(".forum-summary").text()
					.substring(0, f.select(".forum-summary").text().indexOf(" ämnen"));
			numThreads = numThreads.replaceAll("\\s+","");

			n = f.select(":has(.icon-subforum");
			if(n.size() == 0) {

				// Put Key-value pairs in map
				map.put("ForumName", forumName);
				map.put("ForumLink", forumLink);
				map.put("ForumInfo", forumInfo);
				map.put("NumberOfThreads", numThreads);
				// Return it
				forumProgress.onTaskComplete(map);
			}
		}

		// Get threads second

		String name;
		String link;
		String views;
		String author;
		String numReplies;
		String pages;
		String lastPage;
		String lastPost;
		String isBold;
		Boolean locked;

		Elements threads = currentSite.select("#threadslist tr:not(:has(td.alt1.threadslist-announcement)):has(td.alt1.td_status):not(.tr_sticky)");
		Elements sticky = currentSite.select("#threadslist tr.tr_sticky");

		for(Element st : sticky) {
			map = new java.util.HashMap<String, String>();
			//name = st.select("td.alt1.td_title a[id^=thread_title]").text();
			name = st.select("td.alt1.td_title div strong a[id^=thread_title]").text();
			if(name.isEmpty()) {
				// Not bold text
				name = st.select("td.alt1.td_title div a[id^=thread_title]").text();
				isBold = "false";
			} else {
				isBold = "true";
			}
			link = st.select("td.alt1.td_title a[id^=thread_title]").attr("abs:href");
			author = st.select("td.alt1.td_title span[onclick^=window.open('/u]").text();
			views = st.select("td.alt2.td_views").text();
			numReplies = st.select("td.alt1.td_replies").text();
			pages = st.select("td.alt1.td_title a.thread-pagenav-lastpage").text();
			lastPost = st.select("td.alt2.td_last_post").text();

			if(st.select("td.alt1.td_status a.clear.icon-thread-lock").isEmpty()) {
				locked = false;
			} else {
				locked = true;
			}

			if(!pages.equals("")) {
				pages = pages.substring(1, pages.length()-1);
			} else {
				pages = "1";
			}
			lastPage = link.concat("p"+pages);

			map.put("ThreadAuthor", author);
			map.put("ThreadName", name);
			map.put("ThreadLink", link);
			map.put("ThreadSticky", "true");
			map.put("ThreadLocked", Boolean.toString(locked));
			map.put("ThreadNumReplies", numReplies);
			map.put("ThreadNumViews", views);
			map.put("LastPost", lastPost);
			map.put("ThreadLink", link);
			map.put("ThreadNumPages", pages);
			map.put("LastPageUrl", lastPage);
			map.put("BoldTitle", isBold);

			threadProgress.onTaskComplete(map);

		}

		for(Element e : threads) {
			map = new java.util.HashMap<String, String>();
			//name = e.select("td.alt1.td_title a[id^=thread_title]").text();
			name = e.select("td.alt1.td_title div strong a[id^=thread_title]").text();
			if(name.isEmpty()) {
				// Not bold text
				name = e.select("td.alt1.td_title div a[id^=thread_title]").text();
				isBold = "false";
			} else {
				isBold = "true";
			}
			link = e.select("td.alt1.td_title a[id^=thread_title]").attr("abs:href");
			author = e.select("td.alt1.td_title span[onclick^=window.open('/u]").text();
			views = e.select("td.alt2.td_views").text();
			numReplies = e.select("td.alt1.td_replies").text();
			pages = e.select("td.alt1.td_title a.thread-pagenav-lastpage").text();
			lastPost = e.select("td.alt2.td_last_post").text();

			if(e.select("td.alt1.td_status a.clear.icon-thread-lock").isEmpty()) {
				locked = false;
			} else {
				locked = true;
			}

			if(!pages.equals("")) {
				pages = pages.substring(1, pages.length()-1);
			} else {
				pages = "1";
			}
			lastPage = link.concat("p"+pages);

			map.put("ThreadAuthor", author);
			map.put("ThreadName", name);
			map.put("ThreadLink", link);
			map.put("ThreadSticky", "false");
			map.put("ThreadLocked", Boolean.toString(locked));
			map.put("ThreadNumReplies", numReplies);
			map.put("ThreadNumViews", views);
			map.put("LastPost", lastPost);
			map.put("ThreadLink", link);
			map.put("ThreadNumPages", pages);
			map.put("LastPageUrl", lastPage);
			map.put("BoldTitle", isBold);

			threadProgress.onTaskComplete(map);
		}

		return null;
	}

	/**
	 * Does not have a connect of its own, so has to be called directly after a call to a function that does.
	 * @param url
	 * @return
	 */
	public int getThreadPosition(String url) {
		int threadPos = 0;

		String temp = null;
		if(currentSite != null) {
            try {
                temp = currentSite.select("table.tborder.thread-nav div.pagenav.nborder.fr tr td.alt2 a.smallfont2.bold").get(0).text();
            } catch (IndexOutOfBoundsException e) {
                temp = "1";
            }
			if (!temp.equals("")) {
				try {
					threadPos = Integer.parseInt(temp);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
		return threadPos;
	}

	/**
	 * Does not have a connect of its own, so has to be called directly after a call to a function that does.
	 * @param url
	 * @return
	 */
	public int getThreadId(String url) {
		int threadId = 0;

		String temp;
		if(currentSite != null) {
			try {
				temp = currentSite.select("table.nborder.forum-navbar tbody tr td strong a").attr("abs:href");
				threadId = Integer.parseInt(temp.split("/t")[1]);
			} catch (IndexOutOfBoundsException e) {
				showErrorMsg("Error - Could not get threadId");
			} catch (NullPointerException e) {
				showErrorMsg("Error - Could not get threadId");
			}
		}
		return threadId;
	}

    /**
     * Get the number of pages in a thread
     * @param url
     * @return
     */
    public int getThreadNumPages(String url) {
        try {
			Connect(url, mContext);
        } catch(NullPointerException e) {
            return 1;
        } catch (IOException e) {
            showErrorMsg("Kunde inte hämta korrekt antal sidor. Visar första.");
            return 1;
        }
        int number;
        String numPages = currentSite.select("td.vbmenu_control.smallfont2.delim").text();
        String[] array = numPages.split(" ");

        try {
            String page = array[3];
            number = Integer.parseInt(page);
        } catch (ArrayIndexOutOfBoundsException e) {
            number = 1;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            number = 1;
        }

        return number;
    }

    /**
     * Retrieves all of the posts from a page in a thread.
     * Probably needs some work
     * @param url
     * @return
     */
	public String getThreadContent(String url, Callback<ArrayList<Post>> updatePosts) {
		//long startTime = System.nanoTime();
        ArrayList<Post> postArrayList = new ArrayList<Post>();

		try {
			Connect(url, mContext);
		} catch(NullPointerException e) {
			if(e.getMessage() != null) {
				if(e.getMessage().equals("No network connection."))
					return "Error: Det finns ingen internetanslutning.";
			}
			return "Error";
		} catch (IOException e) {
			if(e instanceof SocketTimeoutException) {
				return "Error: Connection timed out.";
			}
			return "Error";
		}


        String author;
		String numposts;
		String membertype;
		String orderNr;
		String date;
		String avatarurl;
		String online;
		String userProfileUrl;
		String regdate;
        String postUrl;
		String threadId;

		threadId = currentSite.select("td.navbar a").attr("abs:href");
		if(threadId.contains("/t")) {
			threadId = threadId.split("/t")[1];
			if(threadId.contains("p")) {
				threadId = threadId.split("p")[0];
			}
		}

		Elements message;

		Elements posts = currentSite.select("table[id^=post]");
		for(Element post : posts) {
			final Post newPost = new Post();

			if(!post.select("a.fr.show-ignored-post").isEmpty()) {
				// Ignored user in this post
				String blockedMessage = post.select("tbody tr td.alt1 div.smallfont").text();
				String blockedUser = post.select("tbody tr td.alt1 div.smallfont strong").text();
				String blockedDate = post.select("tbody tr[title^=Inlägg] td.thead").first().ownText();
				newPost.setDate(blockedDate);
				newPost.setAuthor(blockedUser);
				newPost.addRow("[BLOCKEDUSER]", blockedMessage);
			} else {

				if(!post.select("a[href^=editpost.php").isEmpty()) {
					String temp = post.select("a[href^=editpost.php]").attr("abs:href");
					newPost.setEditUrl(temp);
				} else {
					newPost.setEditUrl("");
				}

				date = post.select("tbody tr td.thead.post-date").first().ownText();
				orderNr = "#" + post.select("tbody tr td.thead.post-date span a").text();
				postUrl = post.select("tbody tr td.thead.post-date span a").attr("abs:href");
				author = post.select("tbody tr[style^=vertical] td.alt2.post-left div[id^=postmenu]").text();
				membertype = post.select("table.post-user-title").text();
				online = post.select("div[class^=icon-user]").attr("title");
				userProfileUrl = post.select("a.bigusername").attr("abs:href");
				avatarurl = post.select("a.post-user-avatar img").attr("src");
				regdate = post.select("div.post-user-info.smallfont div:contains(Reg)").text();
				numposts = post.select("tbody tr[style^=vertical] td.alt2.post-left div.post-user div.post-user-info.smallfont div:nth-child(2)").text();
				newPost.setDate(date);
				newPost.setOrderNr(orderNr);
				newPost.setAuthor(author);
				newPost.setMembertype(membertype);
				newPost.setOnline(online);
				newPost.setUserProfileUrl(userProfileUrl);
				newPost.setAvatarurl(avatarurl);
				newPost.setRegdate(regdate);
				newPost.setNumposts(numposts);
				newPost.setPostUrl(postUrl);
				newPost.setThreadId(threadId);
				message = post.select("div.post_message");



				// Links in posts.
				// div#posts div[id^=edit] td.alt1.post-right div.post_message a
				for (int i = 0; i < post.select("td.alt1.post-right div.post_message a").size(); i++) {
					String text = post.select("td.alt1.post-right div.post_message a").get(i).attr("href");
					if (!text.contains("flashback.org") && text.startsWith("/leave.php") && !(text.length() < 14))
						try {
							text = text.substring(13);
						} catch (IndexOutOfBoundsException e) {
							e.printStackTrace();
							text = "[FB-ParseError] Please report bug in official thread";
						}

					try {
						text = URLDecoder.decode(text, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						throw new AssertionError("UTF-8 is unknown");
					}

					post.select("td.alt1.post-right div.post_message a").get(i).text(text);
				}
				// Remove nested spoilers in quotes. (Spoilers containing spoilers, in a quote)
				message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote div.alt2.post-bbcode-spoiler div.alt2.post-bbcode-spoiler").before("[NESTED SPOILER]<br>");
				message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote div.alt2.post-bbcode-spoiler div.alt2.post-bbcode-spoiler").remove();

				// Remove codetags within quotes
				message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote div[style=margin:5px 10px;]").before("[CITERAD KOD]<br>");
				message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote div[style=margin:5px 10px;]").remove();
				message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote div[style=margin:20px; margin-top:5px]").before("[CITERAD KOD]<br>");
				message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote div[style=margin:20px; margin-top:5px]").remove();

				// Remove codeheader "Kod:"
				message.select("div[style=margin:5px 10px;] > div:first-child").remove();
				// Surround code with own tags
				message.select("pre.alt2.post-bbcode-code").before("{phptag}").after("{/phptag}");

				// Remove phpcode header "Kod:"
				message.select("div[style=margin:20px; margin-top:5px] > div:first-child").remove();
				// Surround code with own tags
				message.select("div.alt2.post-bbcode-php").before("{phptag}").after("{/phptag}");

				// Replace text-styling tags
				message.select("b").before("[b]").after("[/b]");
				message.select("i").before("[i]").after("[/i]");
				message.select("u").before("[u]").after("[/u]");

				// Remove codetags within spoilers
				message.select("div.alt2.post-bbcode-spoiler div[style=margin:5px 10px;]").before("[SPOILAD KOD]<br>");
				message.select("div.alt2.post-bbcode-spoiler div[style=margin:5px 10px;]").remove();
				message.select("div.alt2.post-bbcode-spoiler div[style=margin:20px; margin-top:5px]").before("[SPOILAD KOD]<br>");
				message.select("div.alt2.post-bbcode-spoiler div[style=margin:20px; margin-top:5px]").remove();

				// Remove spoiler headers
				for (int i = 0; i < message.select("div[style=margin:5px 20px 20px 20px]").size(); i++) {
					message.select("div[style=margin:5px 20px 20px 20px] div.smallfont").remove();
				}
				// Remove quote header
				message.select("div.post-quote-holder > div.smallfont.post-quote-title").remove();

				// Replace smileys with [IMAGE...] tags
				for (int i = 0; i < message.size(); i++) {
					for (int j = 0; j < message.get(i).select("img").size(); j++) {
						message.get(i).select("img").get(j).before("[IMAGE" + message.get(i).select("img").get(j).attr("title").toUpperCase() + "]");
					}
				}


				// Surround regular spoilers and spoilers within quotes
				//message.select(">div[style=margin:5px 20px 20px 20px]").before("{spoilertext}").after("{/spoilertext}");
				message.select("div.post_message > div > div.alt2.post-bbcode-spoiler").before("{spoilertext}").after("{/spoilertext}");
				//message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote > div > div[style=margin:5px 20px 20px 20px]").before("{quotespoilertext}").after("{/quotespoilertext}");
				message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote div.alt2.post-bbcode-spoiler").before("{quotespoilertext}").after("{/quotespoilertext}");

				// Remove nested quotes in quotes
				message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote div.post-quote-holder").remove();

				// Remove nested quotes within spoilers
				message.select("div.alt2.post-bbcode-spoiler div.post-quote-holder").remove();

				// Surround quotes with {quote}{/quote}
				message.select("div.post-quote-holder").before("{quote}").after("{/quote}");

				// Distinguish between regular quotes, and quotes with no name in the header.
				int quotesize = message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote").size();

				for (int i = 0; i < quotesize; i++) {
					//message);
					String quotetext;

                /* Radbrytningar i citat */
					message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote").get(i).getElementsByTag("br").before("[newline]");
					message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote").get(i).getElementsByTag("br").remove();


                 /*
                * Fails for some malformed quotes. One is present on https://www.flashback.org/t1672095p4
                * Using 40 posts per page
                * */
					try {
						quotetext = message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote").get(i).text().trim();
					} catch (Exception e) {
						e.printStackTrace();
						quotetext = "[FLASHBACK-APP]Error-quote";
					}

                /*
                * Fails for some malformed quotes. One is present on https://www.flashback.org/t1672095p4
                * Using 40 posts per page
                * */
					if (quotetext.startsWith("Ursprungligen postat av")) {
						String n;
						try {
							n = message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote strong:first-child").get(i).text().trim();
						} catch (Exception e) {
							e.printStackTrace();
							n = "[FLASHBACK-APP]ERROR-Name";
						}

						String text;
						try {
							text = message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote div:nth-child(2)").get(i).text().trim();
						} catch (Exception e) {
							e.printStackTrace();
							text = "[FLASHBACK-APP]ERROR-Msg";
						}

						// String name = message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote strong").get(i).text().trim();
						String name = n.trim();
						//message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote:has(div)").get(i).html("<strong>"+ name +"</strong>{quote_text}<div>"+text+"</div>{/quote_text}");
						message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote").get(i).html("<strong>" + name + "</strong>{quote_text}<div>" + text + "</div>{/quote_text}");
					} else {
						//String text = message.select("div.post-quote-holder table.p2-4 tbody tr > td.alt2.post-quote:not(:has(div))").get(0).text().trim();
						String text = quotetext.trim();
						try {
							// https://www.flashback.org/t2267536p44
							message.select("div.post-quote-holder table.p2-4 tbody tr > td.alt2.post-quote").get(i).html("{anon_quote}{/anon_quote}{quote_text}<div>" + text + "</div>{/quote_text}");
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}
				// Replace stuff in codetags
				for (Element element : message.select("pre.alt2.post-bbcode-code")) {
					element.text(element.text().replace(" ", "[SPACE]"));
					element.text(element.text().replace("&nbsp;", "[SPACE]"));
					element.text(element.text().replaceAll("\\t", "[SPACE][SPACE][SPACE][SPACE]"));
					element.text(element.text().replaceAll("\\r", "[newline]"));
				}

				// Surround name of the quotee [newline]
				message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote strong").before("{quoter_name}").after("{/quoter_name}");
				//message);


				String site = message.toString();
				site = site.replaceAll("(?i)<br[^>]*> *", "[newline]");
				site = site.replace("&nbsp;", "[SPACE]");
				Element elem = Jsoup.parse(site);

				site = elem.text().replace("<", "&lt;").replace(">", "&gt;");

				Document doc = Jsoup.parseBodyFragment(site);

				site = doc.outerHtml();

				site = site.replace("{anon_quote}", "<anon_quote>").replace("{/anon_quote}", "</anon_quote>");
				site = site.replace("{quoter_name}", "<quoter_name>").replace("{/quoter_name}", "</quoter_name>");
				site = site.replace("{quote}", "<quote>").replace("{/quote}", "</quote>");
				site = site.replace("{quote_text}", "<quote_text>").replace("{/quote_text}", "</quote_text>");
				site = site.replace("{spoilertext}", "<spoiler>").replace("{/spoilertext}", "</spoiler>");
				site = site.replace("{quotespoilertext}", "<quotespoiler>").replace("{/quotespoilertext}", "</quotespoiler>");
				site = site.replace("{phptag}", "<phptag>").replace("{/phptag}", "</phptag>");
				site = site.replace("{codetag}", "<codetag>").replace("{/codetag}", "</codetag>");
				//site = site.replace("[newline]", "\n");

				doc = Jsoup.parseBodyFragment(site);
				// Temporarily remove code- and php-tags
				//doc.select("codetag").remove();
				//doc.select("phptag").remove();

				final ArrayList<String[]> postarr = new ArrayList<String[]>();
				final boolean[] quoteOpen = new boolean[]{false};
				final boolean[] messageOpen = new boolean[]{false};
				final boolean[] spoilerOpen = new boolean[]{false};
				final boolean[] quotespoilerOpen = new boolean[]{false};
				final boolean[] quoteHeaderOpen = new boolean[]{false};
				final boolean[] phptagOpen = new boolean[]{false};
				final boolean[] anonQuoteHeaderOpen = new boolean[]{false};

				Elements body = doc.select("body");

				body.traverse(new NodeVisitor() {

					@Override
					public void head(Node node, int level) {
						if (node instanceof TextNode && node.toString().length() != 1) {

							if (newPost.getPostRows().get(newPost.getPostRows().size() - 1)[0].equals("[QUOTEHEADER]") && quoteOpen[0] == true && quoteHeaderOpen[0] == true) {
								String quoteheader = ((TextNode) node).text();
								quoteheader = quoteheader.replace("<", "&lt;");
								quoteheader = quoteheader.replace(">", "&gt;");
								quoteheader = Jsoup.parse(quoteheader).text();
								quoteheader = quoteheader.replaceAll("\\s*\\[newline\\]\\s*", "\n");
								quoteheader = quoteheader.replace("&lt;", "<");
								quoteheader = quoteheader.replace("&gt;", ">");
								quoteheader = quoteheader.replace("[SPACE]", " ");
								newPost.getPostRows().get(newPost.getPostRows().size() - 1)[1] = quoteheader.trim();

							}
							if (quoteOpen[0] == true && quotespoilerOpen[0] == false && quoteHeaderOpen[0] == false && anonQuoteHeaderOpen[0] == false) {
								String quotemsg = ((TextNode) node).text();
								quotemsg = quotemsg.replace("<", "&lt;");
								quotemsg = quotemsg.replace(">", "&gt;");
								quotemsg = Jsoup.parse(quotemsg).text();
								quotemsg = quotemsg.replaceAll("\\s*\\[newline\\]\\s*", "\n");
								quotemsg = quotemsg.replace("&lt;", "<");
								quotemsg = quotemsg.replace("&gt;", ">");
								quotemsg = quotemsg.replace("[SPACE]", " ");
								newPost.addRow("[QUOTEMESSAGE]", quotemsg.trim());
							}

							if (quoteOpen[0] == true && quotespoilerOpen[0] == true) {
								if (newPost.getPostRows().get(newPost.getPostRows().size() - 1)[0].equals("[QUOTESPOILER]")) {
									String quotespoilertext = ((TextNode) node).text();
									quotespoilertext = quotespoilertext.replace("<", "&lt;");
									quotespoilertext = quotespoilertext.replace(">", "&gt;");
									quotespoilertext = Jsoup.parse(quotespoilertext).text();
									quotespoilertext = quotespoilertext.replaceAll("\\s*\\[newline\\]\\s*", "\n");
									quotespoilertext = quotespoilertext.replace("&lt;", "<");
									quotespoilertext = quotespoilertext.replace("&gt;", ">");
									quotespoilertext = quotespoilertext.replace("[SPACE]", " ");
									newPost.getPostRows().get(newPost.getPostRows().size() - 1)[1] = quotespoilertext.trim();
								}
							}
							if (quoteOpen[0] == false && quotespoilerOpen[0] == false && spoilerOpen[0] == true) {
								if (newPost.getPostRows().get(newPost.getPostRows().size() - 1)[0].equals("[SPOILER]")) {
									String spoilertext = ((TextNode) node).text();
									spoilertext = spoilertext.replace("<", "&lt;");
									spoilertext = spoilertext.replace(">", "&gt;");
									spoilertext = Jsoup.parse(spoilertext).text();
									spoilertext = spoilertext.replaceAll("\\s*\\[newline\\]\\s*", "\n");
									spoilertext = spoilertext.replace("&lt;", "<");
									spoilertext = spoilertext.replace("&gt;", ">");
									spoilertext = spoilertext.replace("[SPACE]", " ");
									newPost.getPostRows().get(newPost.getPostRows().size() - 1)[1] = spoilertext.trim();
								}
							}
							if (quoteOpen[0] == false && quotespoilerOpen[0] == false && spoilerOpen[0] == false && phptagOpen[0] == false) {
								String msg = ((TextNode) node).text();
								msg = msg.replace("<", "&lt;");
								msg = msg.replace(">", "&gt;");
								msg = Jsoup.parse(msg).text();
								msg = msg.replaceAll("\\s*\\[newline\\]\\s*", "\n");
								msg = msg.replaceAll("\\[b\\]\\s*", "[b]");
								msg = msg.replaceAll("\\[i\\]\\s*", "[i]");
								msg = msg.replaceAll("\\[u\\]\\s*", "[u]");
								msg = msg.replace("&lt;", "<");
								msg = msg.replace("&gt;", ">");
								msg = msg.replace("[SPACE]", " ");
								newPost.addRow("[MESSAGE]", msg.trim());
							}
							if (quoteOpen[0] == false && quotespoilerOpen[0] == false && spoilerOpen[0] == false && phptagOpen[0] == true) {
								String msg = ((TextNode) node).text();
								msg = msg.replace("<", "&lt;");
								msg = msg.replace(">", "&gt;");
								msg = Jsoup.parse(msg).text();
								msg = msg.replaceAll("\\s*\\[newline\\]\\s*", "\n");
								msg = msg.replace("&lt;", "<");
								msg = msg.replace("&gt;", ">");
								msg = msg.replace("[SPACE]", " ");
								newPost.addRow("[PHPTAGMESSAGE]", msg.trim());
							}
						}

						if (node instanceof Element) {
							if (node.nodeName().equals("body")) {
								newPost.addRow("[POSTHEADER]", "");
							}
							if (node.nodeName().equals("quotespoiler")) {
								newPost.addRow("[QUOTESPOILER]", "");
								quotespoilerOpen[0] = true;
							}
							if (node.nodeName().equals("quote")) {
								quoteOpen[0] = true;
							}
							if (node.nodeName().equals("quoter_name")) {
								newPost.addRow("[QUOTEHEADER]", "");
								//quoteOpen[0] = true;
								quoteHeaderOpen[0] = true;
							}
							if (node.nodeName().equals("anon_quote")) {
								newPost.addRow("[ANONQUOTEHEADER]", "");
								anonQuoteHeaderOpen[0] = true;
							}
							if (node.nodeName().equals("spoiler")) {
								newPost.addRow("[SPOILER]", "");
								spoilerOpen[0] = true;
							}
							if (node.nodeName().equals("phptag")) {
								newPost.addRow("[PHPTAGHEADER]", "");
								phptagOpen[0] = true;
							}
						}
					}

					@Override
					public void tail(Node node, int level) {
						if (node instanceof Element) {
							if (node.nodeName().equals("quote_text")) {
								newPost.addRow("[QUOTEFOOTER]", "");
								quoteOpen[0] = false;
							}
							if (node.nodeName().equals("quotespoiler")) {
								quotespoilerOpen[0] = false;
							}
							if (node.nodeName().equals("quoter_name")) {
								quoteHeaderOpen[0] = false;
							}
							if (node.nodeName().equals("spoiler")) {
								spoilerOpen[0] = false;
							}
							if (node.nodeName().equals("phptag")) {
								newPost.addRow("[PHPTAGFOOTER]", "");
								phptagOpen[0] = false;
							}
							if (node.nodeName().equals("anon_quote")) {
								anonQuoteHeaderOpen[0] = false;
							}
							if (node.nodeName().equals("body")) {
								newPost.addRow("[POSTFOOTER]", "");
								newPost.getPostRows().get(newPost.getPostRows().size() - 1)[3] = newPost.getAuthor();
								newPost.getPostRows().get(newPost.getPostRows().size() - 1)[4] = newPost.getOrderNr();
							}

						}
					}
				});
			}
            postArrayList.add(newPost);
		}
		//long endTime = System.nanoTime();
		//long duration = endTime - startTime;
		//showErrorMsg("Executiontime: "+ TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS));

		// Return all posts
		updatePosts.onTaskComplete(postArrayList);

		// No errors
        return null;
	}

    /**
     * Retrieves all of the items for "Aktuella ämnen", or "Current threads", and
     * posts them back via callback.
     * @param url
     * @param mProgressUpdate
     * @return
     */
    public boolean getCurrent(String url, Callback mProgressUpdate) {

        try {
			Connect(url, mContext);
        } catch(NullPointerException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        String categoryName = "";
        String threadLink = "";
        String threadName = "";
        String forumLink = "";
        String forumName = "";
        String readers = "";
        String replies = "";
        String views = "";

        // Select på dom fyra kategorierna
        Elements categories = currentSite.select("table.tborder.threadslist tbody");

        // Loopa igenom de fyra
        for(int i = 0; i < categories.size(); i++) {
			HashMap<String, String> newItem = new HashMap<String, String>();

            categoryName = categories.get(i).select("tr:eq(0) td:eq(1)").text();

            newItem.put("Type", Integer.toString(CurrentThreadsAdapter.ITEM_HEADER));
            newItem.put("Headline", categoryName);

            mProgressUpdate.onTaskComplete(newItem);

            for(int j = 0; j < categories.get(i).select("tr:not(:first-child)").size(); j++) {
				HashMap<String, String> newThread = new HashMap<String, String>();

                threadLink = categories.get(i).select("tr:not(:first-child) td:eq(1) a:first-child").get(j).attr("abs:href");
                threadName = categories.get(i).select("tr:not(:first-child) td:eq(1) a:first-child").get(j).text();
                forumName = categories.get(i).select("tr:not(:first-child) td:eq(1) a:eq(1)").get(j).text();
                readers = categories.get(i).select("tr:not(:first-child) td:eq(3)").get(j).text();
                views  = categories.get(i).select("tr:not(:first-child) td:eq(4)").get(j).text();
                replies = categories.get(i).select("tr:not(:first-child) td:eq(5)").get(j).text();

                newThread.put("Type", Integer.toString(CurrentThreadsAdapter.ITEM_ROW));
                newThread.put("Headline", threadName);
                newThread.put("Readers", readers);
                newThread.put("Views", views);
                newThread.put("Replies", replies);
                newThread.put("SourceForum", forumName);
                newThread.put("Link", threadLink);

                mProgressUpdate.onTaskComplete(newThread);
            }
        }
        return true;
    }

    /**
     * Retrieves all of the newly created threads and posts them back
     * one at a time via callback.
     * @param url
     * @param mProgressUpdate
     * @return
     */
    public boolean getNewThreads(String url, Callback mProgressUpdate) {

        try {
			Connect(url, mContext);
        } catch(NullPointerException e) {
            e.printStackTrace();

            return false;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        String threadLink = "";
        String threadName = "";
        String forumLink = "";
        String forumName = "";
        String readers = "";
        String replies = "";
        String views = "";

        Elements elements = currentSite.select("table.tborder.threadslist tbody tr:not(:first-child)");

        for(Element e : elements) {
            threadLink = e.select("td:eq(1) a:first-child").attr("abs:href");
            threadName = e.select("td:eq(1) a:first-child").text();
            forumName = e.select("td:eq(1) a:eq(1)").text();
            readers = e.select("td:eq(3)").text();
            views  = e.select("td:eq(4)").text();
            replies = e.select("td:eq(5)").text();

            HashMap<String, String> newThread = new HashMap<String, String>();
            newThread.put("Headline", threadName);
            newThread.put("Readers", readers);
            newThread.put("Views", views);
            newThread.put("Replies", replies);
            newThread.put("SourceForum", forumName);
            newThread.put("Link", threadLink);

            mProgressUpdate.onTaskComplete(newThread);
        }
        return true;
    }

    /**
     * Retrieves all new posts and posts them back
     * one at a time via callback.
     * @param url
     * @param mProgressUpdate
     * @return
     */
    public boolean getNewPosts(String url, Callback mProgressUpdate) {

        try {
			Connect(url, mContext);
        } catch(NullPointerException e) {
            e.printStackTrace();

            return false;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        String threadLink = "";
        String threadName = "";
        String forumLink = "";
        String forumName = "";
        String readers = "";
        String replies = "";
        String views = "";

        Elements elements = currentSite.select("table.tborder.threadslist tbody tr:not(:first-child)");

        for(Element e : elements) {
            threadLink = e.select("td:eq(1) a:first-child").attr("abs:href");
            threadName = e.select("td:eq(1) a:first-child").text();
            forumName = e.select("td:eq(1) a:eq(1)").text();
            readers = e.select("td:eq(3)").text();
            views  = e.select("td:eq(4)").text();
            replies = e.select("td:eq(5)").text();

            HashMap<String, String> newPost = new HashMap<String, String>();
            newPost.put("Headline", threadName);
            newPost.put("Readers", readers);
            newPost.put("Views", views);
            newPost.put("Replies", replies);
            newPost.put("SourceForum", forumName);
            newPost.put("Link", threadLink);

            mProgressUpdate.onTaskComplete(newPost);
        }
        return true;
    }

    /**
     * Retrieves all of the received private messages from one page
     * @param url
     */
    public boolean getPrivateMessages(String url, Callback progressUpdate) {
		try {
			Connect(url, mContext);
		} catch(NullPointerException e) {
			e.printStackTrace();

			return false;
		} catch (IOException e) {
			e.printStackTrace();

			return false;
		}

        // Get categories
        Elements categories = currentSite.select("form[method=post] table.tborder.p2-4 tbody:not(:last-child):nth-child(2n)");
        Elements categorycontent = currentSite.select("form[method=post] table.tborder.p2-4 tbody:not(:last-child):nth-child(2n+1)");

		for(int i = 0; i < categories.size(); i++) {
			String categoryName = categories.get(i).select("div.smallfont > strong").text();
			String categoryInfo = categories.get(i).select("div.smallfont > span").text();
			HashMap<String, String> category = new HashMap<String, String>();
			category.put("ItemType", "Divider");
			category.put("CategoryName", categoryName);
			category.put("CategoryInfo", categoryInfo);

			progressUpdate.onTaskComplete(category);

			Elements messages = categorycontent.get(i).select(">tr");
			for(int j = 0; j < messages.size(); j++) {
				String icon = messages.get(j).select("td:first-child div").attr("class");
				String messageDate = messages.get(j).select("td:nth-child(2) div:first-child.smallfont").text();
				String messageTime = messages.get(j).select("td:nth-child(2) div:nth-child(2).time").text();
				String messageHeadline = messages.get(j).select("td:nth-child(2) div:first-child a").text();
				String messageFrom = messages.get(j).select("td:nth-child(2) div:nth-child(2) a").text();
				String messageLink = messages.get(j).select("td:nth-child(2) div:first-child a").attr("abs:href");


				HashMap<String, String> message = new HashMap<String, String>();
				message.put("ItemType", "Message");
				message.put("Date", messageDate);
				message.put("Time", messageTime);
				message.put("Headline", messageHeadline);
				message.put("From", messageFrom);
				message.put("Link", messageLink);
				message.put("Icon", icon);
				progressUpdate.onTaskComplete(message);
			}

		}

		return true;
    }

    public int myPostsPages(String url) {
        return myQuotesPages(url);
    }

    public boolean getMyPosts(String url, Callback itemcallback) {
		getMyQuotes(url, itemcallback);
        return true;
    }


    public int myThreadsPages(String url) {
        return myQuotesPages(url);
    }

    public boolean getMyThreads(String url, Callback mProgressUpdate) {
		HashMap<String, String> map;
		try {
			Connect(url, mContext);
		} catch(NullPointerException e) {
			e.printStackTrace();

			return false;
		} catch (IOException e) {
			e.printStackTrace();

			return false;
		}

		String name;
		String link;
		String views;
		String author;
		String numReplies;
		String pages;
		String lastPage;
		String lastPost;
		Boolean locked;


		Elements threads = currentSite.select("table.tborder.threadslist tbody tr:not(:first-child)");

		for(Element e : threads) {
			map = new HashMap<String, String>();
			name = e.select("td.alt1.td_title a[id^=thread_title]").text();
			link = e.select("td.alt1.td_title a[id^=thread_title]").attr("abs:href");
			author = e.select("div.smallfont.thread-poster span").text();
			views = e.select("td:nth-child(5)").text();
			numReplies = e.select("td:nth-child(4)").text();
			pages = e.select("a.thread-pagenav-lastpage").text();
			lastPost = e.select("td.alt2.td_last_post").text();

			if(e.select("td.alt1.td_status a.clear.icon-thread-lock").isEmpty()) {
				locked = false;
			} else {
				locked = true;
			}

			if(!pages.equals("")) {
				pages = pages.substring(1, pages.length()-1);
			} else {
				pages = "1";
			}
			lastPage = link.concat("p"+pages);

			map.put("ThreadAuthor", author);
			map.put("ThreadName", name);
			map.put("ThreadLink", link);
			map.put("ThreadSticky", "false");
			map.put("ThreadLocked", Boolean.toString(locked));
			map.put("ThreadNumReplies", numReplies);
			map.put("ThreadNumViews", views);
			map.put("LastPost", lastPost);
			map.put("ThreadLink", link);
			map.put("ThreadNumPages", pages);
			map.put("LastPageUrl", lastPage);

			mProgressUpdate.onTaskComplete(map);
		}
        return true;
    }

    /**
     * Retrieves the number of pages for "My quoted posts"
     * @param url
     * @return
     */
    public int myQuotesPages(String url) {
        try {
			Connect(url, mContext);
        } catch(NullPointerException e) {
            e.printStackTrace();

            return 1;
        } catch (IOException e) {
            e.printStackTrace();

            return 1;
        }

        String temp = currentSite.select("table.tborder.p2-4 tr td.vbmenu_control.smallfont2.delim").text();
        if(temp.isEmpty()) {
            return 1;
        }
        String[] tempArray = temp.split(" ");
        String pages = "";
        try {
            pages = tempArray[3];
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return 1;
        }
        int numPages = 1;
        try {
            numPages = Integer.parseInt(pages);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 1;
        }

        return numPages;
    }

    /**
     * Retrieves all of the "My quoted posts" from one page and returns them
     * one a time
     * @param url
     * @param itemcallback
     * @return
     */
    public boolean getMyQuotes(String url, Callback itemcallback) {

        HashMap<String, String> map;
        ArrayList<HashMap<String, String>> newList = new ArrayList<HashMap<String, String>>();
        try {
			Connect(url, mContext);
        } catch(NullPointerException e) {
            e.printStackTrace();

            return false;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        String inForum;
        String forumLink;
        String date;
        String postedBy;
        String threadTitle;
        String threadLink;
        String messageText;

        Elements posts = currentSite.select("div#posts table.p4");

        for(int i = 0; i < posts.size(); i++) {
            map = new HashMap<String, String>();
            inForum = posts.get(i).select("td.thead span a").text();
            forumLink = posts.get(i).select("td.thead span a").attr("abs:href");
            date = posts.get(i).select("td.thead").first().ownText();
            postedBy = posts.get(i).select("td.alt1 > div.smallfont a").text();
            threadTitle = posts.get(i).select("div a[href^=/t]").text();
            threadLink = posts.get(i).select("td.alt1 div.alt2.post-quote div em a[href^=/p]").attr("abs:href");
            threadLink = threadLink.substring(0, threadLink.lastIndexOf("#"));

            // Get html of message
            messageText = posts.get(i).select("td.alt1 div.alt2.post-quote em").html();
            // Replace newlines
            Element message = Jsoup.parse(messageText.replaceAll("(?i)<br[^>]*> *", "[newline]"));
            // Remove link-tag
            message.select("a").remove();
            // Remove first two newlines
            messageText = message.text().substring(18);
            // Get actual message and put newline character
            messageText = messageText.replace("[newline]", "\n");

            map.put("ForumName", inForum);
            map.put("ForumLink", forumLink);
            map.put("Date", date);
            map.put("PostedBy", postedBy);
            map.put("ThreadTitle", threadTitle);
            map.put("ThreadLink", threadLink);
            map.put("MessageText", messageText);

            newList.add(map);

            if(itemcallback != null)
                itemcallback.onTaskComplete(map);
        }
        return true;
    }

	public int getSubscriptionPages(String url) {
		int numPages = 1;
		try {
			Connect(url, mContext);
		} catch(NullPointerException e) {
			e.printStackTrace();

			return numPages;
		} catch (IOException e) {
			e.printStackTrace();

			return numPages;
		}

		String temp = currentSite.select("table div.pagenav.nborder.fr table tbody tr td.vbmenu_control.smallfont2.delim").text();

		if(temp.isEmpty()) {
			return 1;
		}
		String[] tempArray = temp.split(" ");
		String pages = "";
		try {
			pages = tempArray[3];
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return numPages;
		}

		try {
			numPages = Integer.parseInt(pages);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return numPages;
		}

		return numPages;
	}

	public boolean getMySubscribedThreads(String url, Callback progressupdate) {
		try {
			Connect(url, mContext);
		} catch(NullPointerException e) {
			e.printStackTrace();

			return false;
		} catch (IOException e) {
			e.printStackTrace();

			return false;
		}

		Elements threads = currentSite.select("table#threadslist tbody tr:has(td.alt1.td_status)");

		for(Element thread : threads) {
			String startuser = thread.select("div.smallfont.thread-poster").text();
			String threadTitle = "";
			String isBold = "";
			threadTitle = thread.select("td.alt1.td_title div strong a[id^=thread_title]").text();
			if(threadTitle.isEmpty()) {
				// Not bold text
				threadTitle = thread.select("td.alt1.td_title div a[id^=thread_title]").text();
				isBold = "false";
			} else {
				isBold = "true";
			}
			String threadLink = thread.select("td.alt1.td_title div a[id^=thread_title]").attr("abs:href");
			String lastPost = thread.select("td.alt2.td_last_post").text();
			String checkbox = thread.select("td input").attr("name");
			String lastPage;

			try {
				lastPage = thread.select("a.thread-pagenav-lastpage").attr("href").split("p")[1];
			} catch (ArrayIndexOutOfBoundsException e) {
				lastPage = "1";
			}

			String monitoring = thread.select("td.alt1.smallfont.alignc").text();

			HashMap<String, String> threadMap = new HashMap<String, String>();
			threadMap.put("User", startuser);
			threadMap.put("Title", threadTitle);
			threadMap.put("Link", threadLink);
			threadMap.put("LastPost", lastPost);
			threadMap.put("LastPage", lastPage);
			threadMap.put("Checkbox", checkbox);
			threadMap.put("Checked", "0");
			threadMap.put("BoldTitle", isBold);
			threadMap.put("Monitoring", monitoring);
			progressupdate.onTaskComplete(threadMap);
		}

		return true;
	}

	public String getPrivateMessageContent(String url, Callback<ArrayList<Post>> pmUpdate) {
		return getThreadContent(url, pmUpdate);
	}

	public String getNewPrivateMessageToken() {
		String token = "";
		if(LoginHandler.loggedIn(mContext)) {
			try {
				Connect("https://www.flashback.org/private.php?do=newpm", mContext);
			} catch (NullPointerException e) {
				e.printStackTrace();
				return "";
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}

			token = currentSite.select("input[name=stoken]").attr("value");
		}
		return token;
	}

	public HashMap<String, String> getEditPostContent(String url) {
		HashMap<String, String> fields = new HashMap<String, String>();

		try {
			Connect(url, mContext);
		} catch(NullPointerException e) {
			e.printStackTrace();
			return fields;
		} catch (IOException e) {
			e.printStackTrace();
			return fields;
		}

		String reason = currentSite.select("input[name=reason]").attr("value");
		String title = currentSite.select("input[name=title]").attr("value");
		String message = currentSite.select("textarea[name=message]").text();

		fields.put("Reason", reason);
		fields.put("Title", title);
		fields.put("Message", message);


		return fields;
	}

}
