package de.e621.rebane;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import de.e621.rebane.SQLite.SQLiteDB;
import de.e621.rebane.activities.SettingsActivity;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLReader;

public abstract class LoginTask extends AsyncTask<String, Void, Boolean> {

    SQLiteDB db;
    String userAgent;
    Context context;

    public String TakeMyLoginString = null;

    public LoginTask(Context context, SQLiteDB openedDB, String userAgent) {
        db = openedDB;
        this.userAgent = userAgent;
        this.context = context;
    }

    @Override protected Boolean doInBackground(String... login) {

        String url = db.getValue(SettingsActivity.SETTINGBASEURL);
        url += "user/login.xml?name=" + login[0] + "&password=" + login[1];
        XMLReader reader = new XMLReader();
        boolean ret = false;
        try {
            HttpURLConnection urlc = (HttpURLConnection) new URL(url).openConnection();
            urlc.setRequestMethod("GET");
            urlc.addRequestProperty("User-Agent", userAgent);
            urlc.setChunkedStreamingMode(256);
            urlc.setConnectTimeout(30000);
            urlc.setReadTimeout(30000);
            urlc.setUseCaches(false);
            InputStream bis = urlc.getInputStream();
            Logger.getLogger("a621").info("Connected with response code " + urlc.getResponseCode() + ": " + urlc.getResponseMessage());

            XMLNode result = XMLReader.parse(bis);

            if (result.getType().equals("error")) {
                db.setValue("password", "");
                Toast.makeText(context, result.getAttribute("type"), Toast.LENGTH_LONG).show();
                ret = false;
            } else {
                XMLNode c = result.children().get(0);
                String hash = c.getAttribute("password_hash");
                TakeMyLoginString = "login="+login[0]+"&password_hash="+hash;
                db.setValue("password", hash);
                ret = true;
            }

        } catch (Exception e) {
            ret = false;
        }
        XMLReader.clearReader();
        return ret;
    }

    abstract protected void onPostExecute(Boolean result);
}
