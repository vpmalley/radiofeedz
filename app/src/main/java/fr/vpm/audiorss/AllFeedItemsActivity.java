package fr.vpm.audiorss;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import fr.vpm.audiorss.http.AsyncFeedRefresh;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.process.FeedAdder;
import fr.vpm.audiorss.process.ItemComparator;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class AllFeedItemsActivity extends Activity implements ProgressListener {

  private static final String PREF_FEED_ORDERING = "pref_feed_ordering";

  private static final String PREF_DISP_MAX_ITEMS = "pref_disp_max_items";

  public static final String E_RETRIEVING_FEEDS = "Issue retrieving the feeds. Please retry.";

  public static final String E_ADDING_FEED = "Issue adding the feed. Please retry.";

  public static final String E_DUPLICATE_FEED = "This feed already exists in your list.";

  private static final int MAX_ITEMS = 80;

  private List<RSSChannel> channels = new ArrayList<RSSChannel>();

  ListView mFeeds;

  /**
   * Progress bar to indicate feeds update is in progress.
   */
  ProgressBar mRefreshProgress;

  /**
   * the items of feeds that are displayed
   */
  List<RSSItem> items;

  /**
   * The counter is used to join all threads for the refresh of multiple feeds.
   * The refresh of the view is done only once, when all feeds are up-to-date.
   */
  int refreshCounter = 0;

  NetworkChecker networkChecker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feeds);

    networkChecker = new DefaultNetworkChecker();

    mRefreshProgress = (ProgressBar) findViewById(R.id.refreshprogress);

    loadChannelsAndRefreshView();
  }

  public void loadChannelsAndRefreshView() {
    AsyncDbReadRSSChannel asyncDbReader = new AsyncDbReadRSSChannel(this);
    // read all RSSChannel items from DB and refresh views
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[0]);
  }

  public void setChannels(List<RSSChannel> channels) {
    this.channels = channels;
  }

  public void refreshView() {
    Log.d("measures", "counter" + refreshCounter);
    if (refreshCounter > 0) {
      refreshCounter--;
      return;
    }
    Log.d("FeedsActivity", "refreshing view");
    SharedPreferences sharedPref = PreferenceManager
        .getDefaultSharedPreferences(AllFeedItemsActivity.this);
    String ordering = sharedPref.getString(PREF_FEED_ORDERING, "reverse_time");
    SortedSet<RSSItem> allItems = new TreeSet<RSSItem>(new ItemComparator(ordering));

    final Map<RSSItem, RSSChannel> channelsByItem = new HashMap<RSSItem, RSSChannel>();
    for (RSSChannel channel : channels) {
      allItems.addAll(channel.getItems());
      for (RSSItem item : channel.getItems()) {
        channelsByItem.put(item, channel);
      }
    }

    int itemNumbers = getNbDisplayedItems(sharedPref, allItems);
    items = new ArrayList<RSSItem>(allItems).subList(0, itemNumbers);

    ArrayAdapter<RSSItem> itemAdapter = new ArrayAdapter<RSSItem>(this, R.layout.activity_item,
        items);
    // fill ListView with all the items
    if (mFeeds == null) {
      mFeeds = (ListView) findViewById(R.id.list);
      mFeeds.setTextFilterEnabled(true);
      mFeeds.setOnItemClickListener(new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
          Intent i = new Intent(AllFeedItemsActivity.this, FeedItemActivity.class);
          i.putExtra(FeedItemActivity.ITEM, items.get(position));
          i.putExtra(FeedItemActivity.CHANNEL, channelsByItem.get(items.get(position)));
          startActivity(i);

        }

      });
    }
    mFeeds.setAdapter(itemAdapter);

  }

  /**
   * Using the number of items in the set and the maximum of items displayed in the preferences, determines the maximum of items to display.
   *
   * @param allItems the set of available items
   * @return the number of items to display
   */
  private int getNbDisplayedItems(SharedPreferences sharedPref, SortedSet<RSSItem> allItems) {
    // preparing the number of items to display
    int maxItems = Integer.valueOf(sharedPref.getString(PREF_DISP_MAX_ITEMS, String.valueOf(MAX_ITEMS)));
    return Math.min(allItems.size(), maxItems);
  }

  public void startRefreshProgress() {
    mRefreshProgress.setVisibility(View.VISIBLE);
  }

  public void updateProgress(int progress) {
    mRefreshProgress.setProgress(progress);
  }

  public void stopRefreshProgress() {
    mRefreshProgress.setVisibility(View.GONE);
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
    refreshCounter = channels.size() - 1;
    for (RSSChannel channel : channels) {
      new AsyncFeedRefresh(AllFeedItemsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, channel.getUrl());
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.welcome, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent i;
    boolean result = false;
    switch (item.getItemId()) {
      case R.id.action_search:
        i = new Intent(AllFeedItemsActivity.this, SearchFeedActivity.class);
        startActivity(i);
        result = true;
        break;
      case R.id.action_add:
        FeedAdder feedAdder = new FeedAdder(this, networkChecker);
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
        i = new Intent(AllFeedItemsActivity.this, PreferencesActivity.class);
        startActivity(i);
        result = true;
        break;
      default:
        result = super.onOptionsItemSelected(item);
    }
    return result;
  }

}
