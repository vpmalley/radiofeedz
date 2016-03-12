package fr.vpm.audiorss.presentation;

import java.util.List;

import fr.vpm.audiorss.AllFeedItems;
import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.R;
import fr.vpm.audiorss.data.RSSRetriever;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.process.NavigationDrawerList;
import fr.vpm.audiorss.process.NavigationDrawerProvider;
import fr.vpm.audiorss.process.RSSCache;
import fr.vpm.audiorss.process.RSSItemArrayAdapter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 12/03/16.
 */
public class FeedItemsPresenter implements FeedItemsInteraction, FeedItemsPresentation {

  private RSSCache cache = new RSSCache();
  private RSSRetriever rssRetriever;

  private AllFeedItems feedItemsActivity;
  private RSSItemArrayAdapter rssItemAdapter;
  private int rssItemLayout = R.layout.list_rss_item;
  private NavigationDrawerList navigationDrawerList;

  public FeedItemsPresenter(AllFeedItems feedItemsActivity, int rssItemLayout) {
    this.feedItemsActivity = feedItemsActivity;
    this.rssItemLayout = rssItemLayout;
  }

  @Override
  public void loadFeedItems() {
    rssRetriever.retrieveFeedItems();
  }

  @Override
  public void loadFeedItems(List<SelectionFilter> filters) {
    rssRetriever.retrieveFeedItems(filters);
  }

  @Override
  public void retrieveLatestFeedItems() {
    rssRetriever.forceRetrieveFeedItemsFromNetwork();
  }

  @Override
  public void retrieveLatestFeedItems(List<RSSChannel> feedsToRetrieve) {
    rssRetriever.forceRetrieveFeedItemsFromNetwork(feedsToRetrieve);
  }


  @Override
  public void addData(String feedUrl) {
    rssRetriever.addFeed(feedUrl);
  }

  @Override
  public RSSItem getFeedItem(String feedItemGuid) {
    return cache.getFeedItem(feedItemGuid);
  }


  @Override
  public void setChannelsAndBuildModel(List<RSSChannel> feeds) {
    cache.invalidate();
    cache.setFeeds(feeds);
    cache.buildChannelsByItem();
    if (cache.isValid()) {
      displayCachedFeedItems();
    }
  }

  @Override
  public void setItemsAndBuildModel(List<RSSItem> items) {
    cache.invalidate();
    cache.setItems(items);
    cache.buildChannelsByItem();
    if (cache.isValid()) {
      displayCachedFeedItems();
    }
  }

  public synchronized void displayCachedFeedItems() {
    if (rssItemAdapter == null) {
      rssItemAdapter = new RSSItemArrayAdapter(feedItemsActivity, rssItemLayout, cache.getItems(), cache.getChannelsByItem());
      feedItemsActivity.refreshView(rssItemAdapter, getNavigationDrawer());
    } else {
      rssItemAdapter.setItems(cache.getItems());
      rssItemAdapter.setChannelsByItem(cache.getChannelsByItem());
      rssItemAdapter.notifyDataSetChanged();
    }
  }

  public NavigationDrawerProvider getNavigationDrawer() {
    if (navigationDrawerList == null) {
      navigationDrawerList = new NavigationDrawerList(feedItemsActivity, this, new ProgressListener.DummyProgressListener());
    }
    navigationDrawerList.clear();
    navigationDrawerList.addStaticItems();
    navigationDrawerList.addChannels(cache.getFeeds());
    return navigationDrawerList;
  }



}
