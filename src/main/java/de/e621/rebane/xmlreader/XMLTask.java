package de.e621.rebane.xmlreader;

import android.content.Context;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import de.e621.rebane.a621.R;

public abstract class XMLTask extends AsyncTask<String, Void, XMLNode> {

    public XMLTask(Context context) {
        userAgent = context.getString(R.string.requestUserAgent);
    }

    String url;
    String userAgent;
    XMLReader reader;

    @Override
    protected XMLNode doInBackground(String... params) {
        url = params[0];
        try {
            HttpURLConnection urlc = (HttpURLConnection) new URL(url).openConnection();
            urlc.addRequestProperty("User-Agent", userAgent);
            urlc.setRequestMethod("GET");
            urlc.setChunkedStreamingMode(256);
            urlc.setConnectTimeout(15000);
            urlc.setReadTimeout(15000);
            urlc.setUseCaches(false);
            InputStream bis = urlc.getInputStream();
            Logger.getLogger("a621").info("Connected with response code " + urlc.getResponseCode() + ": " + urlc.getResponseMessage());

            XMLNode result = (reader = new XMLReader()).parse(bis);

            return result;
        } catch (Exception exc) {
            Logger.getLogger("a621").warning("Unable to connect!");
            Logger.getLogger("a621").warning(exc.getMessage());
            exc.printStackTrace();
            return null;
        }
    }

    abstract protected void onPostExecute(XMLNode result);

}