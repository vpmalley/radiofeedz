package fr.vpm.audiorss.process;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

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
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.media.MediaDownloadListener;
import fr.vpm.audiorss.media.Playlist;
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

  private List<ItemParser> dataToPostProcess = new CopyOnWriteArrayList<>();

  private final RSSCache cache;

  private final ProgressListener progressListener;

  private final Activity activity;

  private final FeedsActivity<RSSItemArrayAdapter> feedsActivity;

  private RSSItemArrayAdapter rssItemAdapter;

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
    this.cache = RSSCache.getInstance();
  }

  @Override
  public void loadData(AsyncCallbackListener<List<RSSItem>> itemsLoadedCallback, AsyncCallbackListener<List<RSSChannel>> channelsLoadedCallback, AsyncCallbackListener loadCallback) {
    Log.d("loadData", "decrease " + String.valueOf(savingFeeds));
    if (savingFeeds > 0) {
      savingFeeds--;
    }
    if (savingFeeds < 2) {
      loadDataFromChannels(false, channelsLoadedCallback);
      Log.d("loadData", "load items");
      loadDataFromItems(itemsLoadedCallback);
    } else {
      loadCallback.onPostExecute(null);
    }
  }

  public void loadDataFromChannels(boolean readItems, AsyncCallbackListener<List<RSSChannel>> channelsLoadedCallback) {
    ChannelRefreshViewCallback callback =
        new ChannelRefreshViewCallback(channelsLoadedCallback, this);
    AsyncDbReadRSSChannel asyncDbReader = new AsyncDbReadRSSChannel(callback, getContext(), readItems);
    // read all RSSChannel items from DB and refresh views
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  public void loadDataFromItems(AsyncCallbackListener<List<RSSItem>> itemsLoadedCallback) {
    Log.d("", "");
    AsyncCallbackListener<List<RSSItem>> callback = new ItemRefreshViewCallback(itemsLoadedCallback, this);
    AsyncDbReadRSSItems asyncDbReader = new AsyncDbReadRSSItems(callback, getContext(), cache.itemFilters);
    // read all RSS items from DB and refresh views
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @Override
  public boolean isReady() {
    return !cache.channelsByItem.isEmpty();
  }

  @Override
  public void setChannelsAndBuildModel(List<RSSChannel> channels) {
    if (channels.size() != this.cache.feeds.size()){
      recreate = true;
    }
    this.cache.feeds = channels;
    buildChannelsByItem();
  }

  @Override
  public void setItemsAndBuildModel(List<RSSItem> items) {
    if (items.size() != this.cache.items.size()){
      recreate = true;
    }
    this.cache.items = items;
    buildChannelsByItem();
  }

  private void buildChannelsByItem(){
    if ((cache.items.size() > 0) && (cache.feeds.size() > 0)) {
      cache.channelsByItem.clear();
      for (RSSItem item : cache.items) {
        for (RSSChannel channel : cache.feeds) {
          if (channel.getId() == item.getChannelId()) {
            cache.channelsByItem.put(item, channel);
          }
        }
      }
    }
    refreshView();
  }

  @Override
  public synchronized void refreshView() {
    if (rssItemAdapter == null || recreate) {
      rssItemAdapter = new RSSItemArrayAdapter(activity, resource, cache.items, cache.channelsByItem);
      feedsActivity.refreshView(rssItemAdapter);
      feedsActivity.refreshNavigationDrawer(getNavigationDrawer());
      recreate = false;
    } else {
      rssItemAdapter.setItems(cache.items);
      rssItemAdapter.setChannelsByItem(cache.channelsByItem);
      rssItemAdapter.notifyDataSetChanged();
    }
  }


  @Override
  public Context getContext() {
    return activity;
  }

  public boolean shouldForceRefresh() {
    Calendar fiveMinutesAgo = Calendar.getInstance();
    fiveMinutesAgo.add(Calendar.MINUTE, -5);
    Date lastBuildDate = DateUtils.parseDBDate(getLastBuildDate(), fiveMinutesAgo.getTime());
    return lastBuildDate.after(fiveMinutesAgo.getTime());
  }

  @Override
  public void preRefreshData() {
    if (new DefaultNetworkChecker().checkNetworkForRefresh(getContext(), false)) {
      Iterator<RSSChannel> feedsIterator = cache.feeds.iterator();
      RSSChannel nextChannel = null;
      Log.d("prerefresh", "feeds: " + cache.feeds.size());
      while (feedsIterator.hasNext() && !(nextChannel = feedsIterator.next()).shouldRefresh()) {
      }
      List<RSSChannel> channelsToRefresh = new ArrayList<>();
      channelsToRefresh.add(nextChannel);
      new CacheManager(channelsToRefresh, this, progressListener).updateCache(getContext());
    }
  }

  @Override
  public void refreshData(){
    boolean forceRefresh = shouldForceRefresh();
    CacheManager cm = new CacheManager(cache.feeds, this, progressListener);
    cm.updateCache(getContext());
  }

  @Override
  public void refreshData(Set<Integer> selection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void refreshData(List<RSSChannel> feedsToUpdate) {
    CacheManager cm = new CacheManager(feedsToUpdate, this, progressListener);
    cm.updateCache(getContext());
  }

  @Override
  public void onFeedFailureBeforeLoad() {
    loadData(new AsyncCallbackListener.DummyCallback<List<RSSItem>>(),
        new AsyncCallbackListener.DummyCallback<List<RSSChannel>>(), TaskManager.getManager());
  }

  @Override
  public String getLastBuildDate() {
    String lastRefresh = "";
    for (RSSChannel feed : cache.feeds) {
      if (feed.getLastBuildDate().compareTo(lastRefresh) > 0) {
        lastRefresh = feed.getLastBuildDate();
      }
    }
    return lastRefresh;
  }

  @Override
  public int getItemPositionByGuid(String guid) {
    int position = 0;
    while ((position < cache.items.size()) && (!guid.equals(cache.items.get(position).getGuid()))){
      position++;
    }
    if ((position >= cache.items.size()) || (!guid.equals(cache.items.get(position).getGuid()))) {
      position = -1;
    }
    return position;
  }

  @Override
  public String getItemGuidByPosition(int position) {
    String guid = "";
    if (!cache.items.isEmpty()){
      guid = cache.items.get(position).getGuid();
    }
    return guid;
  }

  @Override
  public void dataToPostProcess(ItemParser itemParser) {
    dataToPostProcess.add(itemParser);
  }

  @Override
  public void postProcessData() {
    Looper.prepare();
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        // dealing with them by groups of 3
        if (!dataToPostProcess.isEmpty()) {
          int subListSize = Math.min(3, dataToPostProcess.size());
          Log.d("postProcess", "-start- " + subListSize + " - " + dataToPostProcess.size());
          List<ItemParser> itemParsersSublist = dataToPostProcess.subList(0, subListSize);
          ItemParser[] itemParsers = itemParsersSublist.toArray(new ItemParser[subListSize]);
          dataToPostProcess.removeAll(itemParsersSublist);
          savingFeeds += subListSize;
          new AsyncTask<ItemParser, Integer, ItemParser[]>() {
            @Override
            protected ItemParser[] doInBackground(ItemParser... itemParsers) {
              for (ItemParser ip : itemParsers) {
                try {
                  ip.extractRSSItems(ip.getThresholdDate(getContext()));
                } catch (XmlPullParserException | IOException e) {
                  Log.w("extracting RSS items", e.toString());
                }
              }
              return itemParsers;
            }

            @Override
            protected void onPostExecute(ItemParser[] itemParsers) {
              for (ItemParser ip : itemParsers) {
                Log.d("postProcess", "callback: " + ip.getRssChannel().getTitle());
                ip.callback();
              }
              super.onPostExecute(itemParsers);
            }
          }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, itemParsers);
        }
      }
    }, 2000);

  }

  @Override
  public void addData(String feedUrl) {
    FeedAdder feedAdder = new FeedAdder(this, new DefaultNetworkChecker(), progressListener);
    if (feedUrl == null) {
      feedUrl = feedAdder.retrieveFeedFromClipboard();
    }
    if (feedUrl != null) {
      feedAdder.askForFeedValidation(cache.feeds, feedUrl);
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
      Stats.get(getContext()).increment(Stats.ACTION_ARCHIVE);
      cache.items.get(position).setArchived(true);
      itemsToDelete[i++] = cache.items.get(position);
    }
    saveItems(itemsToDelete);
  }

  @Override
  public void markDataRead(Set<Integer> selection, boolean isRead) {
    List<RSSItem> unreadItems = new ArrayList<>();
    for (int position : selection){
      if ((position > -1) && (position < cache.items.size()) && (isRead != cache.items.get(position).isRead())) {
        Stats.get(getContext()).increment(Stats.ACTION_MARK_READ);
        cache.items.get(position).setRead(isRead);
        unreadItems.add(cache.items.get(position));
      }
    }
    RSSItem[] itemsToSave = new RSSItem[unreadItems.size()];
    for (int j = 0; j < unreadItems.size(); j++) {
      itemsToSave[j] = unreadItems.get(j);
    }
    saveItems(itemsToSave);
  }

  @Override
  public void downloadMedia(Set<Integer> selection) {
    for (int position : selection){
      if (position > -1 && position < cache.items.size() && (cache.items.get(position).getMedia() != null)) {
        Stats.get(getContext()).increment(Stats.ACTION_DOWNLOAD);
        cache.items.get(position).downloadMedia(getContext(), new MediaDownloadListener.DummyMediaDownloadListener());
      }
    }
  }

  @Override
  public void createPlaylist(Set<Integer> selection) {
    Stats.get(getContext()).increment(Stats.ACTION_PLAYLIST);
    Playlist playlist = new Playlist();
    for (int i : selection) {
      Media media = cache.items.get(i).getMedia();
      if (media.isDownloaded(getContext(), false)) {
        playlist.add(media);
      }
    }
    playlist.createPlaylist(getContext());
  }

  private void saveItems(RSSItem... itemsToSave) {
    new AsyncDbSaveRSSItem(new LoadDataRefreshViewCallback<RSSItem>(new ProgressListener.DummyProgressListener(), this,
        new AsyncCallbackListener.DummyCallback<List<RSSItem>>(), new AsyncCallbackListener.DummyCallback<List<RSSChannel>>(), new AsyncCallbackListener.DummyCallback<List<RSSItem>>()),
        getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, itemsToSave);
  }

  @Override
  public Bundle getFeedItem(int position) {
    Bundle args = new Bundle();
    if (position < cache.items.size()) {
      RSSItem rssItem = cache.items.get(position);
      args.putParcelable(FeedItemReader.ITEM, rssItem);
      args.putParcelable(FeedItemReader.CHANNEL, cache.channelsByItem.get(rssItem));
    }
    return args;
  }

  @Override
  public void filterData(List<SelectionFilter> filters, AsyncCallbackListener<List<RSSItem>> itemsLoadedCallback, AsyncCallbackListener<List<RSSChannel>> channelsLoadedCallback) {
    boolean needsUnarchivedFilter = true;
    for (SelectionFilter filter : filters){
      if (filter instanceof ArchivedFilter){
        needsUnarchivedFilter = false;
      }
    }
    if (needsUnarchivedFilter){
      filters.add(new UnArchivedFilter());
    }
    boolean filtersAreUpToDate = cache.hasSimilarFilters(filters);
    if (!filtersAreUpToDate) {
      cache.itemFilters.clear();
      cache.itemFilters.addAll(filters);
      loadData(itemsLoadedCallback, channelsLoadedCallback, new AsyncCallbackListener.DummyCallback());
    } else {
      refreshView();
    }
  }

  @Override
  public int size() {
    return cache.items.size();
  }

  public NavigationDrawerProvider getNavigationDrawer() {
    if (navigationDrawerList == null) {
      navigationDrawerList = new NavigationDrawerList(getContext(), this, progressListener);
    }
    navigationDrawerList.clear();
    navigationDrawerList.addStaticItems();
    navigationDrawerList.addChannels(cache.feeds);
    return navigationDrawerList;
  }

  public class OnRSSItemClickListener implements AdapterView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
      Stats.get(getContext()).increment(Stats.ACTION_READ);
      Intent i = new Intent(getContext(), FeedItemReaderActivity.class);
      i.putExtra(FeedItemReaderActivity.INITIAL_POSITION, cache.items.get(position).getGuid());
      i.putParcelableArrayListExtra(FeedItemReaderActivity.ITEM_FILTER, cache.itemFilters);
      activity.startActivityForResult(i, REQ_ITEM_READ);
    }

  }

}
