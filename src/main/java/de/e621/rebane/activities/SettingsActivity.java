package de.e621.rebane.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import java.net.URLDecoder;
import java.net.URLEncoder;

import de.e621.rebane.FilterManager;
import de.e621.rebane.a621.R;

public class SettingsActivity extends DrawerWrapper
        implements View.OnClickListener {

    final static int SETTING_CONFIRM = 0;
    public final static String SETTINGDEFAULTSEARCH = "AppDefaultPostSearch";
    public final static String SETTINGBLACKLIST = "AppPostBlacklist";
    public final static String SETTINGBASEURL = "AppBaseWebUrl";
    public final static String SETTINGPOSTSPERPAGE = "AppPostsPerPage";
    public final static String SETTINGSPREVIEWQUALITY = "AppPreviewImageQuality";

    EditText txtDefaultSearch, txtBlacklist, txtBaseURL, txtPostPage;
    Switch chkQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtDefaultSearch =  (EditText)  findViewById(R.id.txtDSearch);
        txtBlacklist =      (EditText)  findViewById(R.id.txtBlacklist);
        txtBaseURL =        (EditText)  findViewById(R.id.txtBaseURL);
        txtPostPage =       (EditText)  findViewById(R.id.txtPostsPerPage);
        chkQuality =        (Switch)    findViewById(R.id.chkQuality);
        ((Button)findViewById(R.id.bnApply)).setOnClickListener(this);

        String extra = database.getValue(SETTINGDEFAULTSEARCH);
        if (extra != null) txtDefaultSearch.setText(URLDecoder.decode(extra));
        String[] tmp = database.getStringArray(SETTINGBLACKLIST);
        if (tmp != null && tmp.length > 0) {
            extra = "";
            for (int i = 0; i < tmp.length; i++) extra = extra + tmp[i] + '\n';
            txtBlacklist.setText(extra);
        }
        extra = database.getValue(SETTINGBASEURL);
        if (extra == null || extra.isEmpty() || !extra.startsWith("https")) database.setValue(SETTINGBASEURL, baseURL = "https://e621.net/");
        txtBaseURL.setText(extra);
        extra = database.getValue(SETTINGPOSTSPERPAGE);
        int pagesize;
        if (extra == null || extra.isEmpty()) {
            pagesize = 15;
            database.setValue(SETTINGPOSTSPERPAGE, "15");
        } else {
            try {
                pagesize = Integer.valueOf(extra);
            } catch (Exception e) {
                pagesize = 15;
                database.setValue(SETTINGPOSTSPERPAGE, "15");
            }
            if (pagesize < 1 || pagesize > 100) {
                pagesize = 15;
                database.setValue(SETTINGPOSTSPERPAGE, "15");
            }
        }
        txtPostPage.setText(String.valueOf(pagesize));
        extra = database.getValue(SETTINGSPREVIEWQUALITY);
        if (extra == null || extra.isEmpty()) database.setValue(SETTINGSPREVIEWQUALITY, extra = "preview_url");
        chkQuality.setChecked(extra.equals("sample_url"));

        handleIntent(getIntent());
    }

    @Override
    void handleIntent(Intent intent) {
        super.handleIntent(intent);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bnApply) {
            openDB();
            database.setValue(SETTINGDEFAULTSEARCH, URLEncoder.encode(txtDefaultSearch.getText().toString()));
            String[] tmp = txtBlacklist.getText().toString().split("\n");
            if (tmp != null && tmp.length > 0) {
                database.setStringArray(SETTINGBLACKLIST, tmp);
                blacklist = new FilterManager(this, tmp);
            }
            database.setValue(SETTINGBASEURL, txtBaseURL.getText().toString());
            int pagesize = Integer.valueOf(txtPostPage.getText().toString());
            if (pagesize < 1 || pagesize > 100) pagesize = 100;
            database.setValue(SETTINGPOSTSPERPAGE, String.valueOf(pagesize));
            database.setValue(SETTINGSPREVIEWQUALITY, (chkQuality.isChecked()?"sample_url":"preview_url"));
            finish();
        }
    }
}
