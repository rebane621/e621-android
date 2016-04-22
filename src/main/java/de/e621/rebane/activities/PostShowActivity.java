package de.e621.rebane.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.e621.rebane.HTMLformat;
import de.e621.rebane.MiscStatics;
import de.e621.rebane.a621.R;
import de.e621.rebane.components.CommentListAdapter;
import de.e621.rebane.components.DTextView;
import de.e621.rebane.components.TouchImageView;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLTask;

public class PostShowActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView bnMore, bnComments;
    View postInfo, postComments, layMore;

    TouchImageView imgImage;
    WebView swfImage;
//    WebImageView gifImage;
    VideoView webmImage;

    ListView lstTags, lstComments;
    public static final String EXTRAPOSTDATA = "PostShowXMLNodeExtraObject";
    XMLNode data;
    CommentListAdapter results = null;
    String baseURL = null;

    String swfhtml = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"></head><body style='margin:0; pading:0; background-color: #152f56;'>" +
            "<object classid=\"clsid:d27cdb6e-ae6d-11cf-96b8-444553540000\" codebase=\"http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=7,0,0,0\" width=\"100%\" height=\"100%\" id=\"flashcontainer\" align=\"middle\">\n" +
            "<param name=\"allowScriptAccess\" value=\"sameDomain\">\n" +
            "<param name=\"movie\" value=\"@@@URL@@@\">\n" +
            "<param name=\"quality\" value=\"high\">\n" +
            "<param name=\"bgcolor\" value=\"#152f56\">\n" +
            "<embed src=\"@@@URL@@@\" quality=\"high\" bgcolor=\"#152f56\" width=\"600\" height=\"450\" name=\"flashcontainer\" align=\"middle\" allowscriptaccess=\"sameDomain\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.macromedia.com/go/getflashplayer\">\n" +
            "</object></body></html>";

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
        swfImage =  (WebView)   findViewById(R.id.swfView);
//        gifImage =  (WebImageView)      findViewById(R.id.gifView);
        webmImage = (VideoView)         findViewById(R.id.videoView);
        lstTags =   (ListView)          findViewById(R.id.lstTags);
        lstComments = (ListView)        findViewById(R.id.lstComments);
        bnMore.setOnClickListener(this);
        bnComments.setOnClickListener(this);

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
        ws.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/534.36 (KHTML, like Gecko) Chrome/13.0.766.0 Safari/534.36");
        //ws.setUserAgentString(getResources().getString(R.string.requestUserAgent));
        try{
            Class cl=Class.forName("android.webkit.WebSettings");
            Method mthd=cl.getMethod("setFlashPlayerEnabled",Boolean.TYPE);
            mthd.invoke(swfImage.getSettings(),true);
        }catch(Exception e){ e.printStackTrace(); }

        webmImage.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            }
        });

       // webmImage.setDrawingCacheBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        data = (XMLNode)getIntent().getSerializableExtra(EXTRAPOSTDATA);
        if (baseURL == null) baseURL = DrawerWrapper.baseURL;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (data==null) {
            Toast.makeText(this, "The PostView did not receive any data", Toast.LENGTH_LONG).show();
            finish();
        } else {
            boolean deleted;
            String type = data.getFirstChildText("status");
            if (deleted = type.equalsIgnoreCase("deleted")) { //active, flagged, pending, deleted
                imgImage.setVisibility(View.VISIBLE);
                imgImage.setPlaceholderImage(getResources().getDrawable(R.mipmap.thumb_deleted));
            } else {
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
                        webmImage.setVideoPath(data.getFirstChildText("file_url"));
                        webmImage.setMediaController(new MediaController(this));
                        webmImage.start();
                    } catch (Exception e) {
                        Toast.makeText(this, "Could not load webm\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    imgImage.setVisibility(View.VISIBLE);
                    imgImage.setPlaceholderImage(getResources().getDrawable(R.mipmap.thumb_loading));
                    imgImage.setImageUrl(data.getFirstChildText("md5"), data.getFirstChildText("file_url"), type.equalsIgnoreCase("gif"));
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

                    results = new CommentListAdapter(getApplicationContext(), R.id.txtComment, revers);
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
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bnMore) {
            Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
            layMore.startAnimation(bottomDown);
            layMore.setVisibility(View.GONE);

            Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
            postInfo.startAnimation(bottomUp);
            postInfo.setVisibility(View.VISIBLE);

            ActionBar ab = getSupportActionBar();
            ab.setTitle("Post Data");
            ab.setSubtitle("Post #" + data.getFirstChildText("id") + ", " + data.getFirstChildText("status"));
        } else if (view.getId() == R.id.bnComments) {
            Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
            layMore.startAnimation(bottomDown);
            layMore.setVisibility(View.GONE);

            Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
            postComments.startAnimation(bottomUp);
            postComments.setVisibility(View.VISIBLE);

            ActionBar ab = getSupportActionBar();
            ab.setTitle("Post Comments");
            if (results == null)
                ab.setSubtitle("Loading Comments...");
            else
                ab.setSubtitle(results.getResultCount() + " Comments");
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
    public void quickToast(CharSequence message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
