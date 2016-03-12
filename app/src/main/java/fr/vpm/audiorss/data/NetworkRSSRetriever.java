package fr.vpm.audiorss.data;

import android.content.Context;

import java.util.List;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.process.CacheManager;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 12/03/16.
 */
public class NetworkRSSRetriever implements RSSRetriever {

  private Context context;

  @Override
  public void retrieveFeedItems() {
    // retrieve from db and call delegate back
  }

  @Override
  public void retrieveFeedItems(List<SelectionFilter> filters) {

  }

  @Override
  public void forceRetrieveFeedItemsFromNetwork(List<RSSChannel> feedsToRetrieve) {
    CacheManager cm = new CacheManager(feedsToRetrieve, this);
    cm.updateCache(context);
  }

  @Override
  public void addFeed(String feedUrl) {

  }
}
