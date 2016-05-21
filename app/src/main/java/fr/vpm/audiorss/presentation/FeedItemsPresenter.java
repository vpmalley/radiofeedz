package fr.vpm.audiorss.presentation;

import android.widget.ArrayAdapter;

import java.util.List;

import fr.vpm.audiorss.AllFeedItems;
import fr.vpm.audiorss.R;
import fr.vpm.audiorss.adapter.NavigationDrawer;
import fr.vpm.audiorss.adapter.NavigationDrawerItem;
import fr.vpm.audiorss.adapter.RSSItemArrayAdapter;
import fr.vpm.audiorss.interaction.FeedItemsInteraction;
import fr.vpm.audiorss.process.DateUtils;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 08/04/16.
 */
public class FeedItemsPresenter implements FeedItemsPresentation {

  private final RSSCache rssCache;
  private final NavigationDrawer navigationDrawer;

  private final AllFeedItems feedItemsActivity;
  private final FeedItemsInteraction feedItemsInteraction;
  private RSSItemArrayAdapter rssItemAdapter;
  private ArrayAdapter<NavigationDrawerItem> rssChannelAdapter;
  private int rssItemLayout = R.layout.list_rss_item;

  public FeedItemsPresenter(AllFeedItems feedItemsActivity, FeedItemsInteraction feedItemsInteraction, int rssItemLayout) {
    this.feedItemsActivity = feedItemsActivity;
    this.feedItemsInteraction = feedItemsInteraction;
    this.rssItemLayout = rssItemLayout;
    this.rssCache = new RSSCache(feedItemsActivity);
    this.navigationDrawer = new NavigationDrawer(feedItemsActivity, feedItemsInteraction);
  }

  @Override
  public void presentFeeds(List<RSSChannel> feeds, List<RSSItem> feedItems) {
    rssCache.build(feeds, feedItems);
    displayCachedFeedItems();
    displayCachedFeeds(feeds);
    displayLastRefreshTime();
    feedItemsActivity.stopRefreshProgress();
  }

  private synchronized void displayCachedFeeds(List<RSSChannel> feeds) {
    boolean refreshNavigationDrawer = (rssChannelAdapter == null);
    rssChannelAdapter = navigationDrawer.setChannelsAndGetAdapter(feeds);
    if (refreshNavigationDrawer) {
      feedItemsActivity.refreshNavigationDrawer(rssChannelAdapter);
    }
  }

  private synchronized void displayCachedFeedItems() {
    if (rssItemAdapter == null) {
      rssItemAdapter = new RSSItemArrayAdapter(feedItemsActivity, rssItemLayout, rssCache.getDisplayedRSSItems(), feedItemsInteraction);
      feedItemsActivity.refreshFeedItems(rssItemAdapter);
    } else {
      rssItemAdapter.notifyDataSetChanged();
      feedItemsActivity.resetFeedItemsListSelection();
    }
    if (rssCache.getDisplayedRSSItems().isEmpty()) {
      feedItemsActivity.displayShowcase();
    }
  }

  private void displayLastRefreshTime() {
    String displayDate = DateUtils.getDisplayDate(rssCache.getLastBuildDate());
    feedItemsActivity.displayLastRefreshTime(displayDate);
  }

}
