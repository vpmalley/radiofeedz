package fr.vpm.audiorss.process;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import fr.vpm.audiorss.FeedItemReader;
import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.R;
import fr.vpm.audiorss.db.AsyncDbReadRSSChannel;
import fr.vpm.audiorss.db.AsyncDbSaveRSSItem;
import fr.vpm.audiorss.db.RefreshViewCallback;
import fr.vpm.audiorss.http.AsyncFeedRefresh;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.SaveFeedCallback;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 09/11/14.
 */
public class AllFeedItemsDataModel implements DataModel.RSSChannelDataModel, DataModel.RSSItemDataModel {

  private static final String PREF_FEED_ORDERING = "pref_feed_ordering";

  private static final String PREF_DISP_MAX_ITEMS = "pref_disp_max_items";

  private static final int DEFAULT_MAX_ITEMS = 80;

  private Map<RSSItem, RSSChannel> channelsByItem;

  /**
   * the items of feeds that are displayed
   */
  private List<RSSItem> items;

  private List<RSSChannel> feeds;

  private final ProgressListener progressListener;

  private final Activity activity;

  private final FeedsActivity<RSSItemArrayAdapter> feedsActivity;

  public AllFeedItemsDataModel(Activity activity, ProgressListener progressListener, FeedsActivity<RSSItemArrayAdapter>
      feedsActivity) {
    this.progressListener = progressListener;
    this.feedsActivity = feedsActivity;
    this.activity = activity;
  }

  @Override
  public void loadData() {
    RefreshViewCallback callback =
        new RefreshViewCallback(progressListener, this);
    AsyncDbReadRSSChannel asyncDbReader = new AsyncDbReadRSSChannel(callback, getContext());
    // read all RSSChannel items from DB and refresh views
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @Override
  public void setChannelsAndBuildModel(List<RSSChannel> channels) {
    this.feeds = channels;
    SharedPreferences sharedPref = PreferenceManager
        .getDefaultSharedPreferences(getContext());
    String ordering = sharedPref.getString(PREF_FEED_ORDERING, "REVERSE_TIME");
    SortedSet<RSSItem> allItems = new TreeSet<RSSItem>(new ItemComparator(ordering));

    channelsByItem = new HashMap<RSSItem, RSSChannel>();
    for (RSSChannel channel : channels) {
      allItems.addAll(channel.getItems());
      for (RSSItem item : channel.getItems()) {
        channelsByItem.put(item, channel);
      }
    }

    int itemNumbers = getNbDisplayedItems(sharedPref, allItems);
    items = new ArrayList<RSSItem>(allItems).subList(0, itemNumbers);
  }

  @Override
  public void setItemsAndBuildModel(List<RSSItem> data) {
    items = data;

    feeds = new ArrayList<RSSChannel>();
    channelsByItem = new HashMap<RSSItem, RSSChannel>();
    for (RSSItem item : items) {
      if (!channelsByItem.containsKey(item)){
        RSSChannel rssChannel = RSSChannel.fromDbById(item.getChannelId(), getContext());
        channelsByItem.put(item, rssChannel);
        feeds.add(rssChannel);
      }
    }

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

  @Override
  public synchronized void refreshView() {
    RSSItemArrayAdapter rssItemAdapter = new RSSItemArrayAdapter(activity,
        R.layout.list_rss_item, items, channelsByItem);
    feedsActivity.refreshView(rssItemAdapter);
  }


  @Override
  public Context getContext() {
    return activity;
  }

  @Override
  public void refreshData(){
    SaveFeedCallback.updateFeedsCounter(feeds.size());
    for (RSSChannel feed : feeds) {
      SaveFeedCallback callback = new SaveFeedCallback(progressListener, this);
      new AsyncFeedRefresh(getContext(), callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
          feed.getUrl());
    }
  }

  @Override
  public void addData() {
    FeedAdder feedAdder = new FeedAdder(this, new DefaultNetworkChecker(), progressListener);
    String feedUrl = feedAdder.retrieveFeedFromClipboard();
    if (feedUrl != null) {
      feedAdder.askForFeedValidation(feeds, feedUrl);
    } else {
      feedAdder.tellToCopy();
    }
  }

  @Override
  public AdapterView.OnItemClickListener getOnItemClickListener() {
    return new OnRSSItemClickListener();
  }

  @Override
  public void deleteData(Collection<Integer> selection) {
    RSSItem[] itemsToDelete = new RSSItem[selection.size()];
    int i = 0;
    for (int position : selection){
      items.get(position).setDeleted(true);
      itemsToDelete[i++] = items.get(position);
    }
    AsyncCallbackListener<List<RSSItem>> callback = new AsyncCallbackListener<List<RSSItem>>() {
      @Override
      public void onPreExecute() {
        progressListener.startRefreshProgress();
      }

      @Override
      public void onPostExecute(List<RSSItem> result) {
        progressListener.stopRefreshProgress();
        items.removeAll(result);
        refreshView();
      }
    };
    new AsyncDbSaveRSSItem(callback,
        getContext()).executeOnExecutor(AsyncTask
        .THREAD_POOL_EXECUTOR, itemsToDelete);
  }

  @Override
  public void markDataRead(Set<Integer> selection, boolean isRead) {
    for (int position : selection){
      items.get(position).setRead(isRead);
    }
  }

  public class OnRSSItemClickListener implements AdapterView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
      Intent i = new Intent(getContext(), FeedItemReader.class);
      RSSItem rssItem = items.get(position);
      rssItem.setRead(true);
      new AsyncDbSaveRSSItem(new AsyncCallbackListener<List<RSSItem>>() {
        @Override
        public void onPreExecute() {
          // do nothing
        }

        @Override
        public void onPostExecute(List<RSSItem> result) {
          loadData();
        }
      }, getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
          rssItem);
      //RSSChannel channel = RSSChannel.fromDbById(rssItem.getChannelId(), AllFeedItems.this);
      RSSChannel channel = channelsByItem.get(rssItem);
      i.putExtra(FeedItemReader.ITEM, rssItem);
      i.putExtra(FeedItemReader.CHANNEL, channel);
      getContext().startActivity(i);
    }

  }

}
