package de.e621.rebane.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.itwookie.XMLreader.XMLNode;

import java.net.HttpURLConnection;
import java.net.URL;

import de.e621.rebane.DTextParser;
import de.e621.rebane.HTMLformat;
import de.e621.rebane.MiscStatics;
import de.e621.rebane.a621.R;

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

        lblRcpt.setText(node.getFirstChildContent("to-id").orElse(""));
        lblFrom.setText(node.getFirstChildContent("from-id").orElse(""));
        lblAge.setText(MiscStatics.readableTime(node.getFirstChildContent("created-at").orElse("")));
        lblMessage.setText(Html.fromHtml(DTextParser.parse(node.getFirstChildContent("body").orElse(""))));

        Integer from = Integer.parseInt(node.getFirstChildContent("from-id").orElse("")),
                to = Integer.parseInt(node.getFirstChildContent("to-id").orElse(""));
        Integer dir = ((myID == 0 || from.equals(to)) ? 0 : (myID.compareTo(to)!=0 ? 1 : -1));
        int colorcode = (dir < 0 ? getApplicationContext().getResources().getColor(R.color.preview_green) :
                (dir > 0 ? getApplicationContext().getResources().getColor(R.color.preview_red) :
                        getApplicationContext().getResources().getColor(R.color.text_neutral)));
        lblTopic.setText(Html.fromHtml(HTMLformat.colored(node.getFirstChildContent("title").orElse(""), colorcode)));

        //post load usernames

        //load dmail/show/id to mark read
        if (!Boolean.parseBoolean(node.getFirstChildContent("has-seen").orElse(""))) {
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
            }.execute(database.getValue(SettingsActivity.SETTINGBASEURL) + "dmail/show/" + node.getFirstChildContent("id").orElse(""), getApplicationContext().getString(R.string.requestUserAgent));
        }
    }

    @Override public void onClick(View view) {

    }
}