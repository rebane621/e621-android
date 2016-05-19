package de.e621.rebane.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.logging.Logger;

import de.e621.rebane.SQLite.SQLiteDB;
import de.e621.rebane.a621.R;
import de.e621.rebane.components.WebImageView;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_launcher);

        SQLiteDB database = new SQLiteDB(this);
        database.open();
        String cname = database.getValue(SettingsActivity.SETTINGLAUNCHACTIVITY);
        database.close();
        if (cname != null) cname = this.getClass().getPackage().getName() + "." + cname + "Activity";
        //try { Thread.sleep(2000); } catch (Exception e) {}
        Intent intent = null;
        try {
            intent = new Intent(this, Class.forName(cname));
        } catch (Exception e) {
            Logger.getLogger("a621").info("No Activity for " + cname);
            intent = new Intent(this, PostsActivity.class);
        } finally {
            startActivity(intent);
        }
        finish();
    }

    //@Override protected void onResume() {
    //    super.onResume();
    //}
}
