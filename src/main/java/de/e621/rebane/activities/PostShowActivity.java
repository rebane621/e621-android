package de.e621.rebane.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Html;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.e621.rebane.ActionRequest;
import de.e621.rebane.FilterManager;
import de.e621.rebane.HTMLformat;
import de.e621.rebane.LoginManager;
import de.e621.rebane.MiscStatics;
import de.e621.rebane.SQLite.SQLiteDB;
import de.e621.rebane.a621.R;
import de.e621.rebane.components.ColoredListAdapter;
import de.e621.rebane.components.CommentListAdapter;
import de.e621.rebane.components.DTextView;
import de.e621.rebane.components.TouchImageView;
import de.e621.rebane.components.WebImageView;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLTask;

public class PostShowActivity extends AppCompatActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_WRITE_STORAGE = 112;

    ImageView bnMore, bnComments;
    View postInfo, postComments, layMore;

    List<TextView> notes = null;

    TouchImageView imgImage;
    WebView swfImage;
//    WebImageView gifImage;
    VideoView webmImage;
    long VideoDuration;

    ListView lstTags, lstComments;
    public static final String EXTRAPOSTDATA = "PostShowXMLNodeExtraObject";
    public static final String EXTRAPOSTID = "PostShowByIDExtraObject";
    XMLNode data;
    CommentListAdapter results = null;
    String baseURL = null;
    String saveAs = null;
    LoginManager lm;
    FilterManager blacklist = null;
    boolean postLoadUserdata=false;

    String swfhtml = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"></head><body style='margin:0; pading:0; background-color: #152f56;'>" +
            "<object classid=\"clsid:d27cdb6e-ae6d-11cf-96b8-444553540000\" codebase=\"http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=7,0,0,0\" width=\"100%\" height=\"100%\" id=\"flashcontainer\" align=\"middle\">\n" +
            "<param name=\"allowScriptAccess\" value=\"sameDomain\">\n" +
            "<param name=\"movie\" value=\"@@@URL@@@\">\n" +
            "<param name=\"quality\" value=\"high\">\n" +
            "<param name=\"bgcolor\" value=\"#152f56\">\n" +
            "<embed src=\"@@@URL@@@\" quality=\"high\" bgcolor=\"#152f56\" width=\"600\" height=\"450\" name=\"flashcontainer\" align=\"middle\" allowscriptaccess=\"sameDomain\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.macromedia.com/go/getflashplayer\">\n" +
            "</object></body></html>";

    public View.OnClickListener NoteClickListener = new View.OnClickListener() {
        @Override public void onClick(View view) {
            Toast.makeText(PostShowActivity.this, ((TextView)view).getText().toString(), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_show);

        bnMore =    (ImageView)         findViewById(R.id.bnMore);
        bnComments = (ImageView)        findViewById(R.id.bnComments);
        layMore =                       findViewById(R.id.listMore);
        postInfo =                      findViewById(R.id.viewPostInfo);
        postComments =                  findViewById(R.id.viewPostComments);
        imgImage =  (TouchImageView)    findViewById(R.id.imageView);
        swfImage =  (WebView)           findViewById(R.id.swfView);
//        gifImage =  (WebImageView)      findViewById(R.id.gifView);
        webmImage = (VideoView)         findViewById(R.id.videoView);
        lstTags =   (ListView)          findViewById(R.id.lstTags);
        lstComments = (ListView)        findViewById(R.id.lstComments);
        bnMore.setOnClickListener(this);
        bnComments.setOnClickListener(this);
        (findViewById(R.id.bnRateUp)).setOnClickListener(this);
        (findViewById(R.id.bnRateDown)).setOnClickListener(this);
        (findViewById(R.id.bnFav)).setOnClickListener(this);

        imgImage.setOnTouchImageViewListener(new TouchImageView.OnTouchImageViewListener() {
            @Override public void onMove() {
                Logger.getLogger("a621").info("Current scale: " + imgImage.getCurrentZoom() + " " + (imgImage.getCurrentZoom() == imgImage.getMinZoom()) + " " + (imgImage.getCurrentZoom() == imgImage.getMaxZoom()));
                if (notes != null && !notes.isEmpty()) {
                    if (imgImage.getCurrentZoom() == 1.0)
                        for (TextView tv : notes)
                            tv.setVisibility(View.VISIBLE);
                    else
                        for (TextView tv : notes)
                            tv.setVisibility(View.GONE);
                }
            }
        });

        //load notes
        imgImage.setWebImageLoadedListener(new WebImageView.WebImageLoadedListener() {
            @Override public void onImageLoaded() {
                if (notes == null && imgImage.getVisibility()==View.VISIBLE) {
                    (new XMLTask(PostShowActivity.this) {
                        @Override
                        protected void onPostExecute(XMLNode result) {

                            if (result == null) {
                                quickToast("Could not get Notes");
                                return;
                            }
                            if (result.getChildCount() <= 0) { return; } // no notes found

                            notes = new LinkedList<TextView>();
                            TextView newnote; AbsoluteLayout.LayoutParams lapa;

                            AbsoluteLayout lay = (AbsoluteLayout) findViewById(R.id.layNotesOverlay);
                            Drawable b = imgImage.getDrawable();
                            int imgW = Integer.parseInt(data.getFirstChildText("width"));   //image size
                            int imgH = Integer.parseInt(data.getFirstChildText("height"));  //image size
                            //use centered values and calculations since the elements is centered
                            float cX = lay.getWidth()/2;  //screen middle
                            float cY = lay.getHeight()/2; //screen middle
                            double scale = (imgH/imgW > lay.getHeight()/lay.getWidth() ? lay.getHeight()/imgH : lay.getWidth()/imgW);
                            //scale *= 1.15;   //fix offset?
                            //image middle
                            float ciX = (imgW/2);
                            //image middle
                            float ciY = (imgH/2);

                            for (XMLNode note : result.getChildren()) {
                                if (!Boolean.parseBoolean(note.getFirstChildText("is_active"))) continue;

                                newnote = new TextView(PostShowActivity.this);

                                int left = (int)(cX + ( Integer.parseInt(note.getFirstChildText("x")) - ciX ) * scale);
                                int top = (int)(cY + ( Integer.parseInt(note.getFirstChildText("y")) - ciY ) * scale);
                                int width = (int)(Integer.parseInt(note.getFirstChildText("width")) * scale);
                                int height = (int)(Integer.parseInt(note.getFirstChildText("height")) * scale);

                                lapa = new AbsoluteLayout.LayoutParams(width, height, left, top);
                                newnote.setLayoutParams(lapa);
                                newnote.setBackgroundColor(getResources().getColor(R.color.noteBackground));
                                newnote.setText(note.getFirstChildText("body"));

                                newnote.setOnClickListener(PostShowActivity.this.NoteClickListener);

                                lay.addView(newnote);
                                notes.add(newnote);
                            }
                        }
                    }).execute( baseURL +"note/index.xml?post_id=" + data.getFirstChildText("id") );
                }
            }
        });

        //swfImage.setDrawingCacheBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        swfImage.setLayerType(View.LAYER_TYPE_HARDWARE, null);  //for flash
        swfImage.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        WebSettings ws = swfImage.getSettings();

        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        ws.setJavaScriptEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setPluginState(WebSettings.PluginState.ON);

        ws.setBuiltInZoomControls(true);
        ws.setSupportZoom(true);
        ws.setDisplayZoomControls(false);

        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        //ws.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.36 (KHTML, like Gecko) Chrome/13.0.766.0 Safari/534.36");
        ws.setUserAgentString(getResources().getString(R.string.requestUserAgent));
        try{
            Class cl=Class.forName("android.webkit.WebSettings");
            Method mthd=cl.getMethod("setFlashPlayerEnabled",Boolean.TYPE);
            mthd.invoke(swfImage.getSettings(),true);
        }catch(Exception e){ e.printStackTrace(); }

        webmImage.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                //VideoDuration = webmImage.getDuration();
                webmImage.requestFocus();
                webmImage.start();
                mp.setLooping(true);
            }
        });
        webmImage.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                return false;
            }
        });

       // webmImage.setDrawingCacheBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        //intent post data loader
        if (baseURL == null) baseURL = DrawerWrapper.baseURL;
        data = (XMLNode)getIntent().getSerializableExtra(EXTRAPOSTDATA);
        if (data == null) {
            final int postid = getIntent().getIntExtra(EXTRAPOSTID, 0);
            if (postid == 0) {
                Toast.makeText(this, "The PostView did not receive any data", Toast.LENGTH_LONG).show();
                finish();
            } else {
                new XMLTask(this) {
                    @Override protected void onPostExecute(XMLNode result) {
                        if (result == null || !result.getType().equals("post")) {
                            quickToast("Could not load Post " + postid);
                        } else {
                            data = result;
                            data.setAttribute("Blacklisted", String.valueOf(blacklist.isBlacklisted(data)));
                            preLoadPost();
                        }
                    }
                }.execute(baseURL + "post/show.xml?id=" + postid);
            }
        }

        SQLiteDB database = new SQLiteDB(this);
        database.open();
        /*even if saveAs == null*/
        saveAs = database.getValue(SettingsActivity.SETTINGDEFAULTSAVE);
        blacklist = new FilterManager(this, database.getStringArray(SettingsActivity.SETTINGBLACKLIST));
        postLoadUserdata = Boolean.parseBoolean(database.getValue(SettingsActivity.SETTINGFANCYCOMMENTS));
        lm = new LoginManager(this, database);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        preLoadPost();
    }


    void preLoadPost() {
        if (data==null) {
            return; //probably given a postID instead
        } else {
            if (Boolean.valueOf(data.getAttribute("Blacklisted"))) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                postLoadPost();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                PostShowActivity.this.finish(); //user does not want to show blacklisted Post
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                TextView fMsg = new TextView(this);
                fMsg.setText(Html.fromHtml("<big>Blacklist</big><br>This post contains blacklisted tags!<br><small>Post Tags: " + data.getFirstChildText("tags") + "</small><br><br>Do you want to continue?"));
                builder.setView(fMsg)
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .show();
            } else {
                postLoadPost();
            }
        }
    }

    void postLoadPost() {
        boolean deleted;
        String type = data.getFirstChildText("status");
        if (deleted = type.equalsIgnoreCase("deleted")) { //active, flagged, pending, deleted
            imgImage.setVisibility(View.VISIBLE);
            imgImage.setPlaceholderImage(getResources().getDrawable(R.mipmap.thumb_deleted));
        } else {
            double frp = MiscStatics.freeRamPerc();
            long frb = MiscStatics.freeRamB();
            String quality = "file_url";
            int fsz = Integer.valueOf(data.getFirstChildText("file_size"));
            //if (frp < 50)
            MiscStatics.clearMem(this, fsz / (1024 * 1024), 50.0);
            //image requires more than 80% of the total available ram
            if (frb * 0.8 < fsz) WebImageView.clear();   //purge everything, we need it
            //image would require more than 90% of the total available ram
            if (frb-(8*1024*1024)<fsz) {    //try to leave 8 MiB for the rest of the app
                quality = "sample_url"; //reduce quality if low on ram
                quickToast("Had to load lower res");
            }

            type = data.getFirstChildText("file_ext");
            if (type.equalsIgnoreCase("swf")) {
                swfImage.setVisibility(View.VISIBLE);
                String innerHTML = swfhtml.replaceAll("@@@URL@@@", data.getFirstChildText("file_url"));
                Logger.getLogger("a621").info("Inner HTML: " + innerHTML);
                swfImage.loadData(innerHTML, "text/html", "utf-8");
                //swfImage.loadUrl(data.getFirstChildText("file_url"));
            } else if (type.equalsIgnoreCase("webm")) {
                try {
                    (findViewById(R.id.videoContainer)).setVisibility(View.VISIBLE);
                    //new VideoCacher(data.getFirstChildText("md5"), data.getFirstChildText("file_url")){
                    //    @Override protected void onPostExecute(File file) {
                    //        webmImage.setVideoPath(file.getAbsolutePath());
                    //        webmImage.start();
                    //    }
                    //}.execute();
                    webmImage.setVideoURI(Uri.parse(data.getFirstChildText("file_url")));
                    //webmImage.start();
                    MediaController mct = new MediaController(PostShowActivity.this);
                    webmImage.setMediaController(mct);
                } catch (Exception e) {
                    Toast.makeText(this, "Could not load webm\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                imgImage.setVisibility(View.VISIBLE);
                imgImage.setPlaceholderImage(getResources().getDrawable(R.mipmap.thumb_loading));
                imgImage.setImageUrl(data.getFirstChildText("md5"), data.getFirstChildText(quality), true);
            }
        }
        lstTags.setAdapter(new ArrayAdapter<String>(this, R.layout.singleline_listentry, data.getFirstChildText("tags").split(" ")));
        lstTags.invalidate();
        lstTags.requestLayout();
        lstTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClipboardManager cbm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                String text = ((TextView)view).getText().toString();
                cbm.setPrimaryClip(ClipData.newPlainText("text", text));
                Toast.makeText(PostShowActivity.this, "Copied " + text + " to the clipboard", Toast.LENGTH_SHORT).show();
            }
        });


        //try {
        TextView stats = (TextView) findViewById(R.id.txtStats1);
        String rating = data.getFirstChildText("rating").toUpperCase();
        int score = Integer.valueOf(data.getFirstChildText("score"));
        String url = data.getFirstChildText("source");
        String statstext = "Source: " + (deleted || url==null || url.isEmpty() ? " - " : HTMLformat.link(url, url)) +
                "<br>Uploaded " + HTMLformat.colored(MiscStatics.readableTime(data.getFirstChildText("created_at")), Color.WHITE) + " by " + HTMLformat.colored(data.getFirstChildText("author"), Color.WHITE) +
                "<br>Rating: " + HTMLformat.bold( HTMLformat.colored(rating, getResources().getColor("E".equals(rating) ? R.color.preview_red : ( "Q".equals(rating) ? R.color.preview_yellow : R.color.preview_green) )) ) +
                "<br>Score: " + HTMLformat.colored(String.valueOf(score), score>0 ? getResources().getColor(R.color.preview_green) : ( score<0 ? getResources().getColor(R.color.preview_red) :Color.WHITE)) +
                "<br>Favourites: " + HTMLformat.colored(data.getFirstChildText("fav_count"), Color.WHITE) +
                "<br>ID: " + HTMLformat.colored(data.getFirstChildText("id"), Color.WHITE) +
                "<br>Size: " + (deleted ? "??? (? kiB)" : HTMLformat.colored(data.getFirstChildText("width") + "x" + data.getFirstChildText("height") + " (" + Float.valueOf(data.getFirstChildText("file_size")) / 1024 + "kiB)", Color.WHITE) );
        stats.setText(Html.fromHtml(statstext));
        stats.requestLayout();
        //} catch(Exception e) { }    //just in case ;)

        DTextView desc = (DTextView) findViewById(R.id.txtDescription);
        desc.setDText(data.getFirstChildText("description"));

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Post #" + data.getFirstChildText("id"));
        ab.setSubtitle("▲ " + data.getFirstChildText("score") + " ♥ " + data.getFirstChildText("fav_count") + " " + data.getFirstChildText("rating").toUpperCase());

        //comment post loader
        (new XMLTask(this) {
            @Override
            protected void onPostExecute(XMLNode result) {

                if (result == null) {
                    quickToast("Could not get Comments");
                    return;
                }
                //if (result.getChildCount() <= 0) { quickToast("No results found..."); return; }

                List<XMLNode> revers = new LinkedList<XMLNode>();
                for (XMLNode n : result.children()) revers.add(0, n);

                results = new CommentListAdapter(getApplicationContext(), R.id.txtComment, revers, baseURL, blacklist, postLoadUserdata);
                results.svNumPosts = result.getChildCount();

                lstComments.setAdapter(results);
                lstComments.refreshDrawableState();

                if (PostShowActivity.this.postComments.getVisibility() == View.VISIBLE) {
                    ActionBar ab = PostShowActivity.this.getSupportActionBar();
                    if (results == null)
                        ab.setSubtitle("0 Comments");
                    else
                        ab.setSubtitle(results.getResultCount() + " Comments");
                }
            }
        }).execute( baseURL +"comment/index.xml?post_id=" + data.getFirstChildText("id") );

        //tag type loader
        (new XMLTask(this) {
            @Override
            protected void onPostExecute(XMLNode result) {

                if (result == null) {
                    quickToast("Could not get tag types");
                    return;
                }
                //if (result.getChildCount() <= 0) { quickToast("No results found..."); return; }

                List<XMLNode> sorted = new LinkedList<XMLNode>();
                sorted.addAll(result.children());
                Collections.sort(sorted, new Comparator<XMLNode>() {
                    @Override public int compare(XMLNode t0, XMLNode t1) {
                        int comp0 = t0.getFirstChildText("type").compareTo(t1.getFirstChildText("type")) * -1; //reverse sort those to get general tags to the bottom
                        return ( comp0 != 0 ? comp0 : t0.getFirstChildText("name").compareTo(t1.getFirstChildText("name")) );
                    }
                });

                int[] colors = new int[] {
                    getResources().getColor(R.color.tagGe),
                    getResources().getColor(R.color.tagAr),
                        0,          //unused value
                    getResources().getColor(R.color.tagCo),
                    getResources().getColor(R.color.tagCh),
                    getResources().getColor(R.color.tagSp)
                };

                ColoredListAdapter cla = new ColoredListAdapter(PostShowActivity.this, R.id.singleline_listentry);
                for (XMLNode n : sorted) {
                    cla.add(n.getFirstChildText("name"), colors[Integer.parseInt(n.getFirstChildText("type"))]);
                }

                lstTags.setAdapter(cla);
            }
        }).execute( baseURL +"tag/index.xml?post_id=" + data.getFirstChildText("id") );

        //favorite check postload
        if (lm.isLoggedIn()) {
            (new XMLTask(this) {
                @Override protected void onPostExecute(XMLNode result) {
                    if (result != null && result.getType().equals("posts")) {
                        int faved = result.getChildCount();
                        data.setAttribute("faved", String.valueOf(faved)); //getChildCount is more reliable than posts>count
                        if (faved>0)
                            ((ImageView) findViewById(R.id.bnFav)).setImageDrawable(getResources().getDrawable(R.mipmap.ic_img_fav_on));
                    }
                }
            }).execute(baseURL + "post/index.xml?tags=" + URLEncoder.encode("id:" + data.getFirstChildText("id") + " fav:" + lm.getUsername()) );
        } else {
            //Logger.getLogger("a621").info("Not logged in");
        }

        //TODO get rid of the fav check and a score up/down and hope it'll appear in the /post/show
    }

    @Override
    public void onClick(View view) {
        Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        ActionBar ab = getSupportActionBar();
        Map<String, String> postData;
        switch(view.getId()) {
            case (R.id.bnMore):
                layMore.startAnimation(bottomDown);
                layMore.setVisibility(View.GONE);

                postInfo.startAnimation(bottomUp);
                postInfo.setVisibility(View.VISIBLE);

                ab.setTitle("Post Data");
                ab.setSubtitle("Post #" + data.getFirstChildText("id") + ", " + data.getFirstChildText("status"));

                break;
            case (R.id.bnComments):
                layMore.startAnimation(bottomDown);
                layMore.setVisibility(View.GONE);

                postComments.startAnimation(bottomUp);
                postComments.setVisibility(View.VISIBLE);

                ab.setTitle("Post Comments");
                if (results == null)
                    ab.setSubtitle("Loading Comments...");
                else
                    ab.setSubtitle(results.getResultCount() + " Comments");
                break;
            case (R.id.bnFav):
                postData = new HashMap<String, String>();
                postData.put("id", data.getFirstChildText("id"));
                final int action = ((data.attributes().contains("faved") && !data.getAttribute("faved").equals("0")) ? ActionRequest.POST_UNFAVOURITE : ActionRequest.POST_FAVOURITE);
                Logger.getLogger("a621").info(action == ActionRequest.POST_FAVOURITE ? "Adding post to favorites" : "Removing psot from favorites");
                new ActionRequest(this, lm.getLogin(), action, postData) {
                    @Override public void onSuccess(XMLNode result) {
                        Logger.getLogger("a621").info(result.toString());
                        if (action == ActionRequest.POST_FAVOURITE) {
                            ((ImageView) findViewById(R.id.bnFav)).setImageDrawable(getResources().getDrawable(R.mipmap.ic_img_fav_on));
                            PostShowActivity.this.data.setAttribute("faved", "1");
                        } else {
                            ((ImageView) findViewById(R.id.bnFav)).setImageDrawable(getResources().getDrawable(R.mipmap.ic_img_fav_off));
                            PostShowActivity.this.data.setAttribute("faved", "0");
                        }
                    }
                }.on(baseURL);
                break;
            case (R.id.bnRateUp):
                postData = new HashMap<String, String>();
                postData.put("id", data.getFirstChildText("id"));
                postData.put("score", "1");
                new ActionRequest(this, lm.getLogin(), ActionRequest.POST_VOTE_UP, postData) {
                    @Override public void onSuccess(XMLNode result) {
                        Integer c;
                        try { c = Integer.parseInt(result.getFirstChildText("change")); }
                        catch (Exception e) { c = 0; }
                        if (c>0) {
                            ((ImageView) findViewById(R.id.bnRateUp)).setImageDrawable(getResources().getDrawable(R.mipmap.ic_img_rate_up_on));
                            ((ImageView) findViewById(R.id.bnRateDown)).setImageDrawable(getResources().getDrawable(R.mipmap.ic_img_rate_down_off));
                        } else {
                            ((ImageView) findViewById(R.id.bnRateUp)).setImageDrawable(getResources().getDrawable(R.mipmap.ic_img_rate_up_off));
                            ((ImageView) findViewById(R.id.bnRateDown)).setImageDrawable(getResources().getDrawable(R.mipmap.ic_img_rate_down_off));
                        }
                    }
                }.on(baseURL);
                break;
            case (R.id.bnRateDown):
                postData = new HashMap<String, String>();
                postData.put("id", data.getFirstChildText("id"));
                postData.put("score", "-1");
                new ActionRequest(this, lm.getLogin(), ActionRequest.POST_VOTE_DOWN, postData) {
                    @Override public void onSuccess(XMLNode result) {
                        Integer c;
                        try { c = Integer.parseInt(result.getFirstChildText("change")); }
                        catch (Exception e) { c = 0; }
                        if (c<0) {
                            ((ImageView) findViewById(R.id.bnRateDown)).setImageDrawable(getResources().getDrawable(R.mipmap.ic_img_rate_down_on));
                            ((ImageView) findViewById(R.id.bnRateUp)).setImageDrawable(getResources().getDrawable(R.mipmap.ic_img_rate_up_off));
                        } else {
                            ((ImageView) findViewById(R.id.bnRateUp)).setImageDrawable(getResources().getDrawable(R.mipmap.ic_img_rate_up_off));
                            ((ImageView) findViewById(R.id.bnRateDown)).setImageDrawable(getResources().getDrawable(R.mipmap.ic_img_rate_down_off));
                        }
                    }
                }.on(baseURL);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (layMore.getVisibility() != View.VISIBLE) {
            if (postInfo.getVisibility() == View.VISIBLE) {
                Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
                postInfo.startAnimation(bottomDown);
                postInfo.setVisibility(View.GONE);
            }
            if ((postComments.getVisibility() == View.VISIBLE)) {
                Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
                postComments.startAnimation(bottomDown);
                postComments.setVisibility(View.GONE);
            }

            Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
            layMore.startAnimation(bottomUp);
            layMore.setVisibility(View.VISIBLE);

            ActionBar ab = getSupportActionBar();
            ab.setTitle("Post #" + data.getFirstChildText("id"));
            ab.setSubtitle("▲ " + data.getFirstChildText("score") + " ♥ " + data.getFirstChildText("fav_count") + " " + data.getFirstChildText("rating").toUpperCase());
        } else
            super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (findViewById(R.id.videoContainer).getVisibility()== View.VISIBLE) {
            savedInstanceState.putInt("Position", webmImage.getCurrentPosition());
            webmImage.pause();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Integer position = savedInstanceState.getInt("Position");
        if (position != null) webmImage.seekTo(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.postshow, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_saveas) {
            if (imgImage.getVisibility()!=View.VISIBLE || imgImage.getCachedPath() == null) {
                quickToast("Can't save image");
                return true;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    quickToast("We can't write to SDCard unless you grant Permission!");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            } else SaveImageAs();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SaveImageAs() {
        Intent fchooser = new Intent(this, FolderChooser.class);
        if (saveAs != null) fchooser.putExtra(FolderChooser.FOLDERINTENTEXTRA, saveAs);
        startActivityForResult(fchooser, 1); //response code 1 for saveAs result
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SaveImageAs();
                } else {
                    quickToast("Saving to SDCard was denied by YOU D:");
                }
                return;
            }
        }
    }

    public void quickToast(CharSequence message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private class PostDownloader extends AsyncTask<Void, Void, File> {
        String url, userAgent;
        File target;
        Context context;
        public PostDownloader(File target, String url) {
            this.url = url;
            this.target = target;
            userAgent = (context = PostShowActivity.this.getApplicationContext()).getResources().getString(R.string.requestUserAgent);
        }
        @Override protected File doInBackground(Void... voids) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) new URL(url).openConnection();
                urlc.setRequestMethod("GET");
                urlc.addRequestProperty("User-Agent", userAgent);
                BufferedInputStream bis = new BufferedInputStream(urlc.getInputStream());
                Logger.getLogger("a621").info("Connected with response code " + urlc.getResponseCode() + ": " + urlc.getResponseMessage());

                int count;
                BufferedOutputStream output = null;
                boolean success=false;
                if (target.exists()) target.delete(); // re downloading it...
                while (target.exists());    //wait for file to be deleted?
                try {
                    Logger.getLogger("a621").info("Downloading post " + url + " to " + target.getAbsolutePath());
                    output = new BufferedOutputStream(new FileOutputStream(target));

                    byte data[] = new byte[1024];

                    while ((count = bis.read(data)) != -1) {
                        output.write(data, 0, count);
                    }

                    success=true;
                } catch (Exception e) {
                    Logger.getLogger("a621").log(Level.INFO, "ERROR ", e);
                    e.printStackTrace();
                }
                try { output.flush(); } catch (Exception e) { e.printStackTrace(); }
                try { output.close(); } catch (Exception e) { e.printStackTrace(); }
                try { bis.close(); } catch (Exception e) { e.printStackTrace(); }

                if (!success) return null;
                return target;
            } catch (Exception exc) {
                Logger.getLogger("a621").log(Level.INFO, "ERROR ", exc);
                exc.printStackTrace();
                return null;
            }
        }

        @Override protected void onPostExecute(File file) {
            Toast.makeText(context, (file == null || !file.exists() || !file.isFile() ? "Download failed!" : "Post downloaded"), Toast.LENGTH_LONG).show();
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) { //saveAs result
            if(resultCode == Activity.RESULT_OK) {
                String saveLoc = data.getStringExtra(FolderChooser.FOLDERINTENTEXTRA);
                if (saveLoc == null) {
                    quickToast("Invalid Directory!");
                }
                String imgPath = PostShowActivity.this.data.getFirstChildText("file_url");
                try {
                    new PostDownloader(new File(saveLoc, imgPath.substring(imgPath.lastIndexOf('/'))), imgPath).execute();
                } catch (Exception e) {
                    quickToast("Can't save file:\n"+e.getMessage());
                    e.printStackTrace();
                    return;
                }
                Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //nothing changes
            }
        }
    }
}
