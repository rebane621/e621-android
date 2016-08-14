package de.e621.rebane;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.URLDecoder;
import java.net.URLEncoder;

import de.e621.rebane.SQLite.SQLiteDB;
import de.e621.rebane.a621.R;
import de.e621.rebane.activities.SettingsActivity;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLTask;

public class LoginManager {

    Context context;
    SQLiteDB db;
    static String URLappendix = null;
    static String username = null;
    static Boolean loggedIn = false;
    static Boolean isBusy = false;
    static Integer userid = null;
    static Integer avatarid = null;
    static String avatarurl = null;

    public LoginManager(Context context, SQLiteDB openedDB) {
        this.context = context;
        db = openedDB;
    }

    public boolean isLoggedIn() {
        if (URLappendix == null) getLogin();
        return loggedIn;
    }

    /** If this funtion returns null, you are not logged in */
    public String getLogin() {
        if (!loggedIn) {
            String hash = db.getValue("password");
            String user = db.getValue("username");
            if (hash == null || user == null || hash.isEmpty() || user.isEmpty()) return null;
            URLappendix = "login=" + user + "&password_hash=" + hash;
            username = user;
            loggedIn = true;
            String val = db.getValue("userid");
            if (val != null) {
                try { userid = Integer.parseInt(val); }
                catch (Exception e) { userid = null; }
            }
            val = db.getValue("useravatarid");
            if (val != null) {
                try { avatarid = Integer.parseInt(val); }
                catch (Exception e) { avatarid = null; }
            }
            val = db.getValue("useravatarurl");
            avatarurl = val==null?null: URLDecoder.decode(val);
        }
        return URLappendix;
    }
    /** save to use, will return empty string if not logged in */
    public String getLogin(String prefix) {
        String apx = getLogin();
        return (apx == null ? "" : prefix + apx);
    }

    /** attempts to log in, if no other request is already running
     * On success getLogin() will not return null
     */
    public void login(String username, String password) { login(username, password, null, null); }
    /** attempts to log in, if no other request is already running
     * On success getLogin() will not return null
     */
    public void login(final String loginname, String password, final View pbar, final Activity closeme) {
        if (isBusy) return; isBusy=true;

        pbar.setVisibility(View.VISIBLE);
        db.open();
        db.setValue("username", loginname);
        final String baseURL = db.getValue(SettingsActivity.SETTINGBASEURL);
        (new LoginTask(context, db, context.getResources().getString(R.string.requestUserAgent)){
            @Override protected void onPostExecute(Boolean result) {
                if (result) {
                    URLappendix = TakeMyLoginString;
                    username = loginname;
                    Toast.makeText(context, "Login Successfull!", Toast.LENGTH_SHORT).show();
                    try { closeme.finish(); } catch (Exception e) {e.printStackTrace();} // catch in case closeme is null or activity already closed

                    new XMLTask(context) {
                        @Override protected void onPostExecute(XMLNode result) {
                            try {
                                for (XMLNode n : result.getChildren()) {
                                    if (n.getAttribute("name").equalsIgnoreCase(username)) {    //we have to double check
                                        userid = Integer.parseInt(n.getAttribute("id"));
                                        db.setValue("userid", userid.toString());
                                        avatarid = Integer.parseInt(n.getAttribute("avatar_id"));
                                        db.setValue("useravatarid", avatarid.toString());
                                    }
                                }
                            } catch (Exception eee) {
                                eee.printStackTrace();
                                onExecutionFailed(eee);
                            }  //well, fack it

                            if (avatarid != null && avatarid>0) {
                                new XMLTask(context) {
                                    @Override protected void onPostExecute(XMLNode result) {
                                        if (result==null || result.getChildCount()==0) return;
                                        avatarurl = result.getFirstChildText("sample_url");
                                        db.setValue("useravatarurl", URLEncoder.encode(avatarurl));
                                    }
                                }.execute(baseURL + "post/show.xml?id=" + avatarid);
                            }
                        }

                        @Override public void onExecutionFailed(Exception exc) {
                            userid=null;
                            avatarid=null;
                            avatarurl=null;
                            Toast.makeText(context, "Unable to get user data", Toast.LENGTH_LONG).show();
                        }
                    }.execute(baseURL + "user/index.xml?name=" + username);
                } else {
                    URLappendix = null;
                    username = null;
                    Toast.makeText(context, "Incorrect Username/Passwod!", Toast.LENGTH_SHORT).show();
                }
                loggedIn = result;
                isBusy=false;
                try { pbar.setVisibility(View.GONE); } catch (Exception e) {}   // catch in case activity was closed
            }
        }).execute(loginname, password);
    }

    public void logout() {
        URLappendix = null;
        username = null;
        userid = null;
        avatarid = null;
        loggedIn = false;
        db.setValue("password", "");
        db.setValue("username", "");
        db.setValue("userid", "");
        db.setValue("useravatarid", "");
        db.setValue("useravatarurl", "");
    }

    public String getUsername() {
        return username;
    }
    public Integer getUserid() { return userid; }
    public Integer getAvatarid() { return avatarid; }
    public String getAvatarurl() { return avatarurl; }

    private static LoginManager instance = null;
    public static LoginManager getInstance(Context context, SQLiteDB openedDB) {
        if (instance == null) instance = new LoginManager(context, openedDB);
        return instance;
    }
}
