package de.e621.rebane.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.view.View;

import de.e621.rebane.a621.R;
import de.e621.rebane.components.listadapter.ForumListAdapter;
import de.e621.rebane.xmlreader.XMLNode;


/** with what little information is returned by the forum API I won't continue that for now */
public class ForumsActivity extends PaginatedListActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    final static String FORUMQUERYPAGE = "a621 SearchManager Forum Page";

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paginated_list);
        postLayoutInflated(this.getClass());
    }*/

    @Override
    @SuppressLint("MissingSuperCall")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.content_paginated_list, savedInstanceState);
        //setContentView(R.layout.activity_posts);
        postLayoutInflated(this.getClass());
    }

    @Override void setPagesize() {
        pagesize = 30; //given by API
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(FORUMQUERYPAGE, page);
    }

    @Override
    void handleIntent(Intent intent) {
        super.handleIntent(intent);
        page = intent.getIntExtra(FORUMQUERYPAGE,1);

        if (results==null) searchPosts("", page);
    }

    @Override void searchPosts(String escapedQuery, int page) {
        API_URI = "forum/index.xml?page="+page;
        API_LOGIN = false;

        super.searchPosts(escapedQuery, page);
    }

    @Override void onSearchResult(XMLNode result, String query, int page) {
        results = new ForumListAdapter(getApplicationContext(), R.id.lblTitle, result.children());
        //results.svNumPosts = (result.attributes().contains("count") ? Integer.valueOf(result.getAttribute("count")) : 0); //this value is not provided
        this.page = page;


        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.title_forums) + " | " + page);
        //setTitle(getResources().getString(R.string.title_posts) + " " + _page + (_query.equals("")?" | *":" | "+XMLUtils.unescapeXML(_query)));
    }

}
