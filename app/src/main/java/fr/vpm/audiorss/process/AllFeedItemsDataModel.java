package fr.vpm.audiorss.process;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.vpm.audiorss.FeedItemReader;
import fr.vpm.audiorss.FeedItemReaderActivity;
import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.db.AsyncDbReadRSSChannel;
import fr.vpm.audiorss.db.AsyncDbReadRSSItems;
import fr.vpm.audiorss.db.AsyncDbSaveRSSItem;
import fr.vpm.audiorss.db.ChannelRefreshViewCallback;
import fr.vpm.audiorss.db.ItemRefreshViewCallback;
import fr.vpm.audiorss.db.LoadDataRefreshViewCallback;
import fr.vpm.audiorss.db.filter.ArchivedFilter;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.db.filter.UnArchivedFilter;
import fr.vpm.audiorss.http.AsyncFeedRefresh;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.SaveFeedCallback;
import fr.vpm.audiorss.media.MediaDownloadListener;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 09/11/14.
 */
public class AllFeedItemsDataModel implements DataModel.RSSChannelDataModel, DataModel.RSSItemDataModel {

  public static final int REQ_ITEM_READ = 1;

  private static final String PREF_FEED_ORDERING = "pref_feed_ordering";

  private static final String PREF_DISP_MAX_ITEMS = "pref_disp_max_items";

  private static final int DEFAULT_MAX_ITEMS = 80;

  private Map<RSSItem, RSSChannel> channelsByItem = new HashMap<RSSItem, RSSChannel>();

  /**
   * the items of feeds that are displayed
   */
  private List<RSSItem> items = new ArrayList<RSSItem>();

  private List<RSSChannel> feeds = new ArrayList<RSSChannel>();

  private final ProgressListener progressListener;

  private final Activity activity;

  private final FeedsActivity<RSSItemArrayAdapter> feedsActivity;

  private RSSItemArrayAdapter rssItemAdapter;

  private ArrayList<SelectionFilter> itemFilters = new ArrayList<SelectionFilter>();

  private List<SelectionFilter> channelFilters = new ArrayList<SelectionFilter>();

  private int resource;

  /**
   * Whether the view should be recreated when refreshed
   */
  private boolean recreate = false;
  private NavigationDrawerList navigationDrawerList;
  private int savingFeeds = 0;

  public AllFeedItemsDataModel(Activity activity, ProgressListener progressListener, FeedsActivity<RSSItemArrayAdapter>
          feedsActivity, int resId) {
    this.progressListener = progressListener;
    this.feedsActivity = feedsActivity;
    this.activity = activity;
    this.resource = resId;
    this.itemFilters.add(new UnArchivedFilter());
  }

  @Override
  public void loadData() {
    if (savingFeeds > 1) {
      savingFeeds--;
    } else {
      loadDataFromChannels(false);
      loadDataFromItems();
    }
  }

