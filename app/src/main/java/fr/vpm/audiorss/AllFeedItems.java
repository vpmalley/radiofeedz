package fr.vpm.audiorss;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.presentation.FeedItemsInteraction;
import fr.vpm.audiorss.presentation.FeedItemsPresenter;
import fr.vpm.audiorss.process.FeedChoiceModeListener;
import fr.vpm.audiorss.process.NavigationDrawer;
import fr.vpm.audiorss.process.RSSItemArrayAdapter;
import fr.vpm.audiorss.process.RecurrentTaskManager;
import fr.vpm.audiorss.process.Stats;
import fr.vpm.audiorss.rss.RSSChannel;

public class AllFeedItems extends AppCompatActivity implements FeedsActivity<RSSItemArrayAdapter> {

  public static final String DISP_GRID = "disp_grid";
  public static final String DISP_LIST = "disp_list";
  public static final int REQ_ITEM_READ = 1;
  private static final int REQ_PREFS = 2;
  private static final int REQ_CATALOG = 3;
  private static final String FILTERS_KEY = "filters";

  private AbsListView mFeedItems;

  private NetworkChecker networkChecker;

  private NavigationDrawer navigationDrawerList;
  private ActionBarDrawerToggle drawerToggle;

  private String title = "Radiofeedz";
  private ArrayList<SelectionFilter> filters;
  private FeedItemsInteraction interactor;
  private ProgressBarListener progressBarListener;
  private TextView lastRefreshTimeView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // list layouts
    int activity_layout = R.layout.activity_feeds_list;
    int rss_item_layout = R.layout.list_rss_item;
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    if (DISP_GRID.equals(sharedPrefs.getString("pref_disp_items_layout", DISP_LIST))){
      // grid layouts
      activity_layout = R.layout.activity_feeds_grid;
      rss_item_layout = R.layout.grid_rss_item;
    }

    setContentView(activity_layout);

    progressBarListener = new ProgressBarListener((ProgressBar) findViewById(R.id.refreshprogress));

    // services
    networkChecker = new DefaultNetworkChecker();
    interactor = new FeedItemsPresenter(this, rss_item_layout);

    mFeedItems = (AbsListView) findViewById(R.id.allitems);
    mFeedItems.setTextFilterEnabled(true);
    setEmptyView();
    lastRefreshTimeView = (TextView)findViewById(R.id.latestupdate);

    // Navigation drawer
    setNavigationDrawer();

    // restoring data if possible
    if (savedInstanceState != null) {
      filters = savedInstanceState.getParcelableArrayList(FILTERS_KEY);
    }
    filterData();

    Intent i = getIntent();
    if (i.hasExtra(FeedAddingActivity.CHANNEL_NEW_URL)) {
      progressBarListener.startRefreshProgress();
      interactor.addFeed(i.getStringExtra(FeedAddingActivity.CHANNEL_NEW_URL));
    }

    setupSnackBarWithClipboardContent();

