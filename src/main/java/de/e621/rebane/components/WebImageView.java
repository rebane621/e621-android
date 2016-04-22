package de.e621.rebane.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.e621.rebane.a621.R;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class WebImageView extends GifImageView {    //just to allow gifs, do not wrap this in a touchimageview, that wont work
    public static Map<String, Drawable> webImageCache = new HashMap<String, Drawable>();  // using this should only request a image ONCE per activity (or app call if we're lucky)
    static List<String> reqIids = new LinkedList<String>();

    public static void clear() { webImageCache.clear(); reqIids.clear(); }

    private Drawable mPlaceholder;
    private String userAgent;
    private boolean sUseCache;

    String url = null;

    public WebImageView(Context context) {
        this(context, null, 0);
    }

    public WebImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebImageView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);

        userAgent = context.getResources().getString(R.string.requestUserAgent);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.WebImageView,
                0, 0);

        try {
            sUseCache = a.getBoolean(R.styleable.WebImageView_useCache, true);
        } finally {
            a.recycle();
        }
    }

    public void setPlaceholderImage(Drawable drawable) {
        mPlaceholder = drawable;
        if (url == null) {
            setImageDrawable(mPlaceholder);
        }
    }

    public void setPlaceholderImage(int resid) {
        mPlaceholder = getResources().getDrawable(resid);
        if (url == null) {
            setImageDrawable(mPlaceholder);
        }
    }

    public void setImageUrl(String iid, String url, boolean gif) {
        if (!webImageCache.containsKey(iid)) {
            if (!reqIids.contains(iid)) {
                reqIids.add(iid);
                Logger.getLogger("a621").info("Downloading " + iid);
                DownloadTask task = new DownloadTask(gif);
                task.execute(iid, url, getContext().getCacheDir().getAbsolutePath());
            }
        } else {
            Logger.getLogger("a621").info("Using cache for " + iid);
            Drawable resource = webImageCache.get(iid);
            setImageDrawable(webImageCache.get(iid));
            if (resource instanceof GifDrawable) ((GifDrawable)resource).start();
        }
    }

    class DownloadTask extends AsyncTask<String, Void, Drawable> {
        String url;
        String iid;
        boolean gif;
        String cacheDir;

        public DownloadTask() {
            gif = false;
        }
        public DownloadTask(boolean gif) {
            this.gif = gif;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            iid = params[0];
            url = params[1];
            if (params.length>2) cacheDir=params[2];

            try {
                HttpURLConnection urlc = (HttpURLConnection) new URL(url).openConnection();
                urlc.setRequestMethod("GET");
                urlc.addRequestProperty("User-Agent", userAgent);
                BufferedInputStream bis = new BufferedInputStream(urlc.getInputStream());
                if (gif) {
                    int count;
                    OutputStream output = null;
                    //int lenghtOfFile = urlc.getContentLength();
                    boolean success=false;
                    File cache = File.createTempFile(iid, "gif", new File(cacheDir));//new File(cacheDir ,iid+".gif");
                    try {
                        Logger.getLogger("a621").info("Chaching gif to " + cache.getCanonicalPath());
                        //output = new FileOutputStream(cache.getAbsolutePath());
                        output = new FileOutputStream(cache);

                        byte data[] = new byte[1024];

                        while ((count = bis.read(data)) != -1) {
                            output.write(data, 0, count);
                        }
                        output.flush();
                        success=true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try { output.close(); } catch (Exception e) {}
                        try { bis.close(); } catch (Exception e) {}
                    }
                    if (!success) return null;
                    return new GifDrawable( cache );
                } else {
                    Bitmap staticImg = BitmapFactory.decodeStream(bis);
                    return (staticImg == null ? null : new BitmapDrawable(staticImg));
                }
            } catch (Exception exc) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                Logger.getLogger("a621").info("Storing " + iid + "...");
                if (sUseCache) WebImageView.webImageCache.put(iid, result);
                WebImageView.this.setImageDrawable(result);
                if (gif) {
                    GifDrawable rr = (GifDrawable) result;
                    rr.setLoopCount(0);
                    rr.start();
                }
                WebImageView.this.postInvalidate();

                if (reqIids.contains(iid)) reqIids.remove(iid);
            } else {
                Logger.getLogger("a621").info("Could not download " + iid);
            }
        }
    };

}