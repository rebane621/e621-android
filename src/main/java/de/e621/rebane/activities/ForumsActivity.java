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

import de.e621.rebane.MiscStatics;
import de.e621.rebane.a621.R;
import de.e621.rebane.components.ForumListAdapter;
import de.e621.rebane.components.PostListAdapter;
import de.e621.rebane.components.WebImageView;
import de.e621.rebane.xmlreader.XMLNode;
import de.e621.rebane.xmlreader.XMLTask;

public class ForumsActivity extends DrawerWrapper implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    ListView lstTopics;
    ForumListAdapter results = null;
    LinearLayout pagebar;
    ImageView pageNext, pageLast, pageFirst;
    int page=1;
    String query = "";
    SwipeRefreshLayout swipeLayout;
    Menu menu = null;
    static int pagesize = 30; //given by API

    final static String FORUMQUERYPAGE = "a621 SearchManager Forum Page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forums);
        onCreateDrawer();   //DrawerWrapper function that requires the layout to be set to prepare the drawer
        DrawerWrapper.openActivity = this.getClass();   //block the drawer from reopening this activity while opened

        Logger.getLogger("a621").info("Activity created");

        lstTopics =      (ListView)              findViewById(R.id.lstPostResults);
        swipeLayout =   (SwipeRefreshLayout)    findViewById(R.id.swipe_container);
        pagebar =       (LinearLayout)          findViewById(R.id.pagebar);

        (pageNext =     (ImageView)                findViewById(R.id.bnNext)).setOnClickListener(this);
        (pageLast =     (ImageView)                findViewById(R.id.bnLast)).setOnClickListener(this);
        (pageFirst =    (ImageView)                findViewById(R.id.bnFirst)).setOnClickListener(this);

        lstTopics.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView lv, int i, int i1, int i2) {
                if (lstTopics == null || pagebar == null || results == null) return;

                //boolean canNext = results.canPaginateNext(page, pagesize), canLast = results.canPaginateLast(page);
                //int lastPage = results.getLastPage(pagesize);

                if (lv.getId() == R.id.lstPostResults) {
                    if ((lstTopics.getFirstVisiblePosition()==0 || lstTopics.getLastVisiblePosition()>=results.getResultCount()-1) /*&& (lastPage>1)*/) {
                        if (pagebar.getVisibility()== View.GONE) {
                            Animation bottomUp = AnimationUtils.loadAnimation(ForumsActivity.this, R.anim.bottom_up);
                            pagebar.startAnimation(bottomUp);
                            pagebar.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (pagebar.getVisibility()==View.VISIBLE) {
                            Animation bottomDown = AnimationUtils.loadAnimation(ForumsActivity.this, R.anim.bottom_down);
                            pagebar.startAnimation(bottomDown);
                            pagebar.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        handleIntent(getIntent());
    }

    @Override public void onRefresh() {
        WebImageView.clear();   //in case a stream was interrupted, so it's propperly downloading
        searchForums(page);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.getLogger("a621").info("Activity resumed");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        outState.putInt(FORUMQUERYPAGE, page);
    }

    @Override
    void handleIntent(Intent intent) {
        super.handleIntent(intent);
        page = intent.getIntExtra(FORUMQUERYPAGE,1);

        if (results==null) searchForums(page);
    }

    void searchForums(int page) {
        if (!MiscStatics.canRequest(this) ||
                swipeLayout.isRefreshing()) //for some reason it skips from page 4 to 2 otherwise
            return;

        openDB();
        baseURL = database.getValue(SettingsActivity.SETTINGBASEURL);
        if (baseURL == null || baseURL.isEmpty() || !baseURL.startsWith("https")) database.setValue(SettingsActivity.SETTINGBASEURL, baseURL = "https://e621.net/");

        Logger.getLogger("a621").info("Requesting page "+ baseURL +"forum/index.xml?&page="+ page);

        swipeLayout.setRefreshing(true);
        final int _page = page;

        (new XMLTask(this) {
            @Override
            protected void onPostExecute(XMLNode result) {

                swipeLayout.setRefreshing(false);
                if (result == null) { quickToast("Error while request / parsing!"); return; }
                if (result.getChildCount() <= 0) { quickToast("No results found..."); return; }

                results = new ForumListAdapter(getApplicationContext(), R.id.lblTitle, result.children());
                //results.svNumPosts = (result.attributes().contains("count") ? Integer.valueOf(result.getAttribute("count")) : 0); //this value is not provided
                ForumsActivity.this.page = _page;

                ActionBar ab = getSupportActionBar();
                ab.setTitle(getResources().getString(R.string.title_forums) + " | " + _page);
                //setTitle(getResources().getString(R.string.title_posts) + " " + _page + (_query.equals("")?" | *":" | "+XMLUtils.unescapeXML(_query)));

                pageFirst.setVisibility(_page>1?View.VISIBLE:View.GONE);
                pageLast.setVisibility(_page>2?View.VISIBLE:View.GONE);
                pageNext.setVisibility(View.VISIBLE); //(_page<results.getLastPage(pagesize)?View.VISIBLE:View.GONE);
                lstTopics.setAdapter(results);
                lstTopics.refreshDrawableState();

            }
        }).execute( baseURL +"forum/index.xml?page="+ _page);
    }

    MenuItem searchMenuItem = null;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.generic, menu);
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
                //if ((page-1)*pagesize + results.getResultCount() < results.svNumPosts)
                    searchForums(page+1);
                //break;
            case R.id.bnLast:
                //Logger.getLogger("a621").info("last " + (page-1));
                if (page > 2)
                    searchForums(page-1);
                break;
            case R.id.bnFirst:
                //Logger.getLogger("a621").info("jump 1");
                if (page > 1)
                    searchForums(1);
                break;
        }
    }
}
