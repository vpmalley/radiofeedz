package fr.vpm.audiorss.presentation;

import java.util.List;

import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 12/03/16.
 */
public interface FeedItemsPresentation {


  /**
   * Sets the feeds to the in-memory cache
   * @param allFeeds feeds to cache
   */
  void presentFeeds(List<RSSChannel> allFeeds);

}
