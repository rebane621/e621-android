package de.e621.rebane.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.net.URLDecoder;

import de.e621.rebane.a621.R;
import de.e621.rebane.components.listadapter.CoverListAdapter;
import de.e621.rebane.xmlreader.XMLNode;

public class SetsActivity extends PaginatedListActivity
        implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    Menu menu = null;

    @Override
    @SuppressLint("MissingSuperCall")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.content_cover_list, savedInstanceState);
        //setContentView(R.layout.activity_posts);
        postLayoutInflated(this.getClass());
    }

    void setPagesize() {
        pagesize=50; //hardcoded
    }

    @Override
    void handleIntent(Intent intent) {
        super.handleIntent(intent);
        //page = intent.getIntExtra(SEARCHQUERYPAGE,1); //done by super
        //can't search sets in a meaningfull way

        if (results==null) searchPosts("", page);
    }

    @Override
    void searchPosts(String escapedQuery, int page) {
        API_URI = "set/index.xml?page="+ page;
        API_LOGIN = false;   //for vote meta search

        super.searchPosts(escapedQuery, page);
    }

    void onSearchResult(XMLNode result, String query, int page) {
        openDB();
        String quality = database.getValue(SettingsActivity.SETTINGPREVIEWQUALITY); //i would really like to display thumbnails, a bit like a bookshelf

        results = new CoverListAdapter(this, R.id.txtMid, result.children(), quality, "set");
        //results.svNumPosts = (result.attributes().contains("count") ? Integer.valueOf(result.getAttribute("count")) : 0); // not returned by API
        SetsActivity.this.query = query; SetsActivity.this.page = page;

        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.title_sets) + " | " + page);
        ab.setSubtitle(URLDecoder.decode(query));
        //setTitle(getResources().getString(R.string.title_posts) + " " + _page + (_query.equals("")?" | *":" | "+XMLUtils.unescapeXML(_query)));
    }

    MenuItem searchMenuItem = null;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.generic, menu);
        this.menu = menu;

        return true;
    }
}
