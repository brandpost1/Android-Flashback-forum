package com.dev.flashback_v04;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.dev.flashback_v04.activities.MainActivity;
import com.dev.flashback_v04.adapters.special.CurrentThreadsAdapter;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Viktor on 2013-06-24.
 */
public class Parser {


	private Document currentSite;
	private Context mContext;
    private Connection connection;
    private boolean isLoggedIn;

	public Parser(Context context) {
		mContext = context;

            SharedPreferences myPrefs = mContext.getSharedPreferences("session_cookie", Context.MODE_PRIVATE);
            String loggedin = myPrefs.getString("vbscanpassword",null);
            if(loggedin == null) {
                isLoggedIn = false;
            } else {
                isLoggedIn = true;
            }
	}

    private void showErrorMsg(final String errormsg) {

        ((MainActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, errormsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    private void Connect(String url) throws NullPointerException, IOException  {
        Map<String, String> cookie = LoginHandler.getSessionCookie(mContext);

        SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        int timeout;
        try {
            String to = appPrefs.getString("connection_timeout", "3000");
            timeout = Integer.parseInt(to);
        } catch (Exception e) {
            timeout = 3000;
            e.printStackTrace();
        }

        if(haveNetworkConnection()) {
            // Check for cookie
            if(!cookie.isEmpty()) {
                // Connect with cookie
                currentSite = LoginHandler.cookieConnect(url, cookie);
            } else {
                // Connect without cookie
                currentSite = LoginHandler.basicConnect(url);
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
            Connect("https://www.flashback.org/profile.php?do=editoptions");
        } catch(NullPointerException e) {
            e.printStackTrace();
            showErrorMsg(e.getMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMsg("Some IOException. Possibly timeout.. Go back and try again.");
            return false;
        }

        String numPostsPerPage = currentSite.select("form table.tborder tbody fieldset:contains(Antal inlägg att visa per sida) select option[selected=selected]").attr("value");
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


	public ArrayList<Forum> getCategoryContent(String url) {
        ArrayList<Forum> forumList = new ArrayList<Forum>();
        try {
            Connect(url);
        } catch(NullPointerException e) {
            e.printStackTrace();
            showErrorMsg(e.getMessage());
            return forumList;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMsg("Some IOException. Possibly timeout.. Go back and try again.");
            return forumList;
        }

		Elements forums = currentSite.select("tr td.alt1Active");

		String forumName = null;
		String forumLink = null;
		String forumInfo = null;
		int forumSize = 0;
		String categoryName = null;
		categoryName = currentSite.select(".list-forum-title").text();

		for(Element f : forums) {

			Elements n;

			forumName = f.select("a").text();
			forumLink = f.select("a[href]").attr("abs:href");
			forumInfo = f.select(".forum-summary").text();
			//forumSize = getSize(forumLink);

			n = f.select(":has(.icon-subforum");
			if(n.size() == 0) {
				Forum top = new Forum();
				top.setName(forumName);
				top.setUrl(forumLink);
				top.setInfo(forumInfo);
				top.setSize(forumSize);
				forumList.add(top);
			}
		}
		return forumList;
	}



    public int getSize() {
		Document temp = currentSite;
		int size = 0;


        Element e = currentSite.select(".tborder tbody tr td.alignr.navcontrolbar div.pagenav.nborder.fr table tbody tr td.vbmenu_control.smallfont2.delim").first();
		if(e != null) {
			String page = e.text();
			String[] split = page.split(" ");
			size = Integer.parseInt(split[3]);
		} else {
			size = 1;
		}

		currentSite = temp;
		return size;
	}

	public ArrayList<Thread> getForumContents(String url) throws Exception {
		ArrayList<Thread> mThreads = new ArrayList<Thread>();

        try {
            Connect(url);
        } catch(NullPointerException e) {
            e.printStackTrace();
            showErrorMsg(e.getMessage());
            return mThreads;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMsg("Some IOException. Possibly timeout.. Go back and try again.");
            return mThreads;
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
		// Get all non-sticky threads, name and href
		// #threadslist tr:not(.tr_sticky) td.alt1.threadslistrow div a[id^=thread]
		// Get all threads, name and href
		// #threadslist tr td.alt1.threadslistrow div a[id^=thread]
		// Get last page
		// #threadslist tr td.alt1.threadslistrow div a[class^=thread-pagenav-lastpage]
		// Get author
		// #threadslist tr td.alt1.threadslistrow div.smallfont
		// Number of views
		// #threadslist tr td.alt2.td_views

        int size = getSize();
        SharedPrefs.savePreference(mContext, "forum_size", "size", size);
		Elements threads = currentSite.select("#threadslist tr:not(:has(td.alt1.threadslist-announcement)):has(td.alt1.td_status):not(.tr_sticky)");
        Elements sticky = currentSite.select("#threadslist tr.tr_sticky");

        for(Element st : sticky) {
            name = st.select("td.alt1.td_title a[id^=thread_title]").text();
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
                //if(!name.equals("")) {
                    // Create new thread-object
                Thread thread = new Thread();
                // Set values
                thread.setThreadName(name);

                thread.setThreadAuthor(author);
                thread.setLastPageUrl(lastPage);
                thread.setNumPages(pages);
                thread.setThreadLink(link);
                thread.setThreadReplies(numReplies);
                thread.setThreadViews(views);
                thread.setSticky(true);
                thread.setLocked(locked);
                thread.setLastPost(lastPost);
                // Add to list
                mThreads.add(thread);
            //}
        }

		for(Element e : threads) {
            name = e.select("td.alt1.td_title a[id^=thread_title]").text();
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
            //if(!name.equals("")) {
                // Create new thread-object
                Thread thread = new Thread();
                // Set values
                thread.setThreadName(name);

                thread.setThreadAuthor(author);
                thread.setLastPageUrl(lastPage);
                thread.setNumPages(pages);
                thread.setThreadLink(link);
                thread.setThreadReplies(numReplies);
                thread.setThreadViews(views);
                thread.setSticky(false);
                thread.setLocked(locked);
                thread.setLastPost(lastPost);
                // Add to list
                mThreads.add(thread);
            //}
        }
		return mThreads;
	}

    public int getThreadNumPages(String url) {
        try {
            Connect(url);
        } catch(NullPointerException e) {
            e.printStackTrace();
            showErrorMsg(e.getMessage());
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMsg("Failed to load number of pages.");
            return 1;
        }
        int number;
        String numPages = currentSite.select("td.vbmenu_control.smallfont2.delim").text();
        String[] array = numPages.split(" ");

        try {
            String page = array[3];
            number = Integer.parseInt(page);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Exception parsing number of pages caught! And fixed.");
            number = 1;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            number = 1;
        }

        return number;
    }

    public boolean setUserId() {
        try {
            Connect("https://www.flashback.org/");
        } catch(NullPointerException e) {
            e.printStackTrace();
            return false;
            //showErrorMsg(e.getMessage());
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
            //System.out.println("UserID: " + userId);
            return true;
        } catch(NumberFormatException e) {
            e.printStackTrace();
            return false;
        }

    }

	public ArrayList<Post> getThreadContent(String url) {
        ArrayList<Post> postArrayList = new ArrayList<Post>();
        try {
            Connect(url);
        } catch(NullPointerException e) {
            e.printStackTrace();
            showErrorMsg(e.getMessage());
            return postArrayList;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMsg("Some IOException. Possibly timeout.. Go back and try again.");
            return postArrayList;
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
		Elements message;

		Elements posts = currentSite.select("div#posts div[id^=edit]");
		for(Element post : posts) {
			final Post newPost = new Post();

			date = post.select("table tbody tr td.thead.post-date").text();
            date = date.substring(0, date.indexOf("#"));
			orderNr = "#" + post.select("table tbody tr td.thead.post-date span a").text();
            postUrl = post.select("table tbody tr td.thead.post-date span a").attr("abs:href");
			author = post.select("table tbody tr[style^=vertical] td.alt2.post-left div[id^=postmenu]").text();
			membertype = post.select("table tbody tr[style^=vertical] td.alt2.post-left table").text();
			online = post.select("table tbody tr[style^=vertical] td.alt2.post-left table tbody tr td div").attr("title");
			userProfileUrl = post.select("table tbody tr[style^=vertical] td.alt2.post-left div[class^=smallfont] a").attr("abs:href");
			avatarurl = post.select("table tbody tr[style^=vertical] td.alt2.post-left div[class^=smallfont] a img").attr("src");
			regdate = post.select("table tbody tr[style^=vertical] td.alt2.post-left div[class=smallfont] div:contains(Reg)").text();
			numposts = post.select("table tbody tr[style^=vertical] td.alt2.post-left div.post-user div.post-user-info.smallfont div:nth-child(2)").text();
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

			message = post.select("table tbody tr[style^=vertical] td.alt1.post-right div[id^=post_message]");

            // Links in posts.
            // div#posts div[id^=edit] td.alt1.post-right div.post_message a
            for(int i = 0; i < post.select("td.alt1.post-right div.post_message a").size(); i++) {
                String text = post.select("td.alt1.post-right div.post_message a").get(i).attr("href");
                if(!text.contains("flashback.org") && text.startsWith("/leave.php") && !(text.length() < 14) ) {
                    try {
                        text = text.substring(13);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        text = "[FB-ParseError] Please report bug in official thread";
                    }

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

            // Remove codetags within spoilers
            message.select("div.alt2.post-bbcode-spoiler div[style=margin:5px 10px;]").before("[SPOILAD KOD]<br>");
            message.select("div.alt2.post-bbcode-spoiler div[style=margin:5px 10px;]").remove();
            message.select("div.alt2.post-bbcode-spoiler div[style=margin:20px; margin-top:5px]").before("[SPOILAD KOD]<br>");
            message.select("div.alt2.post-bbcode-spoiler div[style=margin:20px; margin-top:5px]").remove();

			// Remove spoiler headers
			for(int i = 0; i < message.select("div[style=margin:5px 20px 20px 20px]").size(); i++) {
				message.select("div[style=margin:5px 20px 20px 20px] div.smallfont").remove();
			}
			// Remove quote header
			message.select("div.post-quote-holder > div.smallfont.post-quote-title").remove();

            // Replace smileys with [IMAGE...] tags
            for(int i = 0; i < message.size(); i++) {
                for(int j = 0; j < message.get(i).select("img").size(); j++) {
                    message.get(i).select("img").get(j).before("[IMAGE"+message.get(i).select("img").get(j).attr("title").toUpperCase()+"]");
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

            for(int i = 0; i < quotesize; i++) {
                //System.out.println(message);
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
                    } catch(Exception e) {
                        e.printStackTrace();
                        n = "[FLASHBACK-APP]ERROR-Name";
                    }

                    String text;
                    try {
                        text = message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote div:nth-child(2)").get(i).text().trim();
                    } catch(Exception e) {
                        e.printStackTrace();
                        text = "[FLASHBACK-APP]ERROR-Msg";
                    }

                   // String name = message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote strong").get(i).text().trim();
                    String name = n.trim();
                    //message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote:has(div)").get(i).html("<strong>"+ name +"</strong>{quote_text}<div>"+text+"</div>{/quote_text}");
                    message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote").get(i).html("<strong>"+ name +"</strong>{quote_text}<div>"+text+"</div>{/quote_text}");
                } else {
                    //String text = message.select("div.post-quote-holder table.p2-4 tbody tr > td.alt2.post-quote:not(:has(div))").get(0).text().trim();
                    String text = quotetext.trim();
                    try {
                        // https://www.flashback.org/t2267536p44
                        message.select("div.post-quote-holder table.p2-4 tbody tr > td.alt2.post-quote").get(i).html("{anon_quote}{/anon_quote}{quote_text}<div>"+text+"</div>{/quote_text}");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            // Replace stuff in codetags
            for(Element element : message.select("pre.alt2.post-bbcode-code")) {
                element.text(element.text().replace(" ", "[SPACE]"));
                element.text(element.text().replace("&nbsp;", "[SPACE]"));
                element.text(element.text().replaceAll("\\t", "[SPACE][SPACE][SPACE][SPACE]"));
                element.text(element.text().replaceAll("\\r", "[newline]"));
            }

            // Surround name of the quotee [newline]
            message.select("div.post-quote-holder table.p2-4 tbody tr td.alt2.post-quote strong").before("{quoter_name}").after("{/quoter_name}");
            //System.out.println(message);


			String site = message.toString();
            site = site.replaceAll("(?i)<br[^>]*> *", "[newline]");
            site = site.replace("&nbsp;", "[SPACE]");
			Element elem = Jsoup.parse(site);

			site = elem.text().replace("<", "&lt;").replace(">", "&gt;");

			Document doc =  Jsoup.parseBodyFragment(site);

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
            System.out.println();
			// Temporarily remove code- and php-tags
			//doc.select("codetag").remove();
			//doc.select("phptag").remove();

			final ArrayList<String[]> postarr = new ArrayList<String[]>();
			final boolean[] quoteOpen = new boolean[] {false};
			final boolean[] messageOpen = new boolean[] {false};
			final boolean[] spoilerOpen = new boolean[] {false};
			final boolean[] quotespoilerOpen = new boolean[] {false};
			final boolean[] quoteHeaderOpen = new boolean[] {false};
            final boolean[] phptagOpen = new boolean[] {false};
            final boolean[] anonQuoteHeaderOpen = new boolean[] {false};

			Elements body = doc.select("body");

			body.traverse(new NodeVisitor() {

				@Override
				public void head(Node node, int level) {
					if(node instanceof TextNode && node.toString().length() != 1) {

						if(newPost.getPostRows().get(newPost.getPostRows().size()-1)[0].equals("[QUOTEHEADER]") && quoteOpen[0] == true && quoteHeaderOpen[0] == true) {
                            String quoteheader = ((TextNode) node).text();
                            quoteheader = quoteheader.replace("<", "&lt;");
                            quoteheader = quoteheader.replace(">", "&gt;");
                            quoteheader = Jsoup.parse(quoteheader).text();
                            quoteheader = quoteheader.replaceAll("\\s*\\[newline\\]\\s*", "\n");
                            quoteheader = quoteheader.replace("&lt;", "<");
                            quoteheader = quoteheader.replace("&gt;", ">");
                            quoteheader = quoteheader.replace("[SPACE]", " ");
                            newPost.getPostRows().get(newPost.getPostRows().size()-1)[1] = quoteheader.trim();

						}
						if(quoteOpen[0] == true && quotespoilerOpen[0] == false && quoteHeaderOpen[0] == false && anonQuoteHeaderOpen[0] == false) {
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

						if(quoteOpen[0] == true && quotespoilerOpen[0] == true) {
							if(newPost.getPostRows().get(newPost.getPostRows().size()-1)[0].equals("[QUOTESPOILER]")) {
                                String quotespoilertext = ((TextNode) node).text();
                                quotespoilertext = quotespoilertext.replace("<", "&lt;");
                                quotespoilertext = quotespoilertext.replace(">", "&gt;");
                                quotespoilertext = Jsoup.parse(quotespoilertext).text();
                                quotespoilertext = quotespoilertext.replaceAll("\\s*\\[newline\\]\\s*", "\n");
                                quotespoilertext = quotespoilertext.replace("&lt;", "<");
                                quotespoilertext = quotespoilertext.replace("&gt;", ">");
                                quotespoilertext = quotespoilertext.replace("[SPACE]", " ");
                                newPost.getPostRows().get(newPost.getPostRows().size()-1)[1] = quotespoilertext.trim();
							}
						}
						if(quoteOpen[0] == false && quotespoilerOpen[0] == false && spoilerOpen[0] == true) {
							if(newPost.getPostRows().get(newPost.getPostRows().size()-1)[0].equals("[SPOILER]")) {
                                String spoilertext = ((TextNode) node).text();
                                spoilertext = spoilertext.replace("<", "&lt;");
                                spoilertext = spoilertext.replace(">", "&gt;");
                                spoilertext = Jsoup.parse(spoilertext).text();
                                spoilertext = spoilertext.replaceAll("\\s*\\[newline\\]\\s*", "\n");
                                spoilertext = spoilertext.replace("&lt;", "<");
                                spoilertext = spoilertext.replace("&gt;", ">");
                                spoilertext = spoilertext.replace("[SPACE]", " ");
                                newPost.getPostRows().get(newPost.getPostRows().size()-1)[1] = spoilertext.trim();
							}
						}
						if(quoteOpen[0] == false && quotespoilerOpen[0] == false && spoilerOpen[0] == false && phptagOpen[0] == false) {
                            String msg = ((TextNode) node).text();
                            msg = msg.replace("<", "&lt;");
                            msg = msg.replace(">", "&gt;");
                            msg = Jsoup.parse(msg).text();
                            msg = msg.replaceAll("\\s*\\[newline\\]\\s*", "\n");
                            msg = msg.replace("&lt;", "<");
                            msg = msg.replace("&gt;", ">");
                            msg = msg.replace("[SPACE]", " ");
                            newPost.addRow("[MESSAGE]", msg.trim());
						}
                        if(quoteOpen[0] == false && quotespoilerOpen[0] == false && spoilerOpen[0] == false && phptagOpen[0] == true) {
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

					if(node instanceof Element) {
						if(node.nodeName().equals("body")) {
                            newPost.addRow("[POSTHEADER]", "");
						}
						if(node.nodeName().equals("quotespoiler")) {
                            newPost.addRow("[QUOTESPOILER]", "");
							quotespoilerOpen[0] = true;
						}
                        if(node.nodeName().equals("quote")) {
                            quoteOpen[0] = true;
                        }
						if(node.nodeName().equals("quoter_name")) {
                            newPost.addRow("[QUOTEHEADER]", "");
							//quoteOpen[0] = true;
							quoteHeaderOpen[0] = true;
						}
                        if(node.nodeName().equals("anon_quote")) {
                            newPost.addRow("[ANONQUOTEHEADER]", "");
                            anonQuoteHeaderOpen[0] = true;
                        }
						if(node.nodeName().equals("spoiler")) {
                            newPost.addRow("[SPOILER]", "");
							spoilerOpen[0] = true;
						}
                        if(node.nodeName().equals("phptag")) {
                            newPost.addRow("[PHPTAGHEADER]", "");
                            phptagOpen[0] = true;
                        }
					}
				}

				@Override
				public void tail(Node node, int level) {
					if(node instanceof Element) {
						if(node.nodeName().equals("quote_text")) {
                            newPost.addRow("[QUOTEFOOTER]", "");
							quoteOpen[0] = false;
						}
						if(node.nodeName().equals("quotespoiler")) {
							quotespoilerOpen[0] = false;
						}
						if(node.nodeName().equals("quoter_name")) {
							quoteHeaderOpen[0] = false;
						}
						if(node.nodeName().equals("spoiler")) {
							spoilerOpen[0] = false;
						}
                        if(node.nodeName().equals("phptag")) {
                            newPost.addRow("[PHPTAGFOOTER]", "");
                            phptagOpen[0] = false;
                        }
                        if(node.nodeName().equals("anon_quote")) {
                            anonQuoteHeaderOpen[0] = false;
                        }
						if(node.nodeName().equals("body")) {
                            newPost.addRow("[POSTFOOTER]", "");
                            newPost.getPostRows().get(newPost.getPostRows().size()-1)[3] = newPost.getAuthor();
                            newPost.getPostRows().get(newPost.getPostRows().size()-1)[4] = newPost.getOrderNr();
						}

					}
				}
			});

            postArrayList.add(newPost);
		}
        if(postArrayList.isEmpty()) throw new IllegalStateException("Failed to parse posts. List is empty.");

        return postArrayList;
	}

    public ArrayList<Item_CurrentThreads> getCurrent(String url) {

        ArrayList<Item_CurrentThreads> currentList = new ArrayList<Item_CurrentThreads>();
        try {
            Connect(url);
        } catch(NullPointerException e) {
            e.printStackTrace();
            showErrorMsg(e.getMessage());
            return currentList;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMsg("Some IOException. Possibly timeout.. Go back and try again.");
            return currentList;
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

            categoryName = categories.get(i).select("tr:eq(0) td:eq(1)").text();
            Item_CurrentThreads newItem = new Item_CurrentThreads();
            newItem.mType = CurrentThreadsAdapter.ITEM_HEADER;
            newItem.mHeadline = categoryName;
            currentList.add(newItem);
            for(int j = 0; j < categories.get(i).select("tr:not(:first-child)").size(); j++) {

                threadLink = categories.get(i).select("tr:not(:first-child) td:eq(1) a:first-child").get(j).attr("abs:href");
                threadName = categories.get(i).select("tr:not(:first-child) td:eq(1) a:first-child").get(j).text();
                forumName = categories.get(i).select("tr:not(:first-child) td:eq(1) a:eq(1)").get(j).text();
                readers = categories.get(i).select("tr:not(:first-child) td:eq(3)").get(j).text();
                views  = categories.get(i).select("tr:not(:first-child) td:eq(4)").get(j).text();
                replies = categories.get(i).select("tr:not(:first-child) td:eq(5)").get(j).text();
                Item_CurrentThreads newThread = new Item_CurrentThreads();
                newThread.mType = CurrentThreadsAdapter.ITEM_ROW;
                newThread.mHeadline = threadName;
                newThread.mReaders = readers;
                newThread.mViews = views;
                newThread.mReplies = replies;
                newThread.mSourceForum = forumName;
                newThread.mLink = threadLink;
                currentList.add(newThread);
            }
        }
        return currentList;
    }

    public ArrayList<Item_CurrentThreads> getNewThreads(String url) {

        ArrayList<Item_CurrentThreads> newList = new ArrayList<Item_CurrentThreads>();
        try {
            Connect(url);
        } catch(NullPointerException e) {
            e.printStackTrace();
            showErrorMsg(e.getMessage());
            return newList;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMsg("Some IOException. Possibly timeout.. Go back and try again.");
            return newList;
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

            Item_CurrentThreads newThread = new Item_CurrentThreads();
            newThread.mHeadline = threadName;
            newThread.mReaders = readers;
            newThread.mViews = views;
            newThread.mReplies = replies;
            newThread.mSourceForum = forumName;
            newThread.mLink = threadLink;
            newList.add(newThread);
        }
        return newList;
    }

    public void getPrivateMessages(String url) {
        // Get categories
        // form[method=post] table.tborder.p2-4 tbody[id]

    }

}
