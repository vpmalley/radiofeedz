package fr.vpm.audiorss.presentation;

import java.util.List;

import fr.vpm.audiorss.AllFeedItems;
import fr.vpm.audiorss.R;
import fr.vpm.audiorss.interaction.FeedItemsInteraction;
import fr.vpm.audiorss.process.DateUtils;
import fr.vpm.audiorss.process.RSSItemArrayAdapter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 08/04/16.
 */
public class FeedItemsPresenter implements FeedItemsPresentation {

  private final RSSCache rssCache;

  private final AllFeedItems feedItemsActivity;
  private final FeedItemsInteraction feedItemsInteraction;
  private RSSItemArrayAdapter rssItemAdapter;
  private int rssItemLayout = R.layout.list_rss_item;

  public FeedItemsPresenter(AllFeedItems feedItemsActivity, FeedItemsInteraction feedItemsInteraction, int rssItemLayout) {
    this.feedItemsActivity = feedItemsActivity;
    this.feedItemsInteraction = feedItemsInteraction;
    this.rssItemLayout = rssItemLayout;
    this.rssCache = new RSSCache(feedItemsActivity);
  }

  @Override
  public void presentFeeds(List<RSSChannel> feeds, List<RSSItem> feedItems) {
    rssCache.build(feeds, feedItems);
    displayCachedFeedItems();
    feedItemsActivity.refreshNavigationDrawer(feeds);
    presentLastRefreshTime();
  }


  private synchronized void displayCachedFeedItems() {
    if (rssItemAdapter == null) {
      rssItemAdapter = new RSSItemArrayAdapter(feedItemsActivity, rssItemLayout, rssCache.getDisplayedRSSItems(), feedItemsInteraction);
      feedItemsActivity.refreshFeedItems(rssItemAdapter);
    } else {
      rssItemAdapter.notifyDataSetChanged();
    }
  }

  private void presentLastRefreshTime() {
    String displayDate = DateUtils.getDisplayDate(rssCache.getLastBuildDate());
    feedItemsActivity.displayLastRefreshTime(displayDate);
  }

}
