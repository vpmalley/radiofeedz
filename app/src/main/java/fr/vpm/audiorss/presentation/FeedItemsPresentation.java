package fr.vpm.audiorss.presentation;

import java.util.List;

import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 12/03/16.
 */
public interface FeedItemsPresentation {

  void presentNewFeeds(List<RSSChannel> newFeeds);

  /**
   * Sets the feeds to the in-memory cache
   * @param allFeeds feeds to cache
   */
  void presentFeeds(List<RSSChannel> allFeeds);


  /**
   * Sets the items to the in-memory cache
   * @param allItems items to cache
   */
  void presentItems(List<RSSItem> allItems);

}
