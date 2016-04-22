package de.e621.rebane.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Logger;

import de.e621.rebane.a621.R;
import de.e621.rebane.components.PostListAdapter;
import de.e621.rebane.components.WebImageView;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLTask;

public class PostsActivity extends DrawerWrapper
        implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    ListView lstPosts;
    PostListAdapter results = null;
    LinearLayout pagebar;
    ImageView pageNext, pageLast, pageFirst;
    int page=1;
    String query = "";
    SwipeRefreshLayout swipeLayout;
    Menu menu = null;
    static int pagesize = 15;

    final static String SEARCHQUERYPAGE = "a621 SearchManager Post Page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);
        onCreateDrawer();   //DrawerWrapper function that requires the layout to be set to prepare the drawer
        DrawerWrapper.openActivity = this.getClass();   //block the drawer from reopening this activity while opened

        Logger.getLogger("a621").info("Activity created");

        lstPosts =      (ListView)              findViewById(R.id.lstPostResults);
        swipeLayout =   (SwipeRefreshLayout)    findViewById(R.id.swipe_container);
        pagebar =       (LinearLayout)          findViewById(R.id.pagebar);

        (pageNext =     (ImageView)                findViewById(R.id.bnNext)).setOnClickListener(this);
        (pageLast =     (ImageView)                findViewById(R.id.bnLast)).setOnClickListener(this);
        (pageFirst =    (ImageView)                findViewById(R.id.bnFirst)).setOnClickListener(this);

        lstPosts.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView lv, int i, int i1, int i2) {
                if (lstPosts == null || pagebar == null || results == null) return;

                //boolean canNext = results.canPaginateNext(page, pagesize), canLast = results.canPaginateLast(page);
                int lastPage = results.getLastPage(pagesize);

                if (lv.getId() == R.id.lstPostResults) {
                    if ((lstPosts.getFirstVisiblePosition()==0 || lstPosts.getLastVisiblePosition()>=results.getResultCount()-1) && (lastPage>1)) {
                        if (pagebar.getVisibility()==View.GONE) {
                            Animation bottomUp = AnimationUtils.loadAnimation(PostsActivity.this, R.anim.bottom_up);
                            pagebar.startAnimation(bottomUp);
                            pagebar.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (pagebar.getVisibility()==View.VISIBLE) {
                            Animation bottomDown = AnimationUtils.loadAnimation(PostsActivity.this, R.anim.bottom_down);
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

    void setPagesize() {
        String pgsize = database.getValue(SettingsActivity.SETTINGPOSTSPERPAGE);
        if (pgsize == null || pgsize.isEmpty()) {
            pagesize = 15;
            database.setValue(SettingsActivity.SETTINGPOSTSPERPAGE, "15");
        } else {
            try {
                pagesize = Integer.valueOf(pgsize);
            } catch (Exception e) {
                pagesize = 15;
                database.setValue(SettingsActivity.SETTINGPOSTSPERPAGE, "15");
            }
            if (pagesize < 1 || pagesize > 100) {
                pagesize = 15;
                database.setValue(SettingsActivity.SETTINGPOSTSPERPAGE, "15");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.getLogger("a621").info("Activity resumed");
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

    @Override
    void handleIntent(Intent intent) {
        super.handleIntent(intent);
        page = intent.getIntExtra(SEARCHQUERYPAGE,1);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            query = (query==null?"":URLEncoder.encode(query));
        } else {
            openDB();
            query = database.getValue(SettingsActivity.SETTINGDEFAULTSEARCH);
            if (query == null) query = "";
        }

        if (results==null) searchPosts(query, page);
    }

    void searchPosts(String escapedQuery, int page) {
        if (!canRequest()) { quickToast("Cooldown!\nTry again in a few seconds"); return; }

        openDB();
        baseURL = database.getValue(SettingsActivity.SETTINGBASEURL);
        if (baseURL == null || baseURL.isEmpty() || !baseURL.startsWith("https")) database.setValue(SettingsActivity.SETTINGBASEURL, baseURL = "https://e621.net/");
        String quality = database.getValue(SettingsActivity.SETTINGSPREVIEWQUALITY);
        if (quality == null || quality.isEmpty()) database.setValue(SettingsActivity.SETTINGSPREVIEWQUALITY, quality = "preview_url");

        Logger.getLogger("a621").info("Requesting page "+ baseURL +"post/index.xml?tags="+escapedQuery+"&page="+ page + "&limit="+pagesize);

        swipeLayout.setRefreshing(true);
        final int _page = page;
        final String _query = escapedQuery;
        final String _quality = quality;

        (new XMLTask(this) {
            @Override
            protected void onPostExecute(XMLNode result) {

                swipeLayout.setRefreshing(false);
                if (result == null) { quickToast("Error while request / parsing!"); return; }
                if (result.getChildCount() <= 0) { quickToast("No results found..."); return; }

                results = new PostListAdapter(getApplicationContext(), R.id.txtMid, blacklist.proxyBlacklist(result.children()), _quality);
                results.svNumPosts = (result.attributes().contains("count") ? Integer.valueOf(result.getAttribute("count")) : 0);
                PostsActivity.this.query = _query; PostsActivity.this.page = _page;

                ActionBar ab = getSupportActionBar();
                ab.setTitle(getResources().getString(R.string.title_posts) + " | " + _page);
                ab.setSubtitle(URLDecoder.decode(_query));
                //setTitle(getResources().getString(R.string.title_posts) + " " + _page + (_query.equals("")?" | *":" | "+XMLUtils.unescapeXML(_query)));

                pageFirst.setVisibility(_page>1?View.VISIBLE:View.GONE);
                pageLast.setVisibility(_page>2?View.VISIBLE:View.GONE);
                pageNext.setVisibility(_page<results.getLastPage(pagesize)?View.VISIBLE:View.GONE);
                lstPosts.setAdapter(results);
                lstPosts.refreshDrawableState();

            }
        }).execute( baseURL +"post/index.xml?tags="+escapedQuery+"&page="+ page + "&limit="+pagesize);
    }

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
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
                    MenuItem searchMenuItem = PostsActivity.this.searchMenuItem;
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bnNext:
                Logger.getLogger("a621").info("next " + (page+1));
                if ((page-1)*pagesize + results.getResultCount() < results.svNumPosts)
                    searchPosts(query, page+1);
                break;
            case R.id.bnLast:
                Logger.getLogger("a621").info("last " + (page-1));
                if (page > 2)
                    searchPosts(query, page-1);
                break;
            case R.id.bnFirst:
                Logger.getLogger("a621").info("jump 1");
                if (page > 1)
                    searchPosts(query, 1);
                break;
        }
    }
}
