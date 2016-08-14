package de.e621.rebane.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.logging.Logger;

import de.e621.rebane.HTMLformat;
import de.e621.rebane.MiscStatics;
import de.e621.rebane.a621.R;
import de.e621.rebane.components.WebImageView;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLTask;

public class CoverShowActivity extends DrawerWrapper implements View.OnClickListener {

    TextView name, meta, desc;
    Button read;
    WebImageView preview;
    public XMLNode entryPoint = null;
    public int coverID = -1;
    String type;

    public static final String EXTRADISPLAYDATA = "CoverDataForDisplay";
    public static final String EXTRADISPLAYTYPE = "CoverDisplayType";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    @SuppressLint("MissingSuperCall")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.content_cover_show, savedInstanceState);
        //setContentView(R.layout.activity_login);
        onCreateDrawer(this.getClass());   //DrawerWrapper function that requires the layout to be set to prepare the drawer

        type = getIntent().getStringExtra(EXTRADISPLAYTYPE);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(type.substring(0,1).toUpperCase() + type.substring(1).toLowerCase() + " Cover");

        name = (TextView) findViewById(R.id.lblTitle);
        meta = (TextView) findViewById(R.id.lblMeta);
        desc = (TextView) findViewById(R.id.txtDesc);
        preview = (WebImageView) findViewById(R.id.preview);
        preview.setPlaceholderImage(R.mipmap.thumb_loading);

        (read = (Button)findViewById(R.id.bnRead)).setOnClickListener(this);

        XMLNode cover = (XMLNode) getIntent().getSerializableExtra(EXTRADISPLAYDATA);
        if (type.equals("pool")) {
            coverID = Integer.parseInt(cover.getAttribute("id"));
            name.setText(cover.getAttribute("name"));
            meta.setText(Html.fromHtml(HTMLformat.bold("Creator: ") + cover.getAttribute("user_id") + "<br>" +
                    HTMLformat.bold("Post count: ") + cover.getAttribute("post_count") + "<br>" +
                    HTMLformat.bold("Created:<br> ") + cover.getAttribute("created_at") + "<br>" +
                    HTMLformat.bold("Last edit:<br> ") + cover.getAttribute("updated_at") + "<br>" +
                    HTMLformat.bold("Locked: ") + cover.getAttribute("is_locked")));
        } else {
            coverID = Integer.parseInt(cover.getFirstChildText("id"));
            name.setText(cover.getFirstChildText("name"));
            meta.setText(Html.fromHtml(HTMLformat.bold("Creator: ") + cover.getFirstChildText("user-id") + "<br>" +
                    HTMLformat.bold("Post count: ") + cover.getFirstChildText("post-count") + "<br>" +
                    HTMLformat.bold("Created:<br> ") + MiscStatics.readableTime(cover.getFirstChildText("created-at")) + "<br>" +
                    HTMLformat.bold("Last edit:<br> ") + MiscStatics.readableTime(cover.getFirstChildText("updated-at")) + "<br>" +
                    HTMLformat.bold("Type: ") + (Boolean.parseBoolean(cover.getFirstChildText("public")) ? "Public" : "Private")));
        }
        desc.setText(cover.getFirstChildText("description"));

        new XMLTask(getApplicationContext()) {
            @Override protected void onPostExecute(XMLNode result) {
                String url;
                try {
                    //coverID = Integer.parseInt(result.getAttribute("id"));
                    XMLNode firstPost = result.getElementsByTagName("post")[0];
                    String quality = database.getValue(SettingsActivity.SETTINGPREVIEWQUALITY);
                    url = firstPost.getFirstChildText(quality);
                    preview.setImageUrl(type + coverID, url, true);
                    entryPoint = firstPost.clone(); //throw parent away
                    read.setTextColor(getResources().getColor(R.color.colorAccent));
                    Logger.getLogger("a621").info(type + "_id: " + coverID + " previewURL: " + url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute(baseURL + (type.equals("set") ? "set/show.xml?id=" + cover.getFirstChildText("id") : "pool/show.xml?id=" + cover.getAttribute("id")));
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override public void onClick(View view) {
        if (view.getId() == R.id.bnRead) {
            if (entryPoint != null) {
                Logger.getLogger("a621").info(type + ": " + coverID + " XMLnode:\n" + entryPoint.toString());
                Intent intent = new Intent(getApplicationContext(), PostShowActivity.class);
                intent.putExtra(type.equals("pool") ? PostShowActivity.EXTRAPOOLID : PostShowActivity.EXTRASETID, coverID);
                intent.putExtra(PostShowActivity.EXTRAPOSTDATA, entryPoint);
                intent.putExtra(PostShowActivity.EXTRASEARCHOFFSET, 0);
                startActivity(intent);
            }
        }
    }
}

