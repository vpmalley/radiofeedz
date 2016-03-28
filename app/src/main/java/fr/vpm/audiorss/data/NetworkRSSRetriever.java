package fr.vpm.audiorss.data;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.db.AsyncDbDeleteRSSChannel;
import fr.vpm.audiorss.db.AsyncDbReadRSSChannel;
import fr.vpm.audiorss.db.AsyncDbReadRSSItems;
import fr.vpm.audiorss.db.AsyncDbSaveRSSItem;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.media.MediaDownloadListener;
import fr.vpm.audiorss.presentation.FeedItemsPresentation;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.SequentialCacheManager;
import fr.vpm.audiorss.process.Stats;
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
        feedsItemPresenter.presentFeeds(rssChannels);
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
        feedsItemPresenter.presentItems(rssItems);
      }
    };
    AsyncDbReadRSSItems asyncDbReader = new AsyncDbReadRSSItems(callback, context, filters);
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @Override
  public void forceRetrieveFeedItemsFromNetwork(List<RSSChannel> feedsToRetrieve) {
    AsyncTask<List<RSSChannel>, Integer, List<RSSChannel>> asyncSequentialCacheManager = new AsyncTask<List<RSSChannel>, Integer, List<RSSChannel>>() {
      @Override
      protected List<RSSChannel> doInBackground(List<RSSChannel>... feeds) {
        SequentialCacheManager cm = new SequentialCacheManager(context);
        cm.retrieveFeedItemsFromNetwork(feeds[0]);
        return cm.getRssChannels();
      }

      @Override
      protected void onPostExecute(List<RSSChannel> rssChannels) {
        feedsItemPresenter.presentNewFeeds(rssChannels);
      }
    };
    asyncSequentialCacheManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, feedsToRetrieve);
  }

  @Override
  public void addFeed(final String feedUrl) {
    AsyncTask<String, Integer, List<RSSChannel>> asyncSequentialCacheManager = new AsyncTask<String, Integer, List<RSSChannel>>() {
      @Override
      protected List<RSSChannel> doInBackground(String... feedUrls) {
        SequentialCacheManager cm = new SequentialCacheManager(context);
        cm.addFeed(feedUrls[0]);
        return cm.getRssChannels();
      }

      @Override
      protected void onPostExecute(List<RSSChannel> rssChannels) {
        feedsItemPresenter.presentNewFeeds(rssChannels);
      }
    };
    asyncSequentialCacheManager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, feedUrl);
  }

  @Override
  public void deleteFeeds(List<RSSChannel> feedsToRetrieve) {
    RSSChannel[] feedsToDelete = new RSSChannel[feedsToRetrieve.size()];
    for (int j = 0; j < feedsToRetrieve.size(); j++) {
      Stats.get(context).increment(Stats.ACTION_FEED_DELETE);
      feedsToDelete[j] = feedsToRetrieve.get(j);
    }

    if (feedsToDelete.length > 0) {
      new AsyncDbDeleteRSSChannel(new AsyncCallbackListener.DummyCallback<List<RSSChannel>>(),
              context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, feedsToDelete);
    }
  }

  @Override
  public void markAsRead(List<RSSItem> feedItems, boolean isRead) {
    RSSItem[] itemsToSave = new RSSItem[feedItems.size()];
    for (int j = 0; j < feedItems.size(); j++) {
      Stats.get(context).increment(Stats.ACTION_MARK_READ);
    feedItems.get(j).setRead(isRead);
      itemsToSave[j] = feedItems.get(j);
    }
    saveItems(itemsToSave);
  }

  @Override
  public void archiveFeedItems(List<RSSItem> feedItems) {
    RSSItem[] itemsToSave = new RSSItem[feedItems.size()];
    for (int j = 0; j < feedItems.size(); j++) {
      Stats.get(context).increment(Stats.ACTION_ARCHIVE);
      feedItems.get(j).setArchived(true);
      itemsToSave[j] = feedItems.get(j);
    }
    saveItems(itemsToSave);
  }

  @Override
  public void downloadMedia(List<RSSItem> feedItems) {
    for (RSSItem feedItem: feedItems) {
      if (feedItem.getMedia() != null) {
        Stats.get(context).increment(Stats.ACTION_DOWNLOAD);
        feedItem.downloadMedia(context, new MediaDownloadListener.DummyMediaDownloadListener());
      }
    }
  }

  private void saveItems(RSSItem... itemsToSave) {
    new AsyncDbSaveRSSItem(new AsyncCallbackListener.DummyCallback<List<RSSItem>>(),
            context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, itemsToSave);
  }
}
