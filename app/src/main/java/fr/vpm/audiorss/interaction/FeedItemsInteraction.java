package fr.vpm.audiorss.interaction;

import java.util.List;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 12/03/16.
 */
public interface FeedItemsInteraction {

  void loadFeedItems();

  void loadFeedItems(SelectionFilter filter);

  void retrieveLatestFeedItems();

  void retrieveLatestFeedItems(List<RSSChannel> feedsToRetrieve);

  void addFeed(String feedUrl);

  void deleteFeeds(List<RSSChannel> feedsToRetrieve);

  void markAsRead(List<RSSItem> feedItems, boolean isRead);

  void archiveFeedItems(List<RSSItem> feedItems);

  void downloadMedia(RSSItem feedItem);

  void deleteMedia(RSSItem feedItem);

}
