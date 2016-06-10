package de.e621.rebane.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.e621.rebane.FilterManager;
import de.e621.rebane.a621.R;
import de.e621.rebane.service.DMailService;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLTask;

public class DMailShowActivity extends DrawerWrapper implements View.OnClickListener {

    public final static String DMAILMESSAGEID = "a621 DMail message ID";

    TextView lblRcpt, lblFrom, lblAge, lblMessage;

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

        /*(new XMLTask(this) {

            @Override protected void onPostExecute(XMLNode result) {

            }

            @Override public void onExecutionFailed(Exception exc) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        Toast.makeText(getApplicationContext(), "Unable to load DMail", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        }).execute();*/
    }

    @Override public void onClick(View view) {

    }
}