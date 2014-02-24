package com.dev.flashback_v04;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Viktor on 2013-06-24.
 */
public class LoginHandler {

    //TODO: Replace JSoup here

	public static boolean login(String userName, String password, Context context) {
		Connection.Response response = null;
        Map<String, String> mCookies;

		try {
			response = Jsoup.connect("https://www.flashback.org/login.php")
					.data("vb_login_username", userName,
                            "cookieuser", "1",
                            "vb_login_password", password,
                            "url", "",
                            "vb_login_md5password", "",
                            "vb_login_md5password_utf", "",
                            "do", "login")
					.userAgent("Mozilla")
					.method(Connection.Method.POST)
					.execute();
			if(response.hasCookie("vbscanpassword")) {
				mCookies = response.cookies();
				setSessionCookie(mCookies, context);
                getPreferences(context);
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		return false;
	}

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
    public static boolean testPost(String url, String message, Context context) throws IOException {
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

            // Send in correct newline character
            message = message.replace("\n", "<br />");

            BasicNameValuePair[] params = {
                    new BasicNameValuePair("message", message),
                    new BasicNameValuePair("wysiwyg", "1"),
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

            post.addHeader("Content-Type", "application/x-www-form-urlencoded");
            post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            post.addHeader("Cookie", mCookies);


            try {
                post.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), "ISO-8859-1"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                HttpResponse resp = client.execute(post);
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

	public static void postThread(String inForum, Context context) throws IOException {
		Connection.Response response = null;
		Map<String, String> cookies = getSessionCookie(context);

		try {
			response = Jsoup.connect("https://www.flashback.org/newthread.php")
					.data	(
							"subject", "",
							"message", "",
							"wysiwyg", "1",
							"f", "*forumid*",
							"do", "postthread",
							"posthash", "",
							"poststarttime", "",
							"loggedinuser", "",
							"sbutton", "Skicka nytt ämne",
							"signature", "1",
							"parseurl", "1",
							"emailupdate", "9999",   // 9999, 0, 1, 2, 3 - Prenumerera inte, Inget epost, omedelbar epost, dagligt epost, veckovis epost
							//"disablesmilies", "1", //Optional 1 = disable
							"stoken", ""
					)

					.cookies(cookies)
					.userAgent("Mozilla")
					.method(Connection.Method.POST)
					.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