  public void loadDataFromChannels(boolean readItems) {
    ChannelRefreshViewCallback callback =
        new ChannelRefreshViewCallback(progressListener, this);
    AsyncDbReadRSSChannel asyncDbReader = new AsyncDbReadRSSChannel(callback, getContext(), readItems);
    // read all RSSChannel items from DB and refresh views
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  public void loadDataFromItems() {
    AsyncCallbackListener<List<RSSItem>> callback = new ItemRefreshViewCallback(progressListener, this);
    AsyncDbReadRSSItems asyncDbReader = new AsyncDbReadRSSItems(callback, getContext(), itemFilters);
    // read all RSS items from DB and refresh views
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @Override
  public boolean isReady() {
    return !channelsByItem.isEmpty();
  }

  @Override
  public void setChannelsAndBuildModel(List<RSSChannel> channels) {
    if (channels.size() != this.feeds.size()){
      recreate = true;
    }
    this.feeds = channels;
    buildChannelsByItem();
  }

  @Override
  public void setItemsAndBuildModel(List<RSSItem> items) {
    if (items.size() != this.items.size()){
      recreate = true;
    }
    this.items = items;
    buildChannelsByItem();
  }

  private void buildChannelsByItem(){
    if ((items.size() > 0) && (feeds.size() > 0)) {
      channelsByItem.clear();
      for (RSSItem item : items) {
        for (RSSChannel channel : feeds) {
          if (channel.getId() == item.getChannelId()) {
            channelsByItem.put(item, channel);
          }
        }
      }
    }
  }

  @Override
  public synchronized void refreshView() {
    if (rssItemAdapter == null || recreate) {
      rssItemAdapter = new RSSItemArrayAdapter(activity, resource, items, channelsByItem);
      feedsActivity.refreshView(rssItemAdapter, getNavigationDrawer());
      recreate = false;
    } else {
      rssItemAdapter.setItems(items);
      rssItemAdapter.setChannelsByItem(channelsByItem);
      rssItemAdapter.notifyDataSetChanged();
    }
  }


  @Override
  public Context getContext() {
    return activity;
  }

  @Override
  public void refreshData(){
    for (RSSChannel feed : feeds) {
      SaveFeedCallback callback = new SaveFeedCallback(progressListener, this);
      new AsyncFeedRefresh(getContext(), callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
          feed.getUrl());
      savingFeeds++;
    }
  }

  @Override
  public void onFeedFailureBeforeLoad() {
    savingFeeds--;
  }

  @Override
  public void addData(String feedUrl) {
    FeedAdder feedAdder = new FeedAdder(this, new DefaultNetworkChecker(), progressListener);
    if (feedUrl == null) {
      feedUrl = feedAdder.retrieveFeedFromClipboard();
    }
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
      items.get(position).setArchived(true);
      itemsToDelete[i++] = items.get(position);
    }
    saveItems(itemsToDelete);
  }

  @Override
  public void markDataRead(Set<Integer> selection, boolean isRead) {
    RSSItem[] itemsToSave = new RSSItem[selection.size()];
    int i = 0;
    for (int position : selection){
      items.get(position).setRead(isRead);
      itemsToSave[i++] = items.get(position);
    }
    saveItems(itemsToSave);
  }

  @Override
  public void downloadMedia(Set<Integer> selection) {
    for (int position : selection){
      if (items.get(position).getMedia() != null){
        items.get(position).getMedia().download(getContext(), DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
                new MediaDownloadListener.DummyMediaDownloadListener());
      }
    }
  }

  private void saveItems(RSSItem... itemsToSave) {
    new AsyncDbSaveRSSItem(new LoadDataRefreshViewCallback<RSSItem>(progressListener, this),
            getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, itemsToSave);
  }

  @Override
  public Bundle getFeedItem(int position) {
    Bundle args = new Bundle();
    if (position < items.size()) {
      RSSItem rssItem = items.get(position);
      args.putParcelable(FeedItemReader.ITEM, rssItem);
      args.putParcelable(FeedItemReader.CHANNEL, channelsByItem.get(rssItem));
    }
    return args;
  }

  @Override
  public void filterData(List<SelectionFilter> filters) {
    this.itemFilters.clear();
    boolean needsUnarchivedFilter = true;
    for (SelectionFilter filter : filters){
      this.itemFilters.add(filter);
      if (filter instanceof ArchivedFilter){
        needsUnarchivedFilter = false;
      }
    }
    if (needsUnarchivedFilter){
      this.itemFilters.add(new UnArchivedFilter());
    }
    loadData();
  }

  @Override
  public int size() {
    return items.size();
  }

  public NavigationDrawerProvider getNavigationDrawer() {
    if (navigationDrawerList == null) {
      navigationDrawerList = new NavigationDrawerList(getContext(), this, progressListener);
    }
    navigationDrawerList.clear();
    navigationDrawerList.addStaticItems();
    navigationDrawerList.addChannels(feeds);
    return navigationDrawerList;
  }

  public class OnRSSItemClickListener implements AdapterView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
      Intent i = new Intent(getContext(), FeedItemReaderActivity.class);
      i.putExtra(FeedItemReaderActivity.INITIAL_POSITION, position);
      i.putParcelableArrayListExtra(FeedItemReaderActivity.ITEM_FILTER, itemFilters);
      activity.startActivityForResult(i, REQ_ITEM_READ);
    }

  }

}
