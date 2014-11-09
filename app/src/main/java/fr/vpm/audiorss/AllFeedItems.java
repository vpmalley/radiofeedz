package fr.vpm.audiorss;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import fr.vpm.audiorss.db.AsyncDbReadRSSChannel;
import fr.vpm.audiorss.db.AsyncDbSaveRSSItem;
import fr.vpm.audiorss.db.RefreshViewCallback;
import fr.vpm.audiorss.http.AsyncFeedRefresh;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.http.SaveFeedCallback;
import fr.vpm.audiorss.media.PictureLoadedListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.FeedAdder;
import fr.vpm.audiorss.process.ItemComparator;
import fr.vpm.audiorss.process.RSSItemArrayAdapter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class AllFeedItems extends Activity implements FeedsActivity<List<RSSChannel>>,PictureLoadedListener {

  private static final String PREF_FEED_ORDERING = "pref_feed_ordering";

  private static final String PREF_DISP_MAX_ITEMS = "pref_disp_max_items";

  public static final String E_RETRIEVING_FEEDS = "Issue retrieving the feeds. Please retry.";

  public static final String E_ADDING_FEED = "Issue adding the feed. Please retry.";

  public static final String E_DUPLICATE_FEED = "This feed already exists in your list.";

  private static final int DEFAULT_MAX_ITEMS = 80;

  private List<RSSChannel> channels = new ArrayList<RSSChannel>();

  private Map<RSSItem, RSSChannel> channelsByItem;

  private ListView mFeedItems;

  /**
   * Progress bar manager to indicate feeds update is in progress.
   */
  private ProgressBarListener progressBarListener;

  /**
   * the items of feeds that are displayed
   */
  private List<RSSItem> items;

  private NetworkChecker networkChecker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feeds);

    // views
    mFeedItems = (ListView) findViewById(R.id.list);
    mFeedItems.setTextFilterEnabled(true);
    mFeedItems.setOnItemClickListener(new OnRSSItemClickListener());

    progressBarListener = new ProgressBarListener((ProgressBar) findViewById(R.id.refreshprogress));

    // services
    networkChecker = new DefaultNetworkChecker();
    loadDataAndRefreshView();
  }

  @Override
  public void loadDataAndRefreshView() {
    RefreshViewCallback callback = new RefreshViewCallback(progressBarListener, this);
    AsyncDbReadRSSChannel asyncDbReader = new AsyncDbReadRSSChannel(callback, this);
    // read all RSSChannel items from DB and refresh views
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @Override
  public void setData(List<RSSChannel> channels) {
    this.channels = channels;
  }

  public synchronized void refreshView() {
    Log.d("FeedsActivity", "refreshing view");
    SharedPreferences sharedPref = PreferenceManager
        .getDefaultSharedPreferences(AllFeedItems.this);
    String ordering = sharedPref.getString(PREF_FEED_ORDERING, "REVERSE_TIME");
    SortedSet<RSSItem> allItems = new TreeSet<RSSItem>(new ItemComparator(ordering));

    Log.d("measures", "build model start " + System.currentTimeMillis());
    channelsByItem = new HashMap<RSSItem, RSSChannel>();
    for (RSSChannel channel : channels) {
      allItems.addAll(channel.getItems());
      for (RSSItem item : channel.getItems()) {
        channelsByItem.put(item, channel);
      }
    }

    int itemNumbers = getNbDisplayedItems(sharedPref, allItems);
    items = new ArrayList<RSSItem>(allItems).subList(0, itemNumbers);
    ArrayAdapter<RSSItem> rssItemAdapter = new RSSItemArrayAdapter(this,
        R.layout.list_rss_item, items, channelsByItem);
    Log.d("measures", "build model -end- " + System.currentTimeMillis());

    mFeedItems.setAdapter(rssItemAdapter);

  }

  @Override
  public void onPictureLoaded(Bitmap pictureBitmap) {
    refreshView();
  }

  @Override
  public Context getContext() {
    return this;
  }

  /**
   * Using the number of items in the set and the maximum of items displayed in the preferences, determines the maximum of items to display.
   *
   * @param allItems the set of available items
   * @return the number of items to display
   */
  private int getNbDisplayedItems(SharedPreferences sharedPref, SortedSet<RSSItem> allItems) {
    // preparing the number of items to display
    int maxItems = Integer.valueOf(sharedPref.getString(PREF_DISP_MAX_ITEMS, String.valueOf(DEFAULT_MAX_ITEMS)));
    return Math.min(allItems.size(), maxItems);
  }

  /**
   * Displays error, as a Toast
   *
   * @param error
   */
  public void displayError(String error) {
    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
  }

  // launch when click on refresh button
  private void launchFeedRefresh() {
    Log.d("FeedsActivity", "launching feed refresh");
    SaveFeedCallback.updateFeedsCounter(channels.size());
    for (RSSChannel channel : channels) {
      SaveFeedCallback callback = new SaveFeedCallback(progressBarListener, AllFeedItems.this);
      new AsyncFeedRefresh(AllFeedItems.this, callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
          channel.getUrl());
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.allfeedsitem, menu);
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
        FeedAdder feedAdder = new FeedAdder(this, networkChecker, progressBarListener);
        String feedUrl = feedAdder.retrieveFeedFromClipboard();
        if (feedUrl != null) {
          feedAdder.askForFeedValidation(channels, feedUrl);
        } else {
          feedAdder.tellToCopy();
        }
        result = true;
        break;
      case R.id.action_refresh:
        if (networkChecker.checkNetwork(this)) {
          launchFeedRefresh();
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

  class OnRSSItemClickListener implements OnItemClickListener {

      @Override
      public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
      Intent i = new Intent(AllFeedItems.this, FeedItemReader.class);
      items.get(position).setRead(true);
      new AsyncDbSaveRSSItem(new AsyncCallbackListener<List<RSSItem>>() {
        @Override
        public void onPreExecute() {
          // do nothing
        }

        @Override
        public void onPostExecute(List<RSSItem> result) {
          // do nothing
        }
      }, AllFeedItems.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
          items.get(position));
      i.putExtra(FeedItemReader.ITEM, items.get(position));
      i.putExtra(FeedItemReader.CHANNEL, channelsByItem.get(items.get(position)));
      startActivity(i);

    }

    }
}
