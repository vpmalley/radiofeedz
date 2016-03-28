package fr.vpm.audiorss.presentation;

import java.util.ArrayList;
import java.util.List;

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

  public List<RSSItem> getItems() {
    return items;
  }

  public List<RSSChannel> getFeeds() {
    return feeds;
  }
}
