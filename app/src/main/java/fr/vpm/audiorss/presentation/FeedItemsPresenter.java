package fr.vpm.audiorss.presentation;

import java.util.List;

import fr.vpm.audiorss.AllFeedItems;
import fr.vpm.audiorss.R;
import fr.vpm.audiorss.data.NetworkRSSRetriever;
import fr.vpm.audiorss.data.RSSRetriever;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.process.DateUtils;
import fr.vpm.audiorss.process.RSSItemArrayAdapter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 12/03/16.
 */
public class FeedItemsPresenter implements FeedItemsInteraction, FeedItemsPresentation {

  private DBCache dbCache = new DBCache();
  private RSSCache rssCache = new RSSCache();
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
    dbCache.invalidate();
    dbCache.empty();
    rssRetriever.retrieveFeedItems();
  }

  @Override
  public void loadFeedItems(List<SelectionFilter> filters) {
    dbCache.invalidate();
    dbCache.empty();
    rssRetriever.retrieveFeedItems(filters);
  }

  @Override
  public void retrieveLatestFeedItems() {
    rssRetriever.forceRetrieveFeedItemsFromNetwork(dbCache.getFeeds());
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
  public void presentNewFeeds(List<RSSChannel> newFeeds) {
    persist(newFeeds);
    rssCache.buildFromFeeds(newFeeds, feedItemsActivity);
    displayCachedFeedItems();
  }

  private void persist(List<RSSChannel> feeds) {
    for (RSSChannel feed : feeds) {
      feed.asyncSaveToDb(feedItemsActivity);
    }
  }

  @Override
  public void presentFeeds(List<RSSChannel> feeds) {
    dbCache.setFeeds(feeds);
    if (dbCache.isValid()) {
      rssCache.buildFromDBCache(dbCache);
      displayCachedFeedItems();
    }
  }

  @Override
  public void presentItems(List<RSSItem> items) {
    dbCache.setItems(items);
    if (dbCache.isValid()) {
      rssCache.buildFromDBCache(dbCache);
      displayCachedFeedItems();
    }
  }

  public synchronized void displayCachedFeedItems() {
    if (rssItemAdapter == null) {
      rssItemAdapter = new RSSItemArrayAdapter(feedItemsActivity, rssItemLayout, rssCache.getDisplayedRSSItems(), this);
      feedItemsActivity.refreshFeedItems(rssItemAdapter);
    } else {
      rssItemAdapter.notifyDataSetChanged();
    }

    feedItemsActivity.refreshNavigationDrawer(dbCache.getFeeds());
    presentLastRefreshTime();
  }

  private void presentLastRefreshTime() {
    String displayDate = DateUtils.getDisplayDate(rssCache.getLastBuildDate());
    feedItemsActivity.displayLastRefreshTime(displayDate);
  }
}
