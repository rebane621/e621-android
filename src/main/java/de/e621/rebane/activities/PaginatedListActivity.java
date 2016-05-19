package de.e621.rebane.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.logging.Logger;

import de.e621.rebane.MiscStatics;
import de.e621.rebane.a621.R;
import de.e621.rebane.components.WebImageView;
import de.e621.rebane.components.XMLListAdapter;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLTask;

public abstract class PaginatedListActivity extends DrawerWrapper
        implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    GridView lstContent;
    XMLListAdapter results = null;
    LinearLayout pagebar;
    ImageView pageNext, pageLast, pageFirst;
    int page=1;
    String query = "";
    SwipeRefreshLayout swipeLayout;
    Menu menu = null;
    int pagesize = 15;

    String API_URI; //SET THIS IN YOUR SUBCLASS
    Boolean API_LOGIN; //SET THIS IN YOUR SUBCLASS

    final static String SEARCHQUERYPAGE = "a621 Pagination Page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    void postLayoutInflated(Class<? extends DrawerWrapper> subclass) {
        onCreateDrawer();   //DrawerWrapper function that requires the layout to be set to prepare the drawer
        DrawerWrapper.openActivity = subclass;   //block the drawer from reopening this activity while opened

        //Logger.getLogger("a621").info("Paginated List Activity created");

        lstContent =      (GridView)              findViewById(R.id.lstPostResults);
        swipeLayout =   (SwipeRefreshLayout)    findViewById(R.id.swipe_container);
        pagebar =       (LinearLayout)          findViewById(R.id.pagebar);

        (pageNext =     (ImageView)                findViewById(R.id.bnNext)).setOnClickListener(this);
        (pageLast =     (ImageView)                findViewById(R.id.bnLast)).setOnClickListener(this);
        (pageFirst =    (ImageView)                findViewById(R.id.bnFirst)).setOnClickListener(this);

        lstContent.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView lv, int i, int i1, int i2) {
                if (lstContent == null || pagebar == null || results == null) return;

                //boolean canNext = results.canPaginateNext(page, pagesize), canLast = results.canPaginateLast(page);
                int lastPage = results.getLastPage(pagesize);

                if (lv.getId() == R.id.lstPostResults) {
                    if ((lstContent.getFirstVisiblePosition()==0 || lstContent.getLastVisiblePosition()>=results.getResultCount()-1) && (lastPage>1)) {
                        if (pagebar.getVisibility()==View.GONE) {
                            Animation bottomUp = AnimationUtils.loadAnimation(PaginatedListActivity.this, R.anim.bottom_up);
                            pagebar.startAnimation(bottomUp);
                            pagebar.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (pagebar.getVisibility()==View.VISIBLE) {
                            Animation bottomDown = AnimationUtils.loadAnimation(PaginatedListActivity.this, R.anim.bottom_down);
                            pagebar.startAnimation(bottomDown);
                            pagebar.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        setPagesize();

        handleIntent(getIntent());
    }

    @Override public void onRefresh() {
        WebImageView.clear();   //in case a stream was interrupted, so it's propperly downloading
        searchPosts(query, page);
    }

    abstract void setPagesize();

    @Override
    protected void onResume() {
        super.onResume();
        //Logger.getLogger("a621").info("Activity resumed");
        setPagesize();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!query.equals("")) {
            searchPosts("", 1);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.getLogger("a621").info("Activity got new intent");
        results=null;
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(SEARCHQUERYPAGE, page);
        outState.putString(SearchManager.QUERY, query);
    }

    /** If overriding this don't forget call super.handleIntent at the END */
    @Override
    void handleIntent(Intent intent) {
        super.handleIntent(intent);
        page = intent.getIntExtra(SEARCHQUERYPAGE,1);
        String tmp = intent.getStringExtra(SearchManager.QUERY);
        if (tmp != null) query = tmp;
        if (results==null) searchPosts(query, page);
    }

    void searchPosts(String escapedQuery, int page) {
        if (!MiscStatics.canRequest(this) || swipeLayout.isRefreshing()) return;
        swipeLayout.setRefreshing(true);

        openDB();
        baseURL = database.getValue(SettingsActivity.SETTINGBASEURL);
        if (baseURL == null || baseURL.isEmpty() || !baseURL.startsWith("https")) database.setValue(SettingsActivity.SETTINGBASEURL, baseURL = "https://e621.net/");
        String quality = database.getValue(SettingsActivity.SETTINGPREVIEWQUALITY);
        if (quality == null || quality.isEmpty()) database.setValue(SettingsActivity.SETTINGPREVIEWQUALITY, quality = "preview_url");

        Logger.getLogger("a621").info("Requesting page "+ baseURL + API_URI);

        swipeLayout.setRefreshing(true);
        final int _page = page;
        final String _query = escapedQuery;

        (new XMLTask(this) {
            @Override
            protected void onPostExecute(XMLNode result) {

                swipeLayout.setRefreshing(false);
                if (result == null) { quickToast("Error while request / parsing!"); return; }
                if (result.getChildCount() <= 0) { quickToast("No results found..."); return; }

                onSearchResult(result, _query, _page);

                pageFirst.setVisibility(_page>1?View.VISIBLE:View.GONE);
                pageLast.setVisibility(_page>2?View.VISIBLE:View.GONE);
                pageNext.setVisibility(_page<results.getLastPage(pagesize)?View.VISIBLE:View.GONE);
                lstContent.setAdapter(results);
                lstContent.refreshDrawableState();

            }
        }).execute( baseURL + API_URI + (API_LOGIN != null ? login.getLogin("&") : ""));
    }

    abstract void onSearchResult(XMLNode result, String query, int page);

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.generic, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bnNext:
                //Logger.getLogger("a621").info("next " + (page+1));
                if ((page-1)*pagesize + results.getResultCount() < results.svNumPosts)
                    searchPosts(query, page+1);
                break;
            case R.id.bnLast:
                //Logger.getLogger("a621").info("last " + (page-1));
                if (page > 2)
                    searchPosts(query, page-1);
                break;
            case R.id.bnFirst:
                //Logger.getLogger("a621").info("jump 1");
                if (page > 1)
                    searchPosts(query, 1);
                break;
        }
    }
}
