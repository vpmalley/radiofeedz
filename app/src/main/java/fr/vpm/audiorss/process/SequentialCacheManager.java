package fr.vpm.audiorss.process;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.rss.CachableRSSChannel;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 12/03/16.
 */
public class SequentialCacheManager {

  private Context context;
  private List<RSSChannel> rssChannels = new ArrayList<>();

  public SequentialCacheManager(Context context) {
    this.context = context;
  }

  public void retrieveFeedItemsFromNetwork(List<RSSChannel> feedsToRetrieve) {
    List<CachableRSSChannel> feeds = new ArrayList<>();
    for (RSSChannel rssChannel : feedsToRetrieve) {
      feeds.add(new CachableRSSChannel(rssChannel));
    }
    retrieveFeeds(feeds);
  }

  public void addFeed(String feedUrl) {
    List<CachableRSSChannel> feeds = new ArrayList<>();
    feeds.add(new CachableRSSChannel(feedUrl));
    retrieveFeeds(feeds);
  }

  private void retrieveFeeds(List<CachableRSSChannel> feedsToRetrieve) {
    for (CachableRSSChannel cachableFeed : feedsToRetrieve) {
      if (!cachableFeed.failed() && cachableFeed.shouldRefresh()) {
        cachableFeed.query(context);
      }

      if (!cachableFeed.failed() && cachableFeed.isQueried()) {
        cachableFeed.process(context);
      }

      if (!cachableFeed.failed() && cachableFeed.isProcessed()) {
        cachableFeed.persist(context);
        // TODO remove
      }

      if (!cachableFeed.failed() && cachableFeed.isProcessed()) {
        rssChannels.add(cachableFeed.getRSSChannel());
      }
    }
  }

  public List<RSSChannel> getRssChannels() {
    return rssChannels;
  }
}
