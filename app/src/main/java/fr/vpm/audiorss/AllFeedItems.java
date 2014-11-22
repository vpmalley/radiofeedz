package fr.vpm.audiorss;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.process.AllFeedItemsDataModel;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.process.FeedChoiceModeListener;
import fr.vpm.audiorss.process.RSSItemArrayAdapter;
import fr.vpm.audiorss.rss.RSSChannel;

public class AllFeedItems extends Activity implements FeedsActivity<RSSItemArrayAdapter> {

  public static final String CHANNEL_ID = "channelId";
  public static final String DISP_GRID = "disp_grid";
  public static final String DISP_LIST = "disp_list";

  private AbsListView mFeedItems;

  private NetworkChecker networkChecker;

  private DataModel dataModel;

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

    setContextualListeners();
    dataModel.loadData();

    if (i.hasExtra(FeedAddingActivity.CHANNEL_NEW_URL)) {
      dataModel.addData(i.getStringExtra(FeedAddingActivity.CHANNEL_NEW_URL));
    }

  }

  /**
   * Defines the listener when long clicking on one or multiple items of the list
   */
  private void setContextualListeners() {
    mFeedItems.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
    FeedChoiceModeListener<RSSChannel> actionModeCallback = new FeedChoiceModeListener<RSSChannel>(dataModel, R.menu.items_context);
    mFeedItems.setMultiChoiceModeListener(actionModeCallback);
  }

  @Override
  public void refreshView(RSSItemArrayAdapter rssItemAdapter) {
    mFeedItems.setAdapter(rssItemAdapter);
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
        startActivity(i);
        result = true;
        break;
      case R.id.action_manage:
        i = new Intent(AllFeedItems.this, FeedsManager.class);
        startActivity(i);
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
    }
  }
}
