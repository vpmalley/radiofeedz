package fr.vpm.audiorss.data;

import java.util.List;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 12/03/16.
 */
public interface RSSRetriever {

  /**
   * Retrieves the RSS information, from any source (In-Memory/DB/Server)
   */
  void retrieveFeedItems();

  /**
   * Retrieves the RSS information, from any source (In-Memory/DB/Server)
   * @param filters filters for the DB
   */
  void retrieveFeedItems(List<SelectionFilter> filters);

  /**
   * Retrieves the RSS information, from Servers, for select feeds
   */
  void forceRetrieveFeedItemsFromNetwork(List<RSSChannel> feedsToRetrieve);

  void addFeed(String feedUrl);

  void deleteFeeds(List<RSSChannel> feedsToRetrieve);

  void markAsRead(List<RSSItem> feedItems, boolean isRead);

  void archiveFeedItems(List<RSSItem> feedItems);
}
