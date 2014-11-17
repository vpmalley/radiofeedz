package fr.vpm.audiorss;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
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

  private ListView mFeedItems;

  private NetworkChecker networkChecker;

  private DataModel dataModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feeds);

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
    dataModel = new AllFeedItemsDataModel(this, progressBarListener, this, channelIds);

    mFeedItems = (ListView) findViewById(R.id.list);
    mFeedItems.setTextFilterEnabled(true);
    mFeedItems.setOnItemClickListener(dataModel.getOnItemClickListener());

    setContextualListeners();
    dataModel.loadData();
  }

  /**
   * Defines the listener when long clicking on one or multiple items of the list
   */
  private void setContextualListeners() {
    mFeedItems.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
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
        if (networkChecker.checkNetwork(this, true)) {
          dataModel.refreshData();
        }
        result = true;
        break;
      case R.id.action_settings:
        i = new Intent(AllFeedItems.this, PreferencesActivity.class);
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
}
