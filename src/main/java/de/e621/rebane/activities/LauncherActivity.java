package de.e621.rebane.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import junit.runner.Version;

import java.util.logging.Logger;

import de.e621.rebane.HTMLformat;
import de.e621.rebane.SQLite.SQLiteDB;
import de.e621.rebane.a621.R;
import de.e621.rebane.components.WebImageView;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLTask;

public class LauncherActivity extends AppCompatActivity {

    public static final String DATABASELASTUPDATE = "LastDayChecked4Updates";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_launcher);

        SQLiteDB db = new SQLiteDB(this);
        db.open();

        Integer lastCheck=null, today=(int)(System.currentTimeMillis()/86400000); //convert to days
        try {
            lastCheck = Integer.parseInt(db.getValue(DATABASELASTUPDATE));
        } catch (Exception e) {}//never checked
        if (today.equals(lastCheck)) {//check up to once a day
            Logger.getLogger("a621").info("Already checked for updates today");
            goon();
            return;
        }
        db.setValue(DATABASELASTUPDATE, today.toString());

        new XMLTask(getApplicationContext()) {
            @Override protected void onPostExecute(XMLNode result) {
                if (result==null || result.getChildCount()==0) { goon(); return; }
                String body = result.getFirstChildText("body");
                int pos = body.indexOf("(Current version ");
                if (pos<0) { goon(); return; }
                int start = pos+17; //17=len("(Current version ")
                String vnew = body.substring(start, body.indexOf(')', start)),
                vcur = LauncherActivity.this.getResources().getString(R.string.version);
                if (!(vcur.equals(vnew))) {
                    AlertDialog.Builder n = new AlertDialog.Builder(LauncherActivity.this);
                    n.setTitle("Update available");
                    TextView fMsg = new TextView(LauncherActivity.this);
                    fMsg.setPadding(8,8,8,8);
                    fMsg.setText(Html.fromHtml("Installed: " + vcur + "<br>Latest: " + vnew + "<br>Please visit the <a href=\"https://e621.net/forum/show.xml?id=191258\">Forum</a> for more information."));
                    n.setView(fMsg);
                    n.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            goon();
                        }
                    });
                    n.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override public void onDismiss(DialogInterface dialogInterface) {
                            goon();
                        }
                    });
                    n.show();
                } else goon();
            }

            @Override public void onExecutionFailed(Exception exc) {
                goon();
            }

        }.execute("https://e621.net/forum/show.xml?id=191258");
    }

    public void goon() {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                SQLiteDB database = new SQLiteDB(LauncherActivity.this);
                database.open();
                String cname = database.getValue(SettingsActivity.SETTINGLAUNCHACTIVITY);
                database.close();
                if (cname != null) cname = this.getClass().getPackage().getName() + "." + cname + "Activity";
                //try { Thread.sleep(2000); } catch (Exception e) {}
                Intent intent = null;
                try {
                    intent = new Intent(LauncherActivity.this, Class.forName(cname));
                } catch (Exception e) {
                    Logger.getLogger("a621").info("No Activity for " + cname);
                    intent = new Intent(LauncherActivity.this, PostsActivity.class);
                } finally {
                    startActivity(intent);
                }
                finish();
            }
        });
    }
    //@Override protected void onResume() {
    //    super.onResume();
    //}
}
