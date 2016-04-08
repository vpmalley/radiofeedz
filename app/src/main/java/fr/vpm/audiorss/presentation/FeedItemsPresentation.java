package fr.vpm.audiorss.presentation;

import java.util.List;

import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 08/04/16.
 */
public interface FeedItemsPresentation {

  void presentFeeds(List<RSSChannel> feeds, List<RSSItem> feedItems);
}
