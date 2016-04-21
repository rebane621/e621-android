package de.e621.rebane.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.e621.rebane.a621.R;

public class WebImageView extends ImageView {
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

    public void setImageUrl(String iid, String url) {
        if (!webImageCache.containsKey(iid)) {
            if (!reqIids.contains(iid)) {
                reqIids.add(iid);
                Logger.getLogger("a621").info("Downloading " + iid);
                DownloadTask task = new DownloadTask();
                task.execute(iid, url);
            }
        } else {
            Logger.getLogger("a621").info("Using cache for " + iid);
            setImageDrawable(webImageCache.get(iid));
        }
    }

    class DownloadTask extends AsyncTask<String, Void, Bitmap> {
        String url;
        String iid;

        @Override
        protected Bitmap doInBackground(String... params) {
            iid = params[0];
            url = params[1];
            try {
                HttpURLConnection urlc = (HttpURLConnection) new URL(url).openConnection();
                urlc.setRequestMethod("GET");
                urlc.addRequestProperty("User-Agent", userAgent);
                BufferedInputStream bis = new BufferedInputStream((new URL(url)).openConnection().getInputStream());
                return BitmapFactory.decodeStream(bis);
            } catch (Exception exc) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            Drawable mImage = new BitmapDrawable(result);
            if (mImage != null) {
                Logger.getLogger("a621").info("Storing " + iid + "...");
                if (sUseCache) WebImageView.webImageCache.put(iid, mImage);
                WebImageView.this.setImageDrawable(mImage);
                WebImageView.this.invalidate();
                WebImageView.this.requestLayout();
                WebImageView.this.refreshDrawableState();
                if (reqIids.contains(iid)) reqIids.remove(iid);
            } else {
                Logger.getLogger("a621").info("Could not download " + iid);
            }
        }
    };

}