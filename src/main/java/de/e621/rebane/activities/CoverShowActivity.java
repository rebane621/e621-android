package de.e621.rebane.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.itwookie.XMLreader.XMLNode;
import com.itwookie.XMLreader.XMLTask;

import java.util.List;
import java.util.logging.Logger;

import de.e621.rebane.FilterManager;
import de.e621.rebane.HTMLformat;
import de.e621.rebane.MiscStatics;
import de.e621.rebane.SQLite.SQLiteDB;
import de.e621.rebane.a621.R;
import de.e621.rebane.components.WebImageView;

public class CoverShowActivity extends DrawerWrapper implements View.OnClickListener {

    TextView name, meta, desc, seekPos;
    Button read;
    WebImageView preview;
    SeekBar seeker;
    public XMLNode entryPoint = null;
    public int coverID = -1, max=0;
    String type;

    public static final String EXTRADISPLAYDATA = "CoverDataForDisplay";
    public static final String EXTRADISPLAYTYPE = "CoverDisplayType";

    @Override
    @SuppressLint("MissingSuperCall")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.content_cover_show, savedInstanceState);
        //setContentView(R.layout.activity_login);
        onCreateDrawer(this.getClass());   //DrawerWrapper function that requires the layout to be set to prepare the drawer

        type = getIntent().getStringExtra(EXTRADISPLAYTYPE);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(type.substring(0,1).toUpperCase() + type.substring(1).toLowerCase() + " Cover");

        name =      (TextView) findViewById(R.id.lblTitle);
        meta =      (TextView) findViewById(R.id.lblMeta);
        desc =      (TextView) findViewById(R.id.txtDesc);
        seekPos =   (TextView) findViewById(R.id.lblOffset);
        seeker =    (SeekBar) findViewById(R.id.startOffset);
        preview =   (WebImageView) findViewById(R.id.preview);
        preview.setPlaceholderImage(R.mipmap.thumb_loading);

        (read = (Button)findViewById(R.id.bnRead)).setOnClickListener(this);
        seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateSeekerText();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        XMLNode cover = (XMLNode) getIntent().getSerializableExtra(EXTRADISPLAYDATA);
        if (type.equals("pool")) {
            coverID = Integer.parseInt(cover.getAttribute("id").orElse(""));
            max = Integer.parseInt(cover.getAttribute("post_count").orElse(""));
            name.setText(cover.getAttribute("name").orElse(""));
            meta.setText(Html.fromHtml(HTMLformat.bold("Creator: ") + cover.getAttribute("user_id").orElse("") + "<br>" +
                    HTMLformat.bold("Post count: ") + cover.getAttribute("post_count").orElse("") + "<br>" +
                    HTMLformat.bold("Created:<br> ") + cover.getAttribute("created_at").orElse("") + "<br>" +
                    HTMLformat.bold("Last edit:<br> ") + cover.getAttribute("updated_at").orElse("") + "<br>" +
                    HTMLformat.bold("Locked: ") + cover.getAttribute("is_locked").orElse("")));
        } else {
            coverID = Integer.parseInt(cover.getFirstChildContent("id").orElse(""));
            max = Integer.parseInt(cover.getFirstChildContent("post-count").orElse(""));
            name.setText(cover.getFirstChildContent("name").orElse(""));
            meta.setText(Html.fromHtml(HTMLformat.bold("Creator: ") + cover.getFirstChildContent("user-id").orElse("") + "<br>" +
                    HTMLformat.bold("Post count: ") + cover.getFirstChildContent("post-count").orElse("") + "<br>" +
                    HTMLformat.bold("Created:<br> ") + MiscStatics.readableTime(cover.getFirstChildContent("created-at").orElse("")) + "<br>" +
                    HTMLformat.bold("Last edit:<br> ") + MiscStatics.readableTime(cover.getFirstChildContent("updated-at").orElse("")) + "<br>" +
                    HTMLformat.bold("Type: ") + (Boolean.parseBoolean(cover.getFirstChildContent("public").orElse("")) ? "Public" : "Private")));
        }
        desc.setText(cover.getFirstChildContent("description").orElse(""));


        new XMLTask() {
            @Override protected void onPostExecute(XMLNode result) {
                String url;
                try {
                    //coverID = Integer.parseInt(result.getAttribute("id"));
                    XMLNode firstPost = result.getElementsByTagName("post").get(0);
                    String quality = database.getValue(SettingsActivity.SETTINGPREVIEWQUALITY);
                    url = firstPost.getFirstChildContent(quality).orElse("");
                    preview.setImageUrl(type + coverID, url, true);
                    entryPoint = firstPost.clone(); //throw parent away
                    read.setTextColor(getResources().getColor(R.color.colorAccent));
                    seeker.setMax(max-1);
                    updateSeekerText();
                    Logger.getLogger("a621").info(type + "_id: " + coverID + " previewURL: " + url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute(baseURL + (type.equals("set") ? "set/show.xml?id=" + cover.getFirstChildContent("id").orElse("") : "pool/show.xml?id=" + cover.getAttribute("id").orElse("")));
    }

    @Override public void onClick(View view) {
        if (view.getId() == R.id.bnRead) {
            if (entryPoint != null) {
                int start = seeker.getProgress();
                Logger.getLogger("a621").info(type + ": " + coverID + " offset:\n" + start);
                readFromTargetPost(start);
                //Intent intent = new Intent(getApplicationContext(), PostShowActivity.class);
                //intent.putExtra(type.equals("pool") ? PostShowActivity.EXTRAPOOLID : PostShowActivity.EXTRASETID, coverID);
                //intent.putExtra(PostShowActivity.EXTRAPOSTDATA, entryPoint);
                //intent.putExtra(PostShowActivity.EXTRASEARCHOFFSET, 0);
                //startActivity(intent);
            }
        }
    }

    public void readFromTargetPost(final int offset) {
        int poolPageSize=24;

        //get url/index for nth element
        String url; final int index;
        if (type.equals("pool")) {
            url="pool/show.xml?id="+coverID+"&page="+(int)(offset/poolPageSize+1); //page size for pools is 24? (natural counting)
            index=(int)(offset%poolPageSize);
        } else {
            url="post/index.xml?tags=set%3A" + coverID + "+order%3Aset&limit=1&page=" + offset;
            index=0;
        }

        new XMLTask() {
            @Override protected void onPostExecute(XMLNode result) {
                if (!result.getType().equals("posts")){ //try to return the first "posts" element found
                    List<XMLNode> wat = result.getElementsByTagName("posts");
                    if (wat.size()>0) result = wat.get(0);
                }
                if (result == null || !result.getType().equals("posts") || result.getChildCount()<=index) {
                    quickToast("No results");
                } else {
                    XMLNode child0 = result.getChildren().get(index);
                    Intent prev = new Intent(CoverShowActivity.this, PostShowActivity.class);
                    SQLiteDB database = new SQLiteDB(CoverShowActivity.this); database.open(); //required for blacklist string
                    if (new FilterManager(CoverShowActivity.this, database.getStringArray(SettingsActivity.SETTINGBLACKLIST)).isBlacklisted(child0))
                        child0.setAttribute("Blacklisted", "true");
                    prev.putExtra(PostShowActivity.EXTRAPOSTDATA, child0);
                    if (type.equals("pool"))
                        prev.putExtra(PostShowActivity.EXTRAPOOLID, coverID);
                    else
                        prev.putExtra(PostShowActivity.EXTRASETID, coverID);
                    prev.putExtra(PostShowActivity.EXTRASEARCHOFFSET, offset);
                    prev.putExtra(PostShowActivity.EXTRAPAGINATED, true);
                    CoverShowActivity.this.startActivity(prev);
                }
            }
        }.execute(baseURL + url);
    }

    void updateSeekerText() {
        if (seeker.getMax()==0) seekPos.setText("Empty");
        else seekPos.setText("From Page " + (seeker.getProgress()+1) + "/" + max);
    }
}

