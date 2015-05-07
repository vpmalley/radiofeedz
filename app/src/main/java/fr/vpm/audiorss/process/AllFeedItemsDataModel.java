package fr.vpm.audiorss.process;

import android.app.Activity;
import android.app.DownloadManager;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import fr.vpm.audiorss.db.filter.ChannelFilter;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.db.filter.UnArchivedFilter;
import fr.vpm.audiorss.http.AsyncFeedRefresh;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.SaveFeedCallback;
import fr.vpm.audiorss.media.AsyncBitmapLoader;
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

  private static class ItemsAndFeeds {

    /**
     * the items of feeds that are displayed
     */
    List<RSSItem> items;

    List<RSSChannel> feeds;

    ArrayList<SelectionFilter> itemFilters;

    Map<RSSItem, RSSChannel> channelsByItem;

    private static ItemsAndFeeds instance;

    private ItemsAndFeeds() {
      items = new ArrayList<>();
      feeds = new ArrayList<>();
      itemFilters = new ArrayList<>();
      channelsByItem = new HashMap<>();
    }

    public static ItemsAndFeeds getInstance(){
      if (instance == null) {
        instance = new ItemsAndFeeds();
      }
      return instance;
    }

    /**
     * Determines whether the other list of filters is similar to this instance's filters
     * @param otherFilters other filters to compare with this instance's filters
     * @return whether the filters are the same
     */
    public boolean hasSimilarFilters(List<SelectionFilter> otherFilters) {
      Set<String> filterNames = getFilterNames(this.itemFilters);
      Set<String> otherFilterNames = getFilterNames(otherFilters);
      return filterNames.equals(otherFilterNames);
    }

    private Set<String> getFilterNames(List<SelectionFilter> filters){
      Set<String> filterNames = new HashSet<>();
      for (SelectionFilter filter : filters) {
        if (filter instanceof ChannelFilter) {
          filterNames.add(filter.getClass().getName() + ",id=" + filter.getSelectionValues()[0]);
        } else {
          filterNames.add(filter.getClass().getName());
        }
      }
      return filterNames;
    }

  }

  private final ItemsAndFeeds coreData;

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

  private final boolean preloadPictures;

  public AllFeedItemsDataModel(Activity activity, ProgressListener progressListener, FeedsActivity<RSSItemArrayAdapter>
      feedsActivity, int resId, boolean preloadPictures) {
    this.progressListener = progressListener;
    this.feedsActivity = feedsActivity;
    this.activity = activity;
    this.resource = resId;
    //this.preloadPictures = preloadPictures;
    this.preloadPictures = false;
    this.coreData = ItemsAndFeeds.getInstance();
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
        new ChannelRefreshViewCallback(channelsLoadedCallback, progressListener, this);
    AsyncDbReadRSSChannel asyncDbReader = new AsyncDbReadRSSChannel(callback, getContext(), readItems);
    // read all RSSChannel items from DB and refresh views
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  public void loadDataFromItems(AsyncCallbackListener<List<RSSItem>> itemsLoadedCallback) {
    Log.d("", "");
    AsyncCallbackListener<List<RSSItem>> callback = new ItemRefreshViewCallback(itemsLoadedCallback, progressListener, this);
    AsyncDbReadRSSItems asyncDbReader = new AsyncDbReadRSSItems(callback, getContext(), coreData.itemFilters);
    // read all RSS items from DB and refresh views
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @Override
  public boolean isReady() {
    return !coreData.channelsByItem.isEmpty();
  }

  @Override
  public void setChannelsAndBuildModel(List<RSSChannel> channels) {
    if (channels.size() != this.coreData.feeds.size()){
      recreate = true;
    }
    this.coreData.feeds = channels;
    buildChannelsByItem();
  }

  @Override
  public void setItemsAndBuildModel(List<RSSItem> items) {
    if (items.size() != this.coreData.items.size()){
      recreate = true;
    }
    this.coreData.items = items;
    buildChannelsByItem();
    if (preloadPictures) {
      List<RSSItem> rssItemsSubset = coreData.items;
      int MAX_CACHED = 10;
      if (coreData.items.size() > MAX_CACHED){
        rssItemsSubset = coreData.items.subList(6, Math.min(coreData.items.size(), MAX_CACHED));
      }
      new AsyncBitmapLoader(getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rssItemsSubset.toArray(new RSSItem[rssItemsSubset.size()]));
    }
  }

  private void buildChannelsByItem(){
    if ((coreData.items.size() > 0) && (coreData.feeds.size() > 0)) {
      coreData.channelsByItem.clear();
      for (RSSItem item : coreData.items) {
        for (RSSChannel channel : coreData.feeds) {
          if (channel.getId() == item.getChannelId()) {
            coreData.channelsByItem.put(item, channel);
          }
        }
      }
      refreshView();
    }
  }

  @Override
  public synchronized void refreshView() {
    if (rssItemAdapter == null || recreate) {
      rssItemAdapter = new RSSItemArrayAdapter(activity, resource, coreData.items, coreData.channelsByItem);
      feedsActivity.refreshView(rssItemAdapter, getNavigationDrawer());
      recreate = false;
    } else {
      rssItemAdapter.setItems(coreData.items);
      rssItemAdapter.setChannelsByItem(coreData.channelsByItem);
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
      Iterator<RSSChannel> feedsIterator = coreData.feeds.iterator();
      RSSChannel nextChannel = null;
      Log.d("prerefresh", "feeds: " + coreData.feeds.size());
      final TaskManager tm = TaskManager.getManager();
      while (feedsIterator.hasNext() && !(nextChannel = feedsIterator.next()).shouldRefresh()) {
      }

      if ((nextChannel != null) && (nextChannel.shouldRefresh())) {
        Log.d("prerefresh", nextChannel.getUrl());
        //queueFeedRefresh(tm, nextChannel);
        //savingFeeds++;
      }
      tm.startTasks();
    }
  }

  @Override
  public void refreshData(){
    boolean forceRefresh = shouldForceRefresh();
    /*
    final TaskManager tm = TaskManager.getManager();
    for (final RSSChannel feed : coreData.feeds.subList(0, Math.min(50, coreData.feeds.size()))) {
      Log.d("refreshing", feed.getTitle() + " : " + forceRefresh + "/" + feed.shouldRefresh() + " : " + feed.getNextRefresh());
      if ((forceRefresh) || (feed.shouldRefresh())) {
        queueFeedRefresh(tm, feed);
        savingFeeds++;
      }
    }
    tm.startTasks();
    */
    CacheManager cm = CacheManager.createManager(coreData.feeds, this, progressListener);
    cm.updateCache(getContext());
  }

  @Override
  public void refreshData(Set<Integer> selection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void refreshData(List<RSSChannel> feedsToUpdate) {
    final TaskManager tm = TaskManager.getManager();
    for (final RSSChannel feed : feedsToUpdate) {
      Log.d("refreshing", feed.getTitle());
      queueFeedRefresh(tm, feed);
      savingFeeds++;
    }
    tm.startTasks();
  }

  /**
   * Queues a task for feed refresh
   * @param tm the task manager
   * @param feed the feed to refresh
   */
  private void queueFeedRefresh(final TaskManager tm, final RSSChannel feed) {
    tm.queueTask(new TaskManager.AsynchTask() {
      @Override
      public void execute() {
        LoadDataRefreshViewCallback<RSSChannel> rssChannelCallback = new LoadDataRefreshViewCallback<RSSChannel>(progressListener,
            AllFeedItemsDataModel.this, new AsyncCallbackListener.DummyCallback<List<RSSItem>>(), new AsyncCallbackListener.DummyCallback<List<RSSChannel>>(), tm);
        SaveFeedCallback callback = new SaveFeedCallback(progressListener, AllFeedItemsDataModel.this, rssChannelCallback);
        new AsyncFeedRefresh(getContext(), callback, AllFeedItemsDataModel.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
            feed.getUrl());
      }

      @Override
      public TaskManager.Priority getPriority() {
        return TaskManager.Priority.HIGH;
      }
    });
  }

  @Override
  public void onFeedFailureBeforeLoad() {
    loadData(new AsyncCallbackListener.DummyCallback<List<RSSItem>>(),
        new AsyncCallbackListener.DummyCallback<List<RSSChannel>>(), TaskManager.getManager());
  }

  @Override
  public String getLastBuildDate() {
    String lastRefresh = "";
    for (RSSChannel feed : coreData.feeds) {
      if (feed.getLastBuildDate().compareTo(lastRefresh) > 0) {
        lastRefresh = feed.getLastBuildDate();
      }
    }
    return lastRefresh;
  }

  @Override
  public int getItemPositionByGuid(String guid) {
    int position = 0;
    while ((position < coreData.items.size()) && (!guid.equals(coreData.items.get(position).getGuid()))){
      position++;
    }
    if ((position >= coreData.items.size()) || (!guid.equals(coreData.items.get(position).getGuid()))) {
      position = -1;
    }
    return position;
  }

  @Override
  public String getItemGuidByPosition(int position) {
    String guid = "";
    if (!coreData.items.isEmpty()){
      guid = coreData.items.get(position).getGuid();
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
      feedAdder.askForFeedValidation(coreData.feeds, feedUrl);
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
      coreData.items.get(position).setArchived(true);
      itemsToDelete[i++] = coreData.items.get(position);
    }
    saveItems(itemsToDelete);
  }

  @Override
  public void markDataRead(Set<Integer> selection, boolean isRead) {
    List<RSSItem> unreadItems = new ArrayList<>();
    int i = 0;
    for (int position : selection){
      if ((position > -1) && (position < coreData.items.size()) && (!coreData.items.get(position).isRead())) {
        coreData.items.get(position).setRead(isRead);
        unreadItems.add(coreData.items.get(position));
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
      if (position > -1 && position < coreData.items.size() && (coreData.items.get(position).getMedia() != null)) {
        coreData.items.get(position).getMedia().download(getContext(), DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
            new MediaDownloadListener.DummyMediaDownloadListener());
      }
    }
  }

  @Override
  public void createPlaylist(Set<Integer> selection) {
    Playlist playlist = new Playlist();
    for (int i : selection) {
      Media media = coreData.items.get(i).getMedia();
      if (media.isDownloaded(getContext(), false)) {
        playlist.add(media);
      }
    }
    playlist.createPlaylist(getContext());
  }

  private void saveItems(RSSItem... itemsToSave) {
    new AsyncDbSaveRSSItem(new LoadDataRefreshViewCallback<RSSItem>(progressListener, this,
        new AsyncCallbackListener.DummyCallback<List<RSSItem>>(), new AsyncCallbackListener.DummyCallback<List<RSSChannel>>(), new AsyncCallbackListener.DummyCallback<List<RSSItem>>()),
        getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, itemsToSave);
  }

  @Override
  public Bundle getFeedItem(int position) {
    Bundle args = new Bundle();
    if (position < coreData.items.size()) {
      RSSItem rssItem = coreData.items.get(position);
      args.putParcelable(FeedItemReader.ITEM, rssItem);
      args.putParcelable(FeedItemReader.CHANNEL, coreData.channelsByItem.get(rssItem));
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
    boolean filtersAreUpToDate = coreData.hasSimilarFilters(filters);
    if (!filtersAreUpToDate) {
      coreData.itemFilters.clear();
      coreData.itemFilters.addAll(filters);
      loadData(itemsLoadedCallback, channelsLoadedCallback, new AsyncCallbackListener.DummyCallback());
    } else {
      refreshView();
    }
  }

  @Override
  public int size() {
    return coreData.items.size();
  }

  public NavigationDrawerProvider getNavigationDrawer() {
    if (navigationDrawerList == null) {
      navigationDrawerList = new NavigationDrawerList(getContext(), this, progressListener);
    }
    navigationDrawerList.clear();
    navigationDrawerList.addStaticItems();
    navigationDrawerList.addChannels(coreData.feeds);
    return navigationDrawerList;
  }

  public class OnRSSItemClickListener implements AdapterView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
      Intent i = new Intent(getContext(), FeedItemReaderActivity.class);
      i.putExtra(FeedItemReaderActivity.INITIAL_POSITION, coreData.items.get(position).getGuid());
      i.putParcelableArrayListExtra(FeedItemReaderActivity.ITEM_FILTER, coreData.itemFilters);
      activity.startActivityForResult(i, REQ_ITEM_READ);
    }

  }

}
