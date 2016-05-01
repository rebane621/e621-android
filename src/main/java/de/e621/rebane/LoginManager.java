package de.e621.rebane;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import de.e621.rebane.SQLite.SQLiteDB;
import de.e621.rebane.a621.R;

/**
 * Created by Michael on 18.04.2016.
 */
public class LoginManager {

    Context context;
    SQLiteDB db;
    static String URLappendix = null;
    static String username = null;
    static Boolean loggedIn = false;
    static Boolean isBusy = false;

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
        (new LoginTask(context, db, context.getResources().getString(R.string.requestUserAgent)){
            @Override protected void onPostExecute(Boolean result) {
                if (result) {
                    URLappendix = TakeMyLoginString;
                    username = loginname;
                    Toast.makeText(context, "Login Successfull!", Toast.LENGTH_SHORT).show();
                    try { closeme.finish(); } catch (Exception e) {e.printStackTrace();} // catch in case closeme is null or activity already closed
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
        loggedIn = false;
        db.setValue("password", "");
        db.setValue("username", "");
    }

    public String getUsername() {
        return username;
    }

    private static LoginManager instance = null;
    public static LoginManager getInstance(Context context, SQLiteDB openedDB) {
        if (instance == null) instance = new LoginManager(context, openedDB);
        return instance;
    }
}
