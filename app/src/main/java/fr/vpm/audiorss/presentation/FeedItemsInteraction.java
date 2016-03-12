package fr.vpm.audiorss.presentation;

import java.util.List;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 12/03/16.
 */
public interface FeedItemsInteraction {

  void loadFeedItems();

  void loadFeedItems(List<SelectionFilter> filters);

  void retrieveLatestFeedItems();

  void retrieveLatestFeedItems(List<RSSChannel> feedsToRetrieve);

  void addData(String feedUrl);

  RSSItem getFeedItem(String feedItemGuid);

}
