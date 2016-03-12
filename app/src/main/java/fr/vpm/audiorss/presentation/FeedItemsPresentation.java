package fr.vpm.audiorss.presentation;

import java.util.List;

import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 12/03/16.
 */
public interface FeedItemsPresentation {


  /**
   * Sets the channels to the in-memory cache
   * @param allFeeds feeds to cache
   */
  void setChannelsAndBuildModel(List<RSSChannel> allFeeds);


  /**
   * Sets the items to the in-memory cache
   * @param allItems items to cache
   */
  void setItemsAndBuildModel(List<RSSItem> allItems);

}
