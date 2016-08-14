package de.e621.rebane.components;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

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

import de.e621.rebane.MiscStatics;
import de.e621.rebane.a621.R;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class WebImageView extends GifImageView {    //just to allow gifs, do not wrap this in a touchimageview, that wont work
    public static Map<String, Drawable> webImageCache = new HashMap<String, Drawable>();  // using this should only request a image ONCE per activity (or app call if we're lucky)
    public static Map<String, Long> webImageUsage = new HashMap<String, Long>();  //to help free ram
    static List<String> reqIids = new LinkedList<String>();

    ArrayAdapter<?> adapter = null;
    public static void clear() { webImageCache.clear(); reqIids.clear(); }

    private Drawable mPlaceholder;
    private String userAgent;
    private boolean sUseCache;

    String url = null;
    String cachedPath=null;
    public String getCachedPath() { return cachedPath; }

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

    public interface WebImageLoadedListener {
        void onImageLoaded();
    }
    WebImageLoadedListener wil = null;
    public void setWebImageLoadedListener(WebImageLoadedListener listener) { wil = listener; }

    public void setAdapter(ArrayAdapter<?> ad) {
        adapter=ad;
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

    private void uUsage(String iid) {
        if (webImageCache.containsKey(iid)) {
            webImageUsage.put(iid, System.currentTimeMillis()/1000);
        }
    }
    private static boolean cleanupisrunning=false; private static int cleanmaxage=60; //by default clear only images older than 60 seconds
    private static long lastcleantime = 0;
    public static void cleanUp(Context context) {
        cleanUp(context, 10, 25); // only clear if less than 10 MiB or 25% are available
    }
    public static void cleanUp(Context context, int WantFreeMiB, double WantFreePercent) {
        if (cleanupisrunning) return; cleanupisrunning=true;
        //chkmem
        long now = System.currentTimeMillis()/1000;
        List<String> marked;
        long freeRam = MiscStatics.freeRamB()/(1024*1024); double freePerc = MiscStatics.freeRamPerc();
        Logger.getLogger("a621").info("Cleaning (" + cleanmaxage + ")- free ram: " + freeRam + " MiB (" + freePerc + " %)");
        if (freeRam < WantFreeMiB || freePerc < WantFreePercent) {
            int rem = 0;
            int override = 0;
            do {
                marked = new LinkedList<String>();
                for (Map.Entry<String, Long> e : webImageUsage.entrySet())
                    if (now - e.getValue() > cleanmaxage - override)
                        marked.add(e.getKey());
                for (String iid : marked) {
                    webImageUsage.remove(iid);
                    webImageCache.remove(iid);
                }
                rem += marked.size();
                marked.clear();
                marked = null;
                override += 5;
            } while (rem < 5 && !webImageCache.isEmpty()); //we're supposed to clean up stuff, so let's at least free 5 images
            //if (cleanmaxage > 5) cleanmaxage-=5; //in case 60 secs was too big of a time span, recude by 5 seconds and retry
            //set max post age to freemem * 60 sec
            cleanmaxage = (int)(60*freePerc/100);
            if (cleanmaxage < 5) { cleanmaxage=5; }
            //Runtime.getRuntime().gc(); don't call it manually... the system should do that
            if (freeRam < 8) Toast.makeText(context, "Image Cache:\nFree RAM < 8 MiB!", Toast.LENGTH_SHORT).show();
        } else {
            if (now-lastcleantime > cleanmaxage) {    //grant some more space every CLEANINGMAXAGE seconds, if the ram was ok, so if you're browsing slow, it's cleaning even slower
                if (cleanmaxage <= 55) cleanmaxage += 5;
                lastcleantime = now;
            }
        }
        cleanupisrunning=false;
    }

    public void setImageUrl(String iid, String url, boolean cache) {
        if (!webImageCache.containsKey(iid)) {
            if (!reqIids.contains(iid)) {
                //if (!MiscStatics.canRequest(getContext())) return;    //may we ignore this here? I mean opening a index page on the browser laods about 70 thumbnails at once
                reqIids.add(iid);
                Logger.getLogger("a621").info("Downloading " + iid);
                DownloadTask task = new DownloadTask();
                task.execute(iid, url, (cache ? getContext().getCacheDir().getAbsolutePath() : null)); //have at least 16 MiB RAM for the app to cache
                cleanUp(getContext());
            }
        } else {
            Logger.getLogger("a621").info("Using cache for " + iid);
            uUsage(iid);    //update timestamp for this image
            Drawable resource = webImageCache.get(iid);
            setImageDrawable(webImageCache.get(iid));
            updateDisplay();
            if (resource instanceof GifDrawable) ((GifDrawable)resource).start();
        }
    }

    //execute args: image id (for result) , url , cache path (omit or null to don't cache)
    class DownloadTask extends AsyncTask<String, Void, Drawable> {
        String url;
        String iid;
        boolean gif = false;
        String cacheDir = null;

        @Override
        protected Drawable doInBackground(String... params) {
            iid = params[0];
            url = params[1];
            if (params.length>2) cacheDir=params[2];
            String fext = url.substring(url.lastIndexOf('.'));
            gif = fext.equals(".gif");
            try {
                HttpURLConnection urlc = (HttpURLConnection) new URL(url).openConnection();
                urlc.setRequestMethod("GET");
                urlc.addRequestProperty("User-Agent", userAgent);
                BufferedInputStream bis = new BufferedInputStream(urlc.getInputStream());
                if (cacheDir != null) {
                    int count;
                    //int maxp=urlc.getContentLength(), p=0;
                    OutputStream output = null;
                    //int lenghtOfFile = urlc.getContentLength();
                    boolean success=false;
                    File cache = new File (cacheDir, iid+fext); //File.createTempFile(iid, fext, new File(cacheDir));//new File(cacheDir ,iid+".gif");
                    if (cache.exists()) cache.delete(); // re downloading it...
                    try {
                        Logger.getLogger("a621").info("Chaching image to " + cache.getAbsolutePath());
                        //output = new FileOutputStream(cache.getAbsolutePath());
                        output = new FileOutputStream(cache);

                        byte data[] = new byte[1024];

                        while ((count = bis.read(data)) != -1) {
                            //p+=count; Logger.getLogger("a621").info("Download: " + p + "/" + maxp);
                            output.write(data, 0, count);
                        }
                        output.flush();
                        cachedPath = cache.getAbsolutePath();
                        success=true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try { output.close(); } catch (Exception e) {}
                        try { bis.close(); } catch (Exception e) {}
                    }
                    if (!success) return null;
                    Logger.getLogger("a621").info("Image is gif? " + gif + ", " + fext);
                    return (gif ? new GifDrawable( cache ) : new BitmapDrawable( cache.getAbsolutePath() ) );
                } else {
                    Bitmap staticImg = BitmapFactory.decodeStream(bis);
                    return (staticImg == null ? null : new BitmapDrawable(staticImg));
                }
            } catch (Exception exc) {
                exc.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                if (sUseCache) {
                    Logger.getLogger("a621").info("Storing " + iid + "...");
                    WebImageView.webImageCache.put(iid, result);
                    WebImageView.webImageUsage.put(iid, System.currentTimeMillis()/1000);
                }
                WebImageView.this.setImageDrawable(result);
                if (gif && cacheDir != null) {  //if cachedir is null we can't have a gif at hand, see code above
                    GifDrawable rr = (GifDrawable) result;
                    rr.setLoopCount(0);
                    rr.start();
                }
                //WebImageView.this.postInvalidate();
                updateDisplay();

                if (reqIids.contains(iid)) reqIids.remove(iid);

                if (wil != null) wil.onImageLoaded();
            } else {
                Logger.getLogger("a621").info("Could not download " + iid);
            }
        }
    }

    void updateDisplay() {
        View view = WebImageView.this;
        View root = view.getRootView();
        view.postInvalidate();
        root.postInvalidate();
        if (adapter != null) adapter.notifyDataSetChanged();    //only thing that truly worked here \(°v°)/ <3
    }
}