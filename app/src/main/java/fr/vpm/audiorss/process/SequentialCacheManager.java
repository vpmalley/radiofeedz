package fr.vpm.audiorss.process;

import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.exception.RetrieveException;
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

  public void retrieveFeedItemsFromNetwork(List<RSSChannel> feedsToRetrieve) throws RetrieveException, XmlPullParserException, ParseException, IOException {
    List<CachableRSSChannel> feeds = new ArrayList<>();
    for (RSSChannel rssChannel : feedsToRetrieve) {
      feeds.add(new CachableRSSChannel(rssChannel));
    }
    retrieveFeeds(feeds);
  }

  public void addFeed(String feedUrl) throws RetrieveException, XmlPullParserException, ParseException, IOException {
    List<CachableRSSChannel> feeds = new ArrayList<>();
    feeds.add(new CachableRSSChannel(feedUrl));
    retrieveFeeds(feeds);
  }

  private void retrieveFeeds(List<CachableRSSChannel> feedsToRetrieve) throws RetrieveException, XmlPullParserException, ParseException, IOException {
    for (CachableRSSChannel cachableFeed : feedsToRetrieve) {
      if (cachableFeed.shouldRefresh()) {
        cachableFeed.query(context);
        cachableFeed.process(context);
        rssChannels.add(cachableFeed.getRSSChannel());
      }
    }
  }

  public List<RSSChannel> getRssChannels() {
    return rssChannels;
  }
}
