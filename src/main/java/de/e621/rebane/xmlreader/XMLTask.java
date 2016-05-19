package de.e621.rebane.xmlreader;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import de.e621.rebane.a621.R;

public abstract class XMLTask extends AsyncTask<String, Void, XMLNode> {

    public XMLTask(Context context) {
        userAgent = context.getString(R.string.requestUserAgent);
    }
    public XMLTask(Context context, Map<String, String> postData) {
        this(context);
        requestPostData = postData;
    }

    String url;
    String userAgent;
    XMLReader reader;
    Map<String, String> requestPostData = null;
    public void setPostData(Map<String, String> postData) {
        if (requestPostData == null)
            requestPostData = postData;
        else
            requestPostData.putAll(postData);
    }

    @Override
    protected XMLNode doInBackground(String... params) {
        url = params[0];
        try {
            HttpURLConnection urlc = (HttpURLConnection) new URL(url).openConnection();
            urlc.addRequestProperty("User-Agent", userAgent);
            urlc.setRequestMethod(requestPostData==null?"GET":"POST");
            urlc.setChunkedStreamingMode(256);
            urlc.setConnectTimeout(15000);
            urlc.setReadTimeout(15000);
            urlc.setUseCaches(false);

            if (requestPostData!=null) {
                urlc.setDoInput(true);
                urlc.setDoOutput(true);

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlc.getOutputStream(), "UTF-8"));
                boolean first=true;
                for (Map.Entry<String, String> entry : requestPostData.entrySet()) {
                    if (!first) writer.write('&');
                    else first=false;
                    writer.write(entry.getKey().toCharArray());
                    writer.write('=');
                    writer.write(entry.getValue().toCharArray());
                }
                writer.flush();
                writer.close();
            }

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