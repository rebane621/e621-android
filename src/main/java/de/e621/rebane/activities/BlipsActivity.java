package de.e621.rebane.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.view.View;

import de.e621.rebane.a621.R;
import de.e621.rebane.components.listadapter.BlipListAdapter;
import de.e621.rebane.xmlreader.XMLNode;

public class BlipsActivity extends PaginatedListActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    final static String BLIPQUERYPAGE = "a621 SearchManager Blip Page";

    boolean postLoadUserdata=false;

    @Override
    @SuppressLint("MissingSuperCall")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.content_blips, savedInstanceState);
        postLayoutInflated(this.getClass());

        postLoadUserdata = Boolean.parseBoolean(database.getValue(SettingsActivity.SETTINGFANCYCOMMENTS));
    }

    @Override void setPagesize() {
        pagesize = 50; //given by API
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(BLIPQUERYPAGE, page);
    }

    @Override
    void handleIntent(Intent intent) {
        super.handleIntent(intent);
        page = intent.getIntExtra(BLIPQUERYPAGE,1);

        if (results==null) searchPosts("", page);
    }

    @Override void searchPosts(String escapedQuery, int page) {
        API_URI = "blip/index.xml?page="+page;
        API_LOGIN = false;

        super.searchPosts(escapedQuery, page);
    }

    @Override void onSearchResult(XMLNode result, String query, int page) {
        results = new BlipListAdapter(getApplicationContext(), R.id.lblTitle, result.children(), baseURL, blacklist, postLoadUserdata);
        //results.svNumPosts = (result.attributes().contains("count") ? Integer.valueOf(result.getAttribute("count")) : 0); //this value is not provided
        this.page = page;


        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.title_blips) + " | " + page);
        //setTitle(getResources().getString(R.string.title_posts) + " " + _page + (_query.equals("")?" | *":" | "+XMLUtils.unescapeXML(_query)));
    }

}
