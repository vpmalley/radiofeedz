package fr.vpm.audiorss.presentation;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import fr.vpm.audiorss.AllFeedItems;
import fr.vpm.audiorss.R;
import fr.vpm.audiorss.data.NetworkRSSRetriever;
import fr.vpm.audiorss.data.RSSRetriever;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.process.DateUtils;
import fr.vpm.audiorss.process.ItemComparator;
import fr.vpm.audiorss.process.RSSCache;
import fr.vpm.audiorss.process.RSSItemArrayAdapter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 12/03/16.
 */
public class FeedItemsPresenter implements FeedItemsInteraction, FeedItemsPresentation {

  private RSSCache cache = new RSSCache();
  private RSSRetriever rssRetriever;

  private AllFeedItems feedItemsActivity;
  private RSSItemArrayAdapter rssItemAdapter;
  private int rssItemLayout = R.layout.list_rss_item;

  public FeedItemsPresenter(AllFeedItems feedItemsActivity, int rssItemLayout) {
    this.feedItemsActivity = feedItemsActivity;
    this.rssItemLayout = rssItemLayout;
    this.rssRetriever = new NetworkRSSRetriever(feedItemsActivity, this);
  }

  @Override
  public void loadFeedItems() {
    cache.invalidate();
    cache.empty();
    rssRetriever.retrieveFeedItems();
  }

  @Override
  public void loadFeedItems(List<SelectionFilter> filters) {
    cache.invalidate();
    cache.empty();
    rssRetriever.retrieveFeedItems(filters);
  }

  @Override
  public void retrieveLatestFeedItems() {
    rssRetriever.forceRetrieveFeedItemsFromNetwork(cache.getFeeds());
  }

  @Override
  public void retrieveLatestFeedItems(List<RSSChannel> feedsToRetrieve) {
    rssRetriever.forceRetrieveFeedItemsFromNetwork(feedsToRetrieve);
  }

  @Override
  public void addFeed(String feedUrl) {
    rssRetriever.addFeed(feedUrl);
  }

  @Override
  public void deleteFeeds(List<RSSChannel> feedsToRetrieve) {
    rssRetriever.deleteFeeds(feedsToRetrieve);
  }

  @Override
  public void markAsRead(List<RSSItem> feedItems, boolean isRead) {
    rssRetriever.markAsRead(feedItems, isRead);
  }

  @Override
  public void archiveFeedItems(List<RSSItem> feedItems) {
    rssRetriever.archiveFeedItems(feedItems);
  }

  @Override
  public void downloadMedia(List<RSSItem> feedItems) {
    rssRetriever.downloadMedia(feedItems);
  }

  @Override
  public RSSItem getFeedItem(String feedItemGuid) {
    return cache.getFeedItem(feedItemGuid);
  }


  @Override
  public void presentFeeds(List<RSSChannel> feeds) {
    persist(feeds);
    addToCache(feeds);
    if (cache.isValid()) {
      displayCachedFeedItems();
    }
  }

  private void addToCache(List<RSSChannel> feeds) {
    cache.invalidate();
    Set<RSSChannel> allChannels = new HashSet<>();
    allChannels.addAll(feeds);
    allChannels.addAll(cache.getFeeds());
    cache.setFeeds(new ArrayList<>(allChannels));

    Set<RSSItem> rssItems = new HashSet<>();
    for (RSSChannel feed: feeds) {
      rssItems.addAll(feed.getItems());
    }
    rssItems.addAll(cache.getItems());

    List<RSSItem> sortedItems = sortAndCapItems(new ArrayList<>(rssItems));
    cache.setItems(sortedItems);

    cache.buildChannelsByItem();
  }

  public List<RSSItem> sortAndCapItems(List<RSSItem> rssItems) {
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(feedItemsActivity);
    String ordering = sharedPrefs.getString("pref_feed_ordering", "pubDate DESC");
    Collections.sort(rssItems, new ItemComparator(ordering));
    String limit = sharedPrefs.getString("pref_disp_max_items", "80");
    if (limit == null || !Pattern.compile("\\d+").matcher(limit).matches()){
      limit = "80";
    }
    int actualLimit = Math.min(rssItems.size(), Integer.valueOf(limit));
    return rssItems.subList(0, actualLimit);
  }

  private void persist(List<RSSChannel> feeds) {
    for (RSSChannel feed : feeds) {
      feed.asyncSaveToDb(feedItemsActivity);
    }
  }

  @Override
  public void setFeedsAndBuildModel(List<RSSChannel> feeds) {
    cache.setFeeds(feeds);
    cache.buildChannelsByItem();
    if (cache.isValid()) {
      displayCachedFeedItems();
    }
  }

  @Override
  public void setItemsAndBuildModel(List<RSSItem> items) {
    cache.setItems(items);
    cache.buildChannelsByItem();
    if (cache.isValid()) {
      displayCachedFeedItems();
    }
  }

  public synchronized void displayCachedFeedItems() {
    if (rssItemAdapter == null) {
      rssItemAdapter = new RSSItemArrayAdapter(feedItemsActivity, rssItemLayout, cache.getItems(), cache.getChannelsByItem(), this);
      feedItemsActivity.refreshFeedItems(rssItemAdapter);
    } else {
      rssItemAdapter.setItems(cache.getItems());
      rssItemAdapter.setChannelsByItem(cache.getChannelsByItem());
      rssItemAdapter.notifyDataSetChanged();
    }

    feedItemsActivity.refreshNavigationDrawer(cache.getFeeds());
    presentLastRefreshTime();
  }

  private void presentLastRefreshTime() {
    String lastRefreshTime = "";
    for (RSSChannel feed : cache.getFeeds()) {
      if (lastRefreshTime.compareTo(feed.getLastBuildDate()) < 0) {
        lastRefreshTime = feed.getLastBuildDate();
      }
    }
    String displayDate = DateUtils.getDisplayDate(lastRefreshTime);
    feedItemsActivity.displayLastRefreshTime(displayDate);
  }
}
