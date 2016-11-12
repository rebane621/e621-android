package de.e621.rebane.activities;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.itwookie.XMLreader.XMLNode;

import java.net.URLDecoder;

import de.e621.rebane.a621.R;
import de.e621.rebane.components.listadapter.CoverListAdapter;

public class PoolsActivity extends PaginatedListActivity
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
        pagesize=20; //hardcoded
    }

    @Override
    void handleIntent(Intent intent) {
        super.handleIntent(intent);
        //page = intent.getIntExtra(SEARCHQUERYPAGE,1); //done by super
        if (!Intent.ACTION_SEARCH.equals(intent.getAction())) { //true case handled by super
            query = "";
        }

        if (results==null) searchPosts(query, page);
    }

    @Override
    void searchPosts(String escapedQuery, int page) {
        API_URI = "pool/index.xml?query="+escapedQuery+"&page="+ page;
        API_LOGIN = false;   //for vote meta search

        super.searchPosts(escapedQuery, page);
    }

    void onSearchResult(XMLNode result, String query, int page) {
        openDB();
        String quality = database.getValue(SettingsActivity.SETTINGPREVIEWQUALITY); //i would really like to display thumbnails, a bit like a bookshelf

        results = new CoverListAdapter(this, R.id.txtMid, result.getChildren(), quality, "pool");
        //results.svNumPosts = (result.attributes().contains("count") ? Integer.valueOf(result.getAttribute("count")) : 0); // not returned by API
        PoolsActivity.this.query = query; PoolsActivity.this.page = page;

        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.title_pools) + " | " + page);
        ab.setSubtitle(URLDecoder.decode(query));
        //setTitle(getResources().getString(R.string.title_posts) + " " + _page + (_query.equals("")?" | *":" | "+XMLUtils.unescapeXML(_query)));
    }

    MenuItem searchMenuItem = null;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.posts, menu);
        this.menu = menu;

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) (searchMenuItem = menu.findItem(R.id.action_search)).getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    MenuItem searchMenuItem = PoolsActivity.this.searchMenuItem;
                    if (searchMenuItem != null) {
                        searchMenuItem.collapseActionView();
                    }
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    return true;
                }
            });
        }
        else Toast.makeText(getApplicationContext(), "Could not enable Search!", Toast.LENGTH_LONG).show();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            SearchView sv = (SearchView)item.getActionView();
            item.expandActionView();
            sv.setQuery(URLDecoder.decode(query), false);
        }

        return super.onOptionsItemSelected(item);
    }

}
