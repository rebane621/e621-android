package de.e621.rebane.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.e621.rebane.FilterManager;
import de.e621.rebane.SQLite.SQLiteDB;
import de.e621.rebane.a621.R;
import de.e621.rebane.components.WebImageView;

public class DrawerWrapper extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FilterManager blacklist;
    public static SQLiteDB database = null;
    public static String baseURL;
    public static Class<? extends DrawerWrapper> openActivity;

    final static String LIMITEREXTRA = "RequestAmmountLimiterTimestampArray";
    static int maxRequests = 30;
    static List<Long> requestLimiter = new LinkedList<Long>();
    public static boolean canRequest() {
        List<Long> remove = new ArrayList<Long>();
        for (Long l : requestLimiter)
            if (System.currentTimeMillis()-l > 60000)
                remove.add(l);
        requestLimiter.removeAll(remove);

        if (requestLimiter.size() < maxRequests) {
            requestLimiter.add(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    void handleIntent(Intent intent) {
        long[] values = intent.getLongArrayExtra(LIMITEREXTRA);
        if (values != null) for (long l : values) requestLimiter.add(l);
        canRequest();

        //apply settings
        /*/
        String extra = intent.getStringExtra(SettingsActivity.SETTINGDEFAULTSEARCH);
        if (extra != null) database.setValue(SettingsActivity.SETTINGDEFAULTSEARCH, extra);
        String[] tmp = intent.getStringArrayExtra(SettingsActivity.SETTINGBLACKLIST);
        if (tmp != null && tmp.length > 0) database.setStringArray(SettingsActivity.SETTINGBLACKLIST, tmp);
        extra = intent.getStringExtra(SettingsActivity.SETTINGBASEURL);
        if (extra != null) database.setValue(SettingsActivity.SETTINGBASEURL, extra);
        /*/
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if (requestLimiter.size()>0) {
            long[] vals = new long[requestLimiter.size()];
            for (int i = 0; i < requestLimiter.size(); i++) vals[i] = (long) requestLimiter.get(i);
            outState.putLongArray(LIMITEREXTRA, vals);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        openDB();
        blacklist = new FilterManager(this, database.getStringArray(SettingsActivity.SETTINGBLACKLIST));
        baseURL = database.getValue(SettingsActivity.SETTINGBASEURL);
        //eventually get login and other data
    }

    @Override
    protected void onResume() {
        super.onResume();
        openDB();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (database != null) { database.close(); database = null; }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (database != null) { database.close(); database = null; }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        WebImageView.clear();
    }

    protected void onCreateDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            /*/Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setClass(this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.SETTINGDEFAULTSEARCH, database.getValue(SettingsActivity.SETTINGDEFAULTSEARCH));
            intent.putExtra(SettingsActivity.SETTINGBASEURL, database.getValue(SettingsActivity.SETTINGBASEURL));
            intent.putExtra(SettingsActivity.SETTINGBLACKLIST, database.getStringArray(SettingsActivity.SETTINGBLACKLIST));
            startActivityForResult(intent, SettingsActivity.SETTING_CONFIRM);
            /*/
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_search) {

        }

        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SettingsActivity.SETTING_CONFIRM) {
            if (resultCode == RESULT_OK) {
                // A contact was picked.  Here we will just display it
                // to the user.
                handleIntent(data);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_posts && !openActivity.equals(PostsActivity.class)) {
            Intent intent = new Intent(getApplicationContext(), PostsActivity.class);
            this.startActivity(intent);
        } else if (id == R.id.nav_pools) {

        } else if (id == R.id.nav_sets) {

        } else if (id == R.id.nav_blips) {

        } else if (id == R.id.nav_forum) {

        } else if (id == R.id.nav_login) {

        } else if (id == R.id.nav_dmail) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void quickToast(CharSequence message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    void openDB() {
        if (database == null) { (database = new SQLiteDB(this)).open(); }
    }
}
