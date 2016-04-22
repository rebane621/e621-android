package de.e621.rebane;

import android.content.Context;

import de.e621.rebane.SQLite.SQLiteDB;
import de.e621.rebane.a621.R;

/**
 * Created by Michael on 18.04.2016.
 */
public class LoginManager {

    Context context;
    SQLiteDB db;
    static String URLappendix = null;
    static Boolean isLoggedId = false;
    static Boolean isBusy = false;

    public LoginManager(Context context, SQLiteDB openedDB) {
        this.context = context;
        db = openedDB;
    }

    /** If this funtion returns null, you are not logged in */
    public String getLogin() {
        if (!isLoggedId) {
            String hash = db.getValue("password");
            String user = db.getValue("username");
            if (hash == null || user == null || hash.isEmpty() || user.isEmpty()) return null;
            URLappendix = "login="+user+"&password_hash="+hash;
            isLoggedId = true;
        }
        return URLappendix;
    }

    /** attempts to log in, if no other request is already running
     * On success getLogin() will not return null
     */
    public void login(String username, String password) {
        if (isBusy) return; isBusy=true;
        db.setValue("username", username);
        (new LoginTask(context, db, context.getResources().getString(R.string.requestUserAgent)){
            @Override protected void onPostExecute(Boolean result) {
                if (result)
                    URLappendix = TakeMyLoginString;
                else
                    URLappendix = null;
                isLoggedId = result;
                isBusy=false;
            }
        }).execute(username, password);
    }
}
