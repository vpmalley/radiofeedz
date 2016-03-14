package fr.vpm.audiorss.data;

import android.content.Context;

import java.util.List;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.presentation.FeedItemsPresentation;
import fr.vpm.audiorss.process.SequentialCacheManager;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 12/03/16.
 */
public class NetworkRSSRetriever implements RSSRetriever {

  private Context context;
  private FeedItemsPresentation feedsItemPresenter;

  public NetworkRSSRetriever(Context context, FeedItemsPresentation feedsItemPresenter) {
    this.context = context;
    this.feedsItemPresenter = feedsItemPresenter;
  }

  @Override
  public void retrieveFeedItems() {
    // retrieve from db and call delegate back
  }

  @Override
  public void retrieveFeedItems(List<SelectionFilter> filters) {

  }

  @Override
  public void forceRetrieveFeedItemsFromNetwork(List<RSSChannel> feedsToRetrieve) {
    SequentialCacheManager cm = new SequentialCacheManager(context, feedsItemPresenter);
    cm.retrieveFeedItemsFromNetwork(feedsToRetrieve);
  }

  @Override
  public void addFeed(String feedUrl) {
    SequentialCacheManager cm = new SequentialCacheManager(context, feedsItemPresenter);
    cm.addFeed(feedUrl);
  }
}
