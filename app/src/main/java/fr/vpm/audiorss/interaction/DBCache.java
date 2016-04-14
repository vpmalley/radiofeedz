package fr.vpm.audiorss.interaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 12/03/16.
 */
public class DBCache {

  /**
   * the items of feeds that are displayed
   */
  List<RSSItem> items;

  List<RSSChannel> feeds;

  private boolean itemsAreSet;
  private boolean feedsAreSet;

  public DBCache() {
    items = new ArrayList<>();
    feeds = new ArrayList<>();
  }

  public void empty() {
    this.feeds.clear();
    this.items.clear();
  }

  public void invalidate() {
    itemsAreSet = false;
    feedsAreSet = false;
  }

  public boolean isValid() {
    return itemsAreSet && feedsAreSet;
  }

  public void setFeeds(List<RSSChannel> feeds) {
    this.feeds = feeds;
    feedsAreSet = true;
  }

  public void setItems(List<RSSItem> items) {
    this.items = items;
    itemsAreSet = true;
  }

  public void addNewFeeds(List<RSSChannel> newFeeds) {
    Set<RSSChannel> uniqueFeeds = new HashSet<>();
    uniqueFeeds.addAll(this.feeds);
    uniqueFeeds.addAll(newFeeds);
    this.feeds.clear();
    this.feeds.addAll(uniqueFeeds);

    addNewFeedItemsFromFeeds(newFeeds);
  }

  private void addNewFeedItemsFromFeeds(List<RSSChannel> newFeeds) {
    Set<RSSItem> uniqueItems = new HashSet<>();
    uniqueItems.addAll(this.items);
    for (RSSChannel feed : newFeeds) {
      uniqueItems.addAll(feed.getItems());
    }
    this.items.clear();
    this.items.addAll(uniqueItems);
  }

  public List<RSSItem> getItems() {
    return items;
  }

  public List<RSSChannel> getFeeds() {
    return feeds;
  }
}
