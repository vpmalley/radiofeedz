package fr.vpm.audiorss;

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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.process.AllFeedItemsDataModel;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.process.FeedAdder;
import fr.vpm.audiorss.process.FeedChoiceModeListener;
import fr.vpm.audiorss.process.NavigationDrawerList;
import fr.vpm.audiorss.process.NavigationDrawerProvider;
import fr.vpm.audiorss.process.RSSItemArrayAdapter;
import fr.vpm.audiorss.process.RecurrentTaskManager;
import fr.vpm.audiorss.process.Stats;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class AllFeedItems extends AppCompatActivity implements FeedsActivity<RSSItemArrayAdapter> {

  public static final String CHANNEL_ID = "channelId";
  public static final String DISP_GRID = "disp_grid";
  public static final String DISP_LIST = "disp_list";
  private static final int REQ_PREFS = 2;
  private static final int REQ_CATALOG = 3;
  private static final String FILTERS_KEY = "filters";

  private AbsListView mFeedItems;

  private NetworkChecker networkChecker;

  private DataModel dataModel;
  private ListView drawerList;
  private ActionBarDrawerToggle drawerToggle;

  private String title = "Radiofeedz";
  private ArrayList<SelectionFilter> filters;

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

    ProgressBarListener progressBarListener = new ProgressBarListener((ProgressBar) findViewById(R.id.refreshprogress));

    // services
    networkChecker = new DefaultNetworkChecker();
    dataModel = new AllFeedItemsDataModel(this, progressBarListener, this, rss_item_layout);

    mFeedItems = (AbsListView) findViewById(R.id.allitems);
    mFeedItems.setTextFilterEnabled(true);
    mFeedItems.setOnItemClickListener(dataModel.getOnItemClickListener());
    setEmptyView();

    final LastRefreshListener lastRefreshListener = new LastRefreshListener((TextView)findViewById(R.id.latestupdate), dataModel, this);
    progressBarListener.setDelegate(lastRefreshListener);

    // Navigation drawer
    setNavigationDrawer();

    // restoring data if possible
    if (savedInstanceState != null) {
      filters = savedInstanceState.getParcelableArrayList(FILTERS_KEY);
    }
    filterData(lastRefreshListener);

    // Contextual actions
    setContextualListeners();

    Intent i = getIntent();
    if (i.hasExtra(FeedAddingActivity.CHANNEL_NEW_URL)) {
      dataModel.addData(i.getStringExtra(FeedAddingActivity.CHANNEL_NEW_URL));
    }

    setupSnackBarWithClipboardContent(progressBarListener);

    new RecurrentTaskManager().performRecurrentTasks(this);
  }

  private void setupSnackBarWithClipboardContent(ProgressBarListener progressBarListener) {
    final FeedAdder feedAdder = new FeedAdder(dataModel, new DefaultNetworkChecker(), progressBarListener);
    final String feedUrl = feedAdder.retrieveFeedFromClipboard();
    if (feedUrl != null) {
      Snackbar
          .make(mFeedItems, R.string.ask_add_feed + feedUrl, Snackbar.LENGTH_LONG)
          .setAction(R.string.action_add, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              dataModel.addData(feedUrl);
            }
          }).show();
    }
  }

  private void filterData(final LastRefreshListener lastRefreshListener) {
    if (filters == null) {
      filters = new ArrayList<>();
    }
    Log.d("prerefresh", "filtering");
    dataModel.filterData(filters, new AsyncCallbackListener.DummyCallback<List<RSSItem>>(),
        new AsyncCallbackListener<List<RSSChannel>>() {
          @Override
          public void onPreExecute() {}

          @Override
          public void onPostExecute(List<RSSChannel> result) {
            lastRefreshListener.stopRefreshProgress(); // updates the last synchro label
          }
        });
  }

  private void setEmptyView() {
    TextView emptyView = (TextView) findViewById(R.id.emptyView);
    emptyView.setText(getString(R.string.emptynews));
    mFeedItems.setEmptyView(emptyView);
  }

  /**
   * Sets the navigation drawer and related elements
   */
  private void setNavigationDrawer() {
    // the navigation drawer
    final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawerList = (ListView) findViewById(R.id.left_drawer);
    drawerList.setOnItemClickListener(new NavigationDrawerClickListener(drawerLayout));

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
    drawerLayout.setDrawerListener(drawerToggle);
  }

  /**
   * Defines the listener when long clicking on one or multiple items of the list
   */
  private void setContextualListeners() {
    mFeedItems.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
    FeedChoiceModeListener<RSSChannel> actionModeCallback = new FeedChoiceModeListener<RSSChannel>(dataModel, R.menu.items_context);
    mFeedItems.setMultiChoiceModeListener(actionModeCallback);
    drawerList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
    FeedChoiceModeListener<RSSChannel> drawerModeCallback = new FeedChoiceModeListener<RSSChannel>(dataModel.getNavigationDrawer(), R.menu.feeds_context);
    drawerList.setMultiChoiceModeListener(drawerModeCallback);
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

  @Override
  public void refreshView(RSSItemArrayAdapter rssItemAdapter, NavigationDrawerProvider navigationDrawer) {
    mFeedItems.setAdapter(rssItemAdapter);
    drawerList.setAdapter(navigationDrawer.getAdapter(R.layout.list_item));
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
          dataModel.refreshData();
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
    if (AllFeedItemsDataModel.REQ_ITEM_READ == requestCode){
      dataModel.loadData(new AsyncCallbackListener.DummyCallback<List<RSSItem>>(),
          new AsyncCallbackListener.DummyCallback<List<RSSChannel>>(), new AsyncCallbackListener.DummyCallback());
    } else if (REQ_PREFS == requestCode){
      dataModel.refreshView();
    } else if (REQ_CATALOG == requestCode) {
      if (data != null) {
        String feedUrl = data.getStringExtra(CatalogActivity.FEED_URL_EXTRA);
        dataModel.addData(feedUrl);
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

    public NavigationDrawerClickListener(DrawerLayout drawerLayout) {
      this.drawerLayout = drawerLayout;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      NavigationDrawerList.NavigationDrawerItem navigationDrawerItem = (NavigationDrawerList.NavigationDrawerItem) drawerList.getAdapter().getItem(position);
      Stats.get(AllFeedItems.this).increment(navigationDrawerItem.getStatTag());
      filters.clear();
      filters.add(navigationDrawerItem.getFilter());
      title = navigationDrawerItem.getTitle();
      dataModel.filterData(filters, new AsyncCallbackListener.DummyCallback<List<RSSItem>>(),
          new AsyncCallbackListener.DummyCallback<List<RSSChannel>>());
      drawerLayout.closeDrawer(drawerList);
    }
  }
}
