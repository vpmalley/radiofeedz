package fr.vpm.audiorss.interaction;

import java.util.List;

import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 12/03/16.
 */
public interface FeedItemsCache {

  void cacheNewFeeds(List<RSSChannel> newFeeds);

  /**
   * Sets the feeds to the in-memory cache
   * @param allFeeds feeds to cache
   */
  void cacheFeeds(List<RSSChannel> allFeeds);


  /**
   * Sets the items to the in-memory cache
   * @param allItems items to cache
   */
  void cacheFeedItems(List<RSSItem> allItems);

}
