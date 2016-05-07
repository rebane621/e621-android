package de.e621.rebane.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;

import de.e621.rebane.FilterManager;
import de.e621.rebane.a621.R;
import de.e621.rebane.service.DMailService;

public class SettingsActivity extends DrawerWrapper
        implements View.OnClickListener {

    final static int SETTING_CONFIRM = 0;
    public final static String SETTINGDEFAULTSEARCH = "AppDefaultPostSearch";
    public final static String SETTINGBLACKLIST = "AppPostBlacklist";
    public final static String SETTINGBASEURL = "AppBaseWebUrl";
    public final static String SETTINGPOSTSPERPAGE = "AppPostsPerPage";
    public final static String SETTINGPREVIEWQUALITY = "AppPreviewImageQuality";
    public final static String SETTINGDMAILSERVICE = "BackgroundDMailService";
    public final static String SETTINGDEFAULTSAVE = "AddDefauleSaveLocation";
    public final static String SETTINGFANCYCOMMENTS = "AppLoadUserdataOnComments";

    EditText txtDefaultSearch, txtBlacklist, txtBaseURL, txtPostPage, txtDefaultSave;
    Switch chkQuality, chkDMail, chkFancyComments;

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
        txtDefaultSave =    (EditText)  findViewById(R.id.txtDefaultSave);
        chkQuality =        (Switch)    findViewById(R.id.chkQuality);
        chkDMail =          (Switch)    findViewById(R.id.chkDMail);
        chkFancyComments =  (Switch)    findViewById(R.id.chkFancyComments);
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
        extra = database.getValue(SETTINGDEFAULTSAVE);
        if (extra == null || extra.isEmpty()) database.setValue(SETTINGDEFAULTSAVE, extra = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "e621").getAbsolutePath());
        txtDefaultSave.setText(extra);
        txtDefaultSave.setOnClickListener(this);
        extra = database.getValue(SETTINGPREVIEWQUALITY);
        if (extra == null || extra.isEmpty()) database.setValue(SETTINGPREVIEWQUALITY, extra = "preview_url");
        chkQuality.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                compoundButton.setText((b?"Acceptable":"Mashed Potato"));
            }
        });
        chkQuality.setChecked(extra.equals("sample_url"));
        chkDMail.setChecked(Boolean.parseBoolean(database.getValue(SETTINGDMAILSERVICE)));
        chkFancyComments.setChecked(Boolean.parseBoolean(database.getValue(SETTINGFANCYCOMMENTS)));

        handleIntent(getIntent());
    }

    @Override
    void handleIntent(Intent intent) {
        super.handleIntent(intent);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.txtDefaultSave) {
            Intent fchooser = new Intent(this, FolderChooser.class);
            fchooser.putExtra(FolderChooser.FOLDERINTENTEXTRA, txtDefaultSave.getText().toString());
            startActivityForResult(fchooser, 1);
        } else if (view.getId() == R.id.bnApply) {
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
            database.setValue(SETTINGDEFAULTSAVE, txtDefaultSave.getText().toString());
            database.setValue(SETTINGPREVIEWQUALITY, (chkQuality.isChecked()?"sample_url":"preview_url"));
            database.setValue(SETTINGDMAILSERVICE, String.valueOf(chkDMail.isChecked()));
            if (!chkDMail.isChecked()) {    //settings was turned off
                DMailService.stop();
            }
            database.setValue(SETTINGFANCYCOMMENTS, String.valueOf(chkFancyComments.isChecked()));
            //database.close();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra(FolderChooser.FOLDERINTENTEXTRA);
                if (result != null) txtDefaultSave.setText(result);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                //nothing changes
            }
        }
    }
}
