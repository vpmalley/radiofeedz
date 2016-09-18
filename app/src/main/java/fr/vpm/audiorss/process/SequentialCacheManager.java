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

  public boolean retrieveFeedItemsFromNetwork(List<RSSChannel> feedsToRetrieve) throws XmlPullParserException, ParseException, IOException {
    List<CachableRSSChannel> feeds = new ArrayList<>();
    for (RSSChannel rssChannel : feedsToRetrieve) {
      feeds.add(new CachableRSSChannel(rssChannel));
    }
    return retrieveFeeds(feeds);
  }

  public boolean addFeed(String feedUrl) throws XmlPullParserException, ParseException, IOException {
    List<CachableRSSChannel> feeds = new ArrayList<>();
    feeds.add(new CachableRSSChannel(feedUrl));
    return retrieveFeeds(feeds);
  }

  private boolean retrieveFeeds(List<CachableRSSChannel> feedsToRetrieve) throws XmlPullParserException, ParseException, IOException {
    boolean failed = false;
    for (CachableRSSChannel cachableFeed : feedsToRetrieve) {
      if (cachableFeed.shouldRefresh()) {
        try {
          cachableFeed.query(context);
          cachableFeed.process(context);
          rssChannels.add(cachableFeed.getRSSChannel());
        } catch (RetrieveException e) {
          failed = true;
        }
      }
    }
    return failed;
  }

  public List<RSSChannel> getRssChannels() {
    return rssChannels;
  }
}
