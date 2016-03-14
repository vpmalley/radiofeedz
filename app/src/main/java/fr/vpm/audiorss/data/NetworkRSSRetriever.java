package fr.vpm.audiorss.data;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.db.AsyncDbReadRSSChannel;
import fr.vpm.audiorss.db.AsyncDbReadRSSItems;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.presentation.FeedItemsPresentation;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.SequentialCacheManager;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

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
    retrieveItemsFromDb(new ArrayList<SelectionFilter>());
    retrieveChannelsFromDb();
  }

  @Override
  public void retrieveFeedItems(List<SelectionFilter> filters) {
    retrieveItemsFromDb(filters);
    retrieveChannelsFromDb();
  }

  public void retrieveChannelsFromDb() {
    AsyncCallbackListener<List<RSSChannel>> callback = new AsyncCallbackListener<List<RSSChannel>>() {

      @Override
      public void onPreExecute() {}

      @Override
      public void onPostExecute(List<RSSChannel> rssChannels) {
        feedsItemPresenter.setFeedsAndBuildModel(rssChannels);
      }
    };
    AsyncDbReadRSSChannel asyncDbReader = new AsyncDbReadRSSChannel(callback, context, false);
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  public void retrieveItemsFromDb(List<SelectionFilter> filters) {
    AsyncCallbackListener<List<RSSItem>> callback = new AsyncCallbackListener<List<RSSItem>>() {

      @Override
      public void onPreExecute() {}

      @Override
      public void onPostExecute(List<RSSItem> rssItems) {
        feedsItemPresenter.setItemsAndBuildModel(rssItems);
      }
    };
    AsyncDbReadRSSItems asyncDbReader = new AsyncDbReadRSSItems(callback, context, filters);
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
