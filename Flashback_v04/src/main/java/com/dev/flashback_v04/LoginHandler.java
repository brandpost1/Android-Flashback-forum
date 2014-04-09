package com.dev.flashback_v04;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.dev.flashback_v04.interfaces.RedirectCallback;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Viktor on 2013-06-24.
 */
public class LoginHandler {

    public static Document basicConnect(String site) throws IOException {
        URL url = new URL(site);
        Document current = null;
        HttpsURLConnection connection = null;

        connection = (HttpsURLConnection)url.openConnection();
		connection.setUseCaches(true);

        InputStream response;
        response = connection.getInputStream();

        current = Jsoup.parse(response, null, site);
        response.close();

        return current;
    }

    public static Document cookieConnect(String site, Map<String, String> cookie, Context context) throws IOException {
		URL url;
		try {
			url = new URL(site);
		} catch (IOException e) {
			throw new IOException("Malformed url - " + site);
		}

		Document current = null;
		HttpsURLConnection connection = null;

		try {
			connection = (HttpsURLConnection) url.openConnection();
		} catch (IOException e) {
			throw new IOException("Failed to open connection");
		}
		//connection.setDoOutput(true);
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(0);
		connection.setUseCaches(true);

		// Get sessioncookie
		Map<String, String> cookies = cookie;

		// Convert cookie to correct format
		String cookiestring = cookies.toString().trim().replace("{", "").replace("}", "").replace(",", ";");

		// Some headers
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36");
		connection.setRequestProperty("Referer", "https://www.flashback.org/");
		connection.setRequestProperty("Cookie", cookiestring);

		// Get cookies from response
		List<String> newcookies = connection.getHeaderFields().get("Set-Cookie");
		Map<String, List<String>> newcookies2 = connection.getHeaderFields();
		if(newcookies != null) {
			// Get "old" cookies
			Map<String, String> oldcookies = getSessionCookie(context);

			// Store in Map
			for (String newcookie : newcookies) {
				String key;
				String value;
				String temp = newcookie.split(";", 2)[0];
				key = temp.split("=")[0];
				value = temp.split("=")[1];
				oldcookies.put(key, value);
			}
			setSessionCookie(oldcookies, context);
		}

		// Make request
		InputStream response;
		try {
			response = connection.getInputStream();
		} catch (IOException e) {
			throw new IOException("Failed to get Inputstream");
		}



        // Parse response
        current = Jsoup.parse(response, null, site);

        response.close();

        return current;
    }

    public static boolean login(String userName, String password, Context context) throws IOException{
        URL url = null;
        HttpURLConnection connection = null;
        Map<String, String> mCookies = new HashMap<String, String>();
        try {
            url = new URL("https://www.flashback.org/login.php");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Malformed url");
        }


        String parameters = "";

        Map<String, String> params = new HashMap<String, String>();
        params.put("vb_login_username", userName);
        params.put("cookieuser", "1");
        params.put("vb_login_password", password);
        params.put("url", "/login.php");
        params.put("vb_login_md5password", "");
        params.put("vb_login_md5password_utf", "");
        params.put("do", "login");

        int count = 0;
        for(Map.Entry<String,String> entry : params.entrySet()) {
            parameters += entry.getKey() + "=" + entry.getValue();
            if(count < params.size() -1) {
                parameters += "&";
            }
            count++;
        }

        try {
            connection = (HttpsURLConnection)url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to open connection");
        }

        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(0);
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        try {
            connection.setRequestMethod("POST");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to set requestmethod");
        }


        // Some headers
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        connection.setRequestProperty("Origin", "https://www.flashback.org");
        connection.setRequestProperty("Host", "www.flashback.org");
        connection.setRequestProperty("Referer", "https://www.flashback.org/");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36");

        // Send post request
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(parameters);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Output failed");
        } finally {
            if(out != null)
                out.close();
        }

        // Get cookies from response
        List<String> cookies = connection.getHeaderFields().get("Set-Cookie");

        // Store in Map
        for (String cookie : cookies) {
            String key;
            String value;
            String temp = cookie.split(";", 2)[0];
            key = temp.split("=")[0];
            value = temp.split("=")[1];
            mCookies.put(key, value);
        }

