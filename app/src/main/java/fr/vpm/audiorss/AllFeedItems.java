package fr.vpm.audiorss;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.persistence.AsyncMaintenance;
import fr.vpm.audiorss.process.AllFeedItemsDataModel;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.process.FeedChoiceModeListener;
import fr.vpm.audiorss.process.NavigationDrawerList;
import fr.vpm.audiorss.process.NavigationDrawerProvider;
import fr.vpm.audiorss.process.RSSItemArrayAdapter;
import fr.vpm.audiorss.rss.RSSChannel;

public class AllFeedItems extends Activity implements FeedsActivity<RSSItemArrayAdapter> {

  public static final String CHANNEL_ID = "channelId";
  public static final String DISP_GRID = "disp_grid";
  public static final String DISP_LIST = "disp_list";
  private static final int REQ_PREFS = 2;

  private AbsListView mFeedItems;

  private NetworkChecker networkChecker;

  private DataModel dataModel;
  private ListView drawerList;

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

    Long[] channelIds = new Long[0];
    Intent i = getIntent();
    if (i.hasExtra(CHANNEL_ID)) {
      long[] chIds = i.getExtras().getLongArray(CHANNEL_ID);
      int j = 0;
      channelIds = new Long[chIds.length];
      for (long chId : chIds){
        channelIds[j++] = chId;
      }
    }

    // services
    networkChecker = new DefaultNetworkChecker();
    dataModel = new AllFeedItemsDataModel(this, progressBarListener, this, channelIds, rss_item_layout);

    mFeedItems = (AbsListView) findViewById(R.id.allitems);
    mFeedItems.setTextFilterEnabled(true);
    mFeedItems.setOnItemClickListener(dataModel.getOnItemClickListener());

    // Navigation drawer
    final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawerList = (ListView) findViewById(R.id.left_drawer);
    drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        List<SelectionFilter> filters = new ArrayList<SelectionFilter>();
        filters.add(((NavigationDrawerList.NavigationDrawerItem) drawerList.getAdapter().getItem(position)).getFilter());
        dataModel.filterData(filters);
        drawerLayout.closeDrawer(drawerList);
      }
    });

    setContextualListeners();
    dataModel.loadData();

    if (i.hasExtra(FeedAddingActivity.CHANNEL_NEW_URL)) {
      dataModel.addData(i.getStringExtra(FeedAddingActivity.CHANNEL_NEW_URL));
    }

    // deletes old items at app startup
    new AsyncMaintenance(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Media.getInternalItemsPicsFolder(), Media.getInternalFeedsPicsFolder());
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
  public void refreshView(RSSItemArrayAdapter rssItemAdapter, NavigationDrawerProvider navigationDrawer) {
    mFeedItems.setAdapter(rssItemAdapter);
    drawerList.setAdapter(navigationDrawer.getAdapter(R.layout.drawer_item));
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
    switch (item.getItemId()) {
      case R.id.action_search:
        i = new Intent(AllFeedItems.this, SearchFeedActivity.class);
        startActivity(i);
        result = true;
        break;
      case R.id.action_add:
        dataModel.addData(null);
        result = true;
        break;
      case R.id.action_refresh:
        if (networkChecker.checkNetworkForRefresh(this, true)) {
          dataModel.refreshData();
        }
        result = true;
        break;
      case R.id.action_settings:
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
      dataModel.loadData();
    } else if (REQ_PREFS == requestCode){
      dataModel.refreshView();
    }
  }
}