    new RecurrentTaskManager().performRecurrentTasks(this);
  }

  private void setupSnackBarWithClipboardContent() {
    final String feedUrl = retrieveFeedUrlFromClipboard();
    if (feedUrl != null) {
      Snackbar
          .make(mFeedItems, R.string.ask_add_feed + feedUrl, Snackbar.LENGTH_LONG)
          .setAction(R.string.action_add, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              interactor.addFeed(feedUrl);
            }
          }).show();
    }
  }

  public String retrieveFeedUrlFromClipboard() {
    String resultUrl = null;
    ClipData data = ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).getPrimaryClip();
    if (data != null) {
      ClipData.Item cbItem = data.getItemAt(0);
      if (cbItem != null) {
        String url = cbItem.getText().toString();
        if ((url != null) && (url.startsWith("http"))) {
          resultUrl = url;
        }
      }
    }
    return resultUrl;
  }

  private void filterData() {
    if (filters == null) {
      filters = new ArrayList<>();
    }
    Log.d("prerefresh", "filtering");
    progressBarListener.startRefreshProgress();
    interactor.loadFeedItems(filters);
  }

  private void setEmptyView() {
    LinearLayout emptyView = (LinearLayout) findViewById(R.id.emptyView);
    mFeedItems.setEmptyView(emptyView);

    ImageView emptyImageView = (ImageView) findViewById(R.id.emptyImageView);
    emptyImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Stats.get(AllFeedItems.this).increment(Stats.ACTION_CATALOG);
        Intent i = new Intent(AllFeedItems.this, CatalogActivity.class);
        startActivityForResult(i, REQ_CATALOG);
      }
    });

  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    drawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    drawerToggle.onConfigurationChanged(newConfig);
  }

  /**
   * Sets the navigation drawer and related elements
   */
  private void setNavigationDrawer() {
    // the navigation drawer
    final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    ListView feedsList = (ListView) findViewById(R.id.left_drawer);
    feedsList.setOnItemClickListener(new NavigationDrawerClickListener(drawerLayout, feedsList));

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    drawerToggle = new ActionBarDrawerToggle(
        this,
        drawerLayout,
        R.string.drawer_open,
        R.string.drawer_close
    ) {
      public void onDrawerClosed(View view) {
        getSupportActionBar().setTitle(title);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }

      public void onDrawerOpened(View drawerView) {
        getSupportActionBar().setTitle(R.string.drawer_opened_title);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }
    };
    navigationDrawerList = new NavigationDrawer(this, interactor);
    drawerLayout.setDrawerListener(drawerToggle);
    setFeedsContextualListener(feedsList);
  }

  @Override
  public void refreshNavigationDrawer(List<RSSChannel> allChannels) {
    ArrayAdapter<NavigationDrawer.NavigationDrawerItem> adapter = navigationDrawerList.setChannelsAndGetAdapter(allChannels);
    ListView feedsList = (ListView) findViewById(R.id.left_drawer);
    feedsList.setAdapter(adapter);
    setFeedsContextualListener(feedsList);
    progressBarListener.stopRefreshProgress();
  }

  public void setFeedsContextualListener(ListView feedsList) {
    feedsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
    FeedChoiceModeListener drawerModeCallback = new FeedChoiceModeListener(navigationDrawerList, R.menu.feeds_context);
    feedsList.setMultiChoiceModeListener(drawerModeCallback);
  }

  @Override
  public void refreshFeedItems(RSSItemArrayAdapter rssItemAdapter) {
    mFeedItems.setAdapter(rssItemAdapter);
  }

  @Override
  public void displayLastRefreshTime(String lastRefreshTime) {
    if (lastRefreshTimeView != null) {
      lastRefreshTimeView.setText(getString(R.string.last_refresh) + " : " + lastRefreshTime);
    }
  }

  @Override
  public Context getContext() {
    return this;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.allfeedsitem, menu);
    MenuItem searchItem = menu.findItem(R.id.action_search);
    searchItem.setVisible(false);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent i;
    boolean result = false;
    if (drawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    switch (item.getItemId()) {
      case R.id.action_search:
        Stats.get(this).increment(Stats.ACTION_SEARCH);
        i = new Intent(AllFeedItems.this, SearchFeedActivity.class);
        startActivity(i);
        result = true;
        break;
      case R.id.action_catalog:
        Stats.get(this).increment(Stats.ACTION_CATALOG);
        i = new Intent(AllFeedItems.this, CatalogActivity.class);
        startActivityForResult(i, REQ_CATALOG);
        result = true;
        break;
      case R.id.action_refresh:
        Stats.get(this).increment(Stats.ACTION_REFRESH);
        if (networkChecker.checkNetworkForRefresh(this, true)) {
          progressBarListener.startRefreshProgress();
          interactor.retrieveLatestFeedItems();
        }
        result = true;
        break;
      case R.id.action_settings:
        Stats.get(this).increment(Stats.ACTION_SETTINGS);
        i = new Intent(AllFeedItems.this, AllPreferencesActivity.class);
        startActivityForResult(i, REQ_PREFS);
        result = true;
        break;
      default:
        result = super.onOptionsItemSelected(item);
    }
    return result;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (REQ_ITEM_READ == requestCode){
      progressBarListener.startRefreshProgress();
      interactor.loadFeedItems();
    } else if (REQ_PREFS == requestCode){
      progressBarListener.startRefreshProgress();
      interactor.loadFeedItems();
    } else if (REQ_CATALOG == requestCode) {
      if (data != null) {
        progressBarListener.startRefreshProgress();
        String feedUrl = data.getStringExtra(CatalogActivity.FEED_URL_EXTRA);
        interactor.addFeed(feedUrl);
      }
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putParcelableArrayList(FILTERS_KEY, filters);
    super.onSaveInstanceState(outState);
  }

  private class NavigationDrawerClickListener implements AdapterView.OnItemClickListener {

    DrawerLayout drawerLayout;

    ListView drawerList;

    public NavigationDrawerClickListener(DrawerLayout drawerLayout, ListView drawerView) {
      this.drawerLayout = drawerLayout;
      this.drawerList = drawerView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      NavigationDrawer.NavigationDrawerItem navigationDrawerItem = (NavigationDrawer.NavigationDrawerItem) drawerList.getAdapter().getItem(position);
      Stats.get(AllFeedItems.this).increment(navigationDrawerItem.getStatTag());
      filters.clear();
      filters.add(navigationDrawerItem.getFilter());
      title = navigationDrawerItem.getTitle();
      progressBarListener.startRefreshProgress();
      interactor.loadFeedItems(filters);
      drawerLayout.closeDrawer(drawerList);
    }
  }
}