        // Check if logged in by looking for a specific cookie
        if(mCookies.containsKey("vbscanpassword")) {
            // Save username
            SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            appPrefs.edit().putString("UserName", userName).commit();

            setSessionCookie(mCookies, context);
            //getPreferences(context);
            return true;
        }
        return false;
    }

    // Get some preferences for the logged in user
    private static void getPreferences(Context context) {
        Parser p = new Parser(context);
        p.getPreferences();
    }

    public static boolean loggedIn(Context context) {
        SharedPreferences sessionPrefs = context.getSharedPreferences("session_cookie", Context.MODE_PRIVATE);
        if(sessionPrefs.contains("vbscanpassword")) {
            return true;
        }
        return false;
    }

	public static boolean logout(Context context) {
		SharedPreferences sessionPrefs = context.getSharedPreferences("session_cookie", Context.MODE_PRIVATE);
		SharedPreferences userPrefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences otherprefs = context.getSharedPreferences("APP_SETTINGS", Context.MODE_PRIVATE);


        if(sessionPrefs.contains("vbscanpassword") || userPrefs.contains("ID")) {
            sessionPrefs.edit().clear().commit();
            userPrefs.edit().clear().commit();
            otherprefs.edit().putInt("Thread_Max_Posts_Page", 12).commit();

            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Already logged out", Toast.LENGTH_SHORT).show();
        }

		return true;
	}

	private static void setSessionCookie(Map<String, String> sessionCookie, Context context) {
		SharedPreferences sessionPrefs = context.getSharedPreferences("session_cookie", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sessionPrefs.edit();

		for (Map.Entry<String, String> entry : sessionCookie.entrySet()) {
			editor.putString(entry.getKey(), entry.getValue());
		}
		editor.commit();
	}

	public static Map<String, String> getSessionCookie(Context context) {
		SharedPreferences sessionPrefs = context.getSharedPreferences("session_cookie", Context.MODE_PRIVATE);

		Map<String, String> cookie = (Map<String, String>) sessionPrefs.getAll();
		return cookie;

	}

    /**
     * @param url
     * Skicka in en url av formen ,https://www.flashback.org/t2223883", vilket är en tråd i forumet.
     * Siffrorna i slutet kommer att plockas ut och användas som trådId för att skicka svaret.
     * @param message
     * Meddelandet som ska skickas.
     * Newline representeras av "\n" i det som skickas in, och kommer att ersättas med "<br />"
     * @param context
     * @throws IOException
     * Om något hände när svaret skickades, så som anslutningsproblem.
     */
    public static boolean postReply(String url, String message, Context context) throws IOException {
        if(loggedIn(context)) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://www.flashback.org/newreply.php");

            // Get user-id from sharedpreferences
            String userId = Integer.toString(SharedPrefs.getPreference(context, "user", "ID"));

            // Get post-id from url
            int indexOf = url.indexOf("/t")+2;
            String threadId = url.substring(indexOf);

            // Get cookies, and remake to appropriate format.
            Map<String, String> cookies = getSessionCookie(context);
            String mCookies = cookies.toString().trim().replace("{", "").replace("}", "").replace(",", ";");

            // Send in correct newline character - Apparently not needed any longer
            //message = message.replace("\n", "<br />");

            BasicNameValuePair[] params = {
                    new BasicNameValuePair("message", message),
                    new BasicNameValuePair("wysiwyg", "0"),
                    new BasicNameValuePair("s", ""),
                    new BasicNameValuePair("do", "postreply"),
                    new BasicNameValuePair("t", threadId),
                    new BasicNameValuePair("p", ""),
                    new BasicNameValuePair("posthash", ""),
                    new BasicNameValuePair("poststarttime", ""),
                    new BasicNameValuePair("loggedinuser", userId),
                    new BasicNameValuePair("sbutton", "Skicka svar"),
                    new BasicNameValuePair("signature", "1"),
                    new BasicNameValuePair("parseurl", "1"),
                    new BasicNameValuePair("disablesmilies", "0"),
                    new BasicNameValuePair("emailupdate", "9999"),    // 9999, 0, 1, 2, 3 - Prenumerera inte, Inget epost, omedelbar epost, dagligt epost, veckovis epost
                    new BasicNameValuePair("stoken", ""),
            };


            // Add some headers
            post.addHeader("Content-Type", "application/x-www-form-urlencoded");
            post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            post.addHeader("Cookie", mCookies);

            // Add parameters to post-object
            try {
                post.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), "ISO-8859-1"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // To:https://www.flashback.org/showthread.php?p=47728039&posted=1#p47728039 with status: 301 Show explanation HTTP/1.1 301 Moved Permanently
            // "Location:"
            try {
                HttpResponse resp = client.execute(post);
                //String body = EntityUtils.toString(resp.getEntity(), "UTF-8");
                if(resp.getStatusLine().getStatusCode() == 200) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                // Connection error
                e.printStackTrace();
                throw new IOException("There was a problem with the connection.");
            }
        } else {
            return false;
        }
    }

	public static boolean editPost(Context context, Bundle args) {
		if(loggedIn(context)) {
			URL url = null;
			HttpsURLConnection connection = null;

			try {
				url = new URL("https://www.flashback.org/editpost.php");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			// Get sessioncookie
			Map<String, String> cookies = getSessionCookie(context);
			// Convert
			String cookiestring = cookies.toString().trim().replace("{", "").replace("}", "").replace(",", ";");

			try {
				connection = (HttpsURLConnection) (url != null ? url.openConnection() : null);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(connection != null) {
				connection.setInstanceFollowRedirects(false);
				connection.setDoOutput(true);
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				connection.setRequestProperty("Cookie", cookiestring);
			}

			String reason;
			String title;
			String message;
			String postId;
			if(args != null) {

				reason = args.getString("Reason") != null ? args.getString("Reason") : "";
				title = args.getString("Title") != null ? args.getString("Title") : "";
				message = args.getString("Message") != null ? args.getString("Message") : "";
				postId = args.getString("PostId") != null ? args.getString("PostId") : "";

				Map<String, String> params = new HashMap<String, String>();
				params.put("reason", reason);
				params.put("title", title);
				params.put("message", message);
				params.put("wysiwyg", "0");
				params.put("s", "");
				params.put("do", "updatepost");
				params.put("p", postId);
				params.put("posthash", "");
				params.put("poststarttime", "");
				params.put("sbutton", "Spara ändringar");
				params.put("signature", "1");
				params.put("parseurl", "1");
				params.put("emailupdate", "9999");
				params.put("folderid", "0");
				params.put("stoken", "");

				// Convert parameters to single querystring
				String parameters = "";
				int count = 0;
				for(Map.Entry<String,String> entry : params.entrySet()) {
					parameters += entry.getKey() + "=" + entry.getValue();
					if(count < params.size() -1) {
						parameters += "&";
					}
					count++;
				}

				// Send post request
				DataOutputStream out;
				if(connection != null) {
					try {
						out = new DataOutputStream(connection.getOutputStream());
						out.writeBytes(parameters);
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				int responseCode;
				if(connection != null) {
					try {
						responseCode = connection.getResponseCode();
						if (responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
							// Likely a success
							return true;
						} else {
							return false;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}
		return false;
	}

	public static boolean addSubscription(Context context, Bundle args) {
		if(loggedIn(context)) {
			URL url = null;
			HttpsURLConnection connection = null;

			try {
				url = new URL("https://www.flashback.org/subscription.php");
			} catch (MalformedURLException e) {

			}
			// Get sessioncookie
			Map<String, String> cookies = getSessionCookie(context);
			// Convert
			String cookiestring = cookies.toString().trim().replace("{", "").replace("}", "").replace(",", ";");

			try {
				connection = (HttpsURLConnection) (url != null ? url.openConnection() : null);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(connection != null) {
				connection.setInstanceFollowRedirects(false);
				connection.setDoOutput(true);
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				connection.setRequestProperty("Cookie", cookiestring);
			}

			String threadId;
			String threadUrl;
			String emailUpdate;
			String folderId;
			if(args != null) {
				threadId = args.getString("ThreadId");
				threadUrl = args.getString("ThreadUrl");
				emailUpdate = (args.getString("Update") != null) ? args.getString("Update") : "0";
				folderId = (args.getString("FolderId") != null) ? args.getString("FolderId") : "0";

				Map<String, String> params = new HashMap<String, String>();
				params.put("s", "");
				params.put("do", "doaddsubscription");
				params.put("threadid", threadId);
				params.put("url", "");
				params.put("emailupdate", emailUpdate);
				params.put("folderid", folderId);
				params.put("stoken", "");

				// Convert parameters to single querystring
				String parameters = "";
				int count = 0;
				for(Map.Entry<String,String> entry : params.entrySet()) {
					parameters += entry.getKey() + "=" + entry.getValue();
					if(count < params.size() -1) {
						parameters += "&";
					}
					count++;
				}

				// Send post request
				DataOutputStream out;
				if(connection != null) {
					try {
						out = new DataOutputStream(connection.getOutputStream());
						out.writeBytes(parameters);
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				int responseCode;
				if(connection != null) {
					try {
						responseCode = connection.getResponseCode();
						if (responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
							// Likely a success
							return true;
						} else {
							return false;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
		return false;
	}

	public static boolean removeSubsription(Context context, Bundle args) {
		if(loggedIn(context)) {
			URL url = null;
			HttpsURLConnection connection = null;

			try {
				url = new URL("https://www.flashback.org/subscription.php");
			} catch (MalformedURLException e) {

			}
			// Get sessioncookie
			Map<String, String> cookies = getSessionCookie(context);
			// Convert
			String cookiestring = cookies.toString().trim().replace("{", "").replace("}", "").replace(",", ";");

			try {
				connection = (HttpsURLConnection) (url != null ? url.openConnection() : null);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(connection != null) {
				connection.setInstanceFollowRedirects(false);
				connection.setDoOutput(true);
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				connection.setRequestProperty("Cookie", cookiestring);
			}


			ArrayList<String> deleteThese;
			if(args != null) {
				deleteThese = args.getStringArrayList("DeleteBoxes");

				Map<String, String> params = new HashMap<String, String>();
				params.put("s", "");
				params.put("do", "dostuff");
				params.put("folderid", "all");
				for(String deletebox : deleteThese) {
					params.put(deletebox, "yes");
				}
				params.put("what", "delete");
				params.put("stoken", "");

				// Convert parameters to single querystring
				String parameters = "";
				int count = 0;
				for(Map.Entry<String,String> entry : params.entrySet()) {
					parameters += entry.getKey() + "=" + entry.getValue();
					if(count < params.size() -1) {
						parameters += "&";
					}
					count++;
				}

				// Send post request
				DataOutputStream out;
				if(connection != null) {
					try {
						out = new DataOutputStream(connection.getOutputStream());
						out.writeBytes(parameters);
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				int responseCode;
				if(connection != null) {
					try {
						responseCode = connection.getResponseCode();
						if (responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
							// Likely a success
							return true;
						} else {
							return false;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
		return false;
	}

	public static boolean sendPrivateMessage(Context context, Bundle bundle) {
		if(loggedIn(context)) {
			URL url = null;
			HttpsURLConnection connection = null;
			try {
				url = new URL("https://www.flashback.org/private.php");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			// Get sessioncookie
			Map<String, String> cookies = getSessionCookie(context);
			// Convert
			String cookiestring = cookies.toString().trim().replace("{", "").replace("}", "").replace(",", ";");

			try {
				connection = (HttpsURLConnection) (url != null ? url.openConnection() : null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(connection != null) {
				connection.setInstanceFollowRedirects(false);
				connection.setDoOutput(true);
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				connection.setRequestProperty("Cookie", cookiestring);
				connection.setRequestProperty("Referer", "https://www.flashback.org/private.php?do=newpm");
				try {
					connection.setRequestMethod("POST");
				} catch (ProtocolException e) {
					e.printStackTrace();
				}
			}

			// Get parameters from bundle
			String recipients = bundle.getString("Recipients");
			String title = bundle.getString("Header");
			String message = bundle.getString("Message");
			String token = bundle.getString("Token");
			boolean savecopy = bundle.getBoolean("SaveCopy");
			boolean convertlinks = bundle.getBoolean("ConvertLinks");

			// Optional parameters if for example replying or forwarding
			String pmId = (bundle.getString("PMId") != null) ? bundle.getString("PMId") : "";
			String forward = (bundle.getString("Forward") != null) ? bundle.getString("Forward") : "";


			// Form parameters
			Map<String, String> params = new HashMap<String, String>();
			params.put("recipients", recipients); // recipients
			params.put("title", title); // title
			params.put("message", message); // the message
			params.put("wysiwyg", "0"); // 0 or 1
			params.put("stoken", token);
			params.put("s", ""); //empty
			params.put("do", "insertpm"); // insertpm = new pm, insertpm = reply to pm,
			params.put("ajax", ""); // empty
			params.put("pmid", pmId); // if replying / forwarding a PM, insert the id of the pm being replied to
			params.put("forward", forward); // empty or 1
			params.put("sbutton", "Skicka meddelande");
			if(savecopy)
				params.put("savecopy", "1"); // 1 or 0
			if(convertlinks)
				params.put("parseurl", "1"); // 1 or 0

			// Convert parameters to single querystring
			String parameters = "";
			int count = 0;
			for(Map.Entry<String,String> entry : params.entrySet()) {
				parameters += entry.getKey() + "=" + entry.getValue();
				if(count < params.size() -1) {
					parameters += "&";
				}
				count++;
			}

			// Send post request
			DataOutputStream out;
			if(connection != null) {
				try {
					out = new DataOutputStream(connection.getOutputStream());
					out.writeBytes(parameters);
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			int responseCode;
			if(connection != null) {
				try {
					responseCode = connection.getResponseCode();
					if (responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
						// Likely a success
						return true;
					} else {
						return false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return false;
	}

    public static boolean postNewThread(RedirectCallback callback, String inForum, String subject, String message, Context context) throws IOException{
        if(loggedIn(context)) {
            URL url = null;
            HttpsURLConnection connection = null;

            try {
                url = new URL("https://www.flashback.org/newthread.php");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            connection = (HttpsURLConnection)url.openConnection();
            connection.setInstanceFollowRedirects(false);

            connection.setDoOutput(true);

            try {
                connection.setRequestMethod("POST");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }

            // Get sessioncookie
            Map<String, String> cookies = getSessionCookie(context);

            // Convert cookie to correct format
            String cookiestring = cookies.toString().trim().replace("{", "").replace("}", "").replace(",", ";");

            // Get user-id from sharedpreferences
            String userId = Integer.toString(SharedPrefs.getPreference(context, "user", "ID"));


            String forumId = inForum;
            String postSubject = subject;
            String postMessage = message;

            // Some headers for first request
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setRequestProperty("Cookie", cookiestring);

            // Get cookie from response, since apparently it's needed in a second request
            List<String> cookielist = connection.getHeaderFields().get("Set-Cookie");

            // Get cookievalue
            String hash = cookielist.get(0).split(";", 2)[0].split("=")[1];

            // Establish a new connection
            connection = (HttpsURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(false);

            // Some headers for the second connection.. Same as first
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setRequestProperty("Cookie", cookiestring);

            // Form parameters
            Map<String, String> params = new HashMap<String, String>();
            params.put("subject", postSubject);
            params.put("message", postMessage);
            params.put("wysiwyg", "0");
            params.put("f", forumId);
            params.put("do", "postthread");
            params.put("posthash", "");
            params.put("poststarttime", "");
            params.put("loggedinuser", userId);
            params.put("s", hash);
            params.put("sbutton", "Skicka nytt ämne");
            params.put("signature", "1");
            params.put("parseurl", "1");
            params.put("emailupdate", "9999");  // 9999, 0, 1, 2, 3 - Prenumerera inte, Inget epost, omedelbar epost, dagligt epost, veckovis epost
            params.put("disablesmilies", "1"); //Optional 1 = disable)
            params.put("folderid", "0");
            params.put("stoken", "");

            // Convert parameters to single querystring
            String parameters = "";
            int count = 0;
            for(Map.Entry<String,String> entry : params.entrySet()) {
                parameters += entry.getKey() + "=" + entry.getValue();
                if(count < params.size() -1) {
                    parameters += "&";
                }
                count++;
            }

            // Send post request
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(parameters);
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                // Probably successful
                String newLocation = connection.getHeaderField("Location");
                // Use newLocation to open the new thread
                callback.setRedirect(newLocation);
                return true;
            }

            InputStream response = connection.getInputStream();

			/*
            if(response != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                try {
                    for (String line; (line = reader.readLine()) != null;) {
                        System.out.println(line);
                    }
                } finally {
                    try { reader.close(); } catch (IOException logOrIgnore) {}
                }
            }
            */


            return false;
        }
        return false;
    }
}
