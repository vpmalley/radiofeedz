package fr.vpm.audiorss.process;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
    this.preloadPictures = preloadPictures;
    this.coreData = ItemsAndFeeds.getInstance();
  }

  @Override
  public void loadData() {
    Log.d("decrease", String.valueOf(savingFeeds));
    if (savingFeeds > 0) {
      savingFeeds--;
    }
    if (savingFeeds < 2) {
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
      int MAX_CACHED = 20;
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

  @Override
  public void refreshData(){
    boolean forceRefresh = shouldForceRefresh();
    for (RSSChannel feed : coreData.feeds) {
      Log.d("refreshing", feed.getTitle() + " : " + forceRefresh + "/" + feed.shouldRefresh() + " : " + feed.getNextRefresh());
      if ((forceRefresh) || (feed.shouldRefresh())) {
        SaveFeedCallback callback = new SaveFeedCallback(progressListener, this);
        new AsyncFeedRefresh(getContext(), callback, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                feed.getUrl());
        savingFeeds++;
      }
    }
  }

  public boolean shouldForceRefresh() {
    Calendar fiveMinutesAgo = Calendar.getInstance();
    fiveMinutesAgo.add(Calendar.MINUTE, -5);
    Date lastBuildDate = DateUtils.parseDBDate(getLastBuildDate(), fiveMinutesAgo.getTime());
    return lastBuildDate.after(fiveMinutesAgo.getTime());
  }

  @Override
  public void refreshData(Set<Integer> selection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void refreshData(List<RSSChannel> feedsToUpdate) {
    for (RSSChannel feed : feedsToUpdate) {
      Log.d("refresh", feed.getTitle());
      SaveFeedCallback callback = new SaveFeedCallback(progressListener, this);
      new AsyncFeedRefresh(getContext(), callback, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
              feed.getUrl());
      savingFeeds++;
    }
  }

  @Override
  public void onFeedFailureBeforeLoad() {
    savingFeeds--;
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
    new AsyncDbSaveRSSItem(new LoadDataRefreshViewCallback<RSSItem>(progressListener, this),
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
  public void filterData(List<SelectionFilter> filters) {
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
      loadData();
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
