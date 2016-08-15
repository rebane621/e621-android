package de.e621.rebane.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

import de.e621.rebane.DTextParser;
import de.e621.rebane.HTMLformat;
import de.e621.rebane.MiscStatics;
import de.e621.rebane.a621.R;
import de.e621.rebane.xmlreader.XMLNode;

public class DMailShowActivity extends DrawerWrapper implements View.OnClickListener {

    public final static String DMAILMESSAGEID = "a621 DMail message ID";
    public final static String DMAILMESSAGENODE = "a621 DMail message XML Node";

    TextView lblRcpt, lblFrom, lblAge, lblMessage, lblTopic;

    @Override
    @SuppressLint("MissingSuperCall")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.content_dmailshow, savedInstanceState);
        //setContentView(R.layout.content_dmailshow);
        onCreateDrawer(this.getClass());   //DrawerWrapper function that requires the layout to be set to prepare the drawer

        lblRcpt =       (TextView) findViewById(R.id.lblRcpt);
        lblFrom =       (TextView) findViewById(R.id.lblFrom);
        lblAge =        (TextView) findViewById(R.id.lblAge);
        lblMessage =    (TextView) findViewById(R.id.lblMessage);
        lblTopic =      (TextView) findViewById(R.id.lblTopic);

        Integer myID = new Integer(getIntent().getIntExtra(DMAILMESSAGEID, 0));
        XMLNode node = (XMLNode)getIntent().getSerializableExtra(DMAILMESSAGENODE);

        lblRcpt.setText(node.getFirstChildText("to-id"));
        lblFrom.setText(node.getFirstChildText("from-id"));
        lblAge.setText(MiscStatics.readableTime(node.getFirstChildText("created-at")));
        lblMessage.setText(Html.fromHtml(DTextParser.parse(node.getFirstChildText("body"))));

        Integer from = Integer.parseInt(node.getFirstChildText("from-id")),
                to = Integer.parseInt(node.getFirstChildText("to-id"));
        Integer dir = ((myID == 0 || from.equals(to)) ? 0 : (myID.compareTo(to)!=0 ? 1 : -1));
        int colorcode = (dir < 0 ? getApplicationContext().getResources().getColor(R.color.preview_green) :
                (dir > 0 ? getApplicationContext().getResources().getColor(R.color.preview_red) :
                        getApplicationContext().getResources().getColor(R.color.text_neutral)));
        lblTopic.setText(Html.fromHtml(HTMLformat.colored(node.getFirstChildText("title"), colorcode)));

        //post load usernames

        //load dmail/show/id to mark read
        if (!Boolean.parseBoolean(node.getFirstChildText("has-seen"))) {
            new AsyncTask<String, Void, Void>() {
                @Override protected Void doInBackground(String... args) {
                    try {
                        HttpURLConnection urlc = (HttpURLConnection) new URL(args[0]).openConnection();
                        urlc.addRequestProperty("User-Agent", args[1]);
                        urlc.getContentLength();
                    } catch (Exception e) {
                    }
                    return null;
                }
            }.execute(database.getValue(SettingsActivity.SETTINGBASEURL) + "dmail/show/" + node.getFirstChildText("id"), getApplicationContext().getString(R.string.requestUserAgent));
        }
    }

    @Override public void onClick(View view) {

    }
}