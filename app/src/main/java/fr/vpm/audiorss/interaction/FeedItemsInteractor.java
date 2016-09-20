package fr.vpm.audiorss.interaction;

import android.content.Context;

import java.util.Collections;
import java.util.List;

import fr.vpm.audiorss.AllFeedItems;
import fr.vpm.audiorss.data.DefaultRSSRetriever;
import fr.vpm.audiorss.data.RSSRetriever;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.media.MediaDownloadManager;
import fr.vpm.audiorss.presentation.FeedItemsPresentation;
import fr.vpm.audiorss.presentation.FeedItemsPresenter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 12/03/16.
 */
public class FeedItemsInteractor implements FeedItemsInteraction, FeedItemsCache {

  private DBCache dbCache = new DBCache();
  private RSSRetriever rssRetriever;
  private MediaDownloadManager mediaDownloadManager;
  private Context feedItemsActivity;
  private FeedItemsPresentation feedItemsPresentation;
  private SelectionFilter filter;

  public FeedItemsInteractor(AllFeedItems feedItemsActivity, int rssItemLayout) {
    this.feedItemsActivity = feedItemsActivity;
    this.feedItemsPresentation = new FeedItemsPresenter(feedItemsActivity, this, rssItemLayout);
    this.rssRetriever = new DefaultRSSRetriever(feedItemsActivity, this, this);
    this.mediaDownloadManager = new MediaDownloadManager(feedItemsActivity);
  }

  @Override
  public void loadFeedItems() {
    dbCache.invalidate();
    dbCache.empty();
    if (this.filter != null) {
      rssRetriever.retrieveFeedItems(Collections.singletonList(this.filter));
    } else {
      rssRetriever.retrieveFeedItems();
    }
  }

  @Override
  public void loadFeedItems(SelectionFilter filter) {
    dbCache.invalidate();
    dbCache.empty();
    this.filter = filter;
    rssRetriever.retrieveFeedItems(Collections.singletonList(this.filter));
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
  public void downloadMedia(RSSItem feedItem) {
    mediaDownloadManager.downloadMedia(feedItem);
  }

  @Override
  public void deleteMedia(RSSItem feedItem) {
    mediaDownloadManager.deleteMedia(feedItem);
  }

  @Override
  public void cacheNewFeeds(List<RSSChannel> newFeeds) {
    persist(newFeeds);
    if (filter == null) {
      dbCache.addNewFeeds(newFeeds);
      feedItemsPresentation.presentFeeds(dbCache.getFeeds(), dbCache.getItems());
    } else {
      loadFeedItems();
    }
  }

  private void persist(List<RSSChannel> feeds) {
    for (RSSChannel feed : feeds) {
      feed.asyncSaveToDb(feedItemsActivity);
    }
  }

  @Override
  public void cacheFeeds(List<RSSChannel> feeds) {
    dbCache.setFeeds(feeds);
    if (dbCache.isValid()) {
      feedItemsPresentation.presentFeeds(dbCache.getFeeds(), dbCache.getItems());
    }
  }

  @Override
  public void cacheFeedItems(List<RSSItem> items) {
    dbCache.setItems(items);
    if (dbCache.isValid()) {
      feedItemsPresentation.presentFeeds(dbCache.getFeeds(), dbCache.getItems());
    }
  }

  @Override
  public void reportFeedRetrieveError() {
    feedItemsPresentation.presentFeedRetrieveError();
  }
}
