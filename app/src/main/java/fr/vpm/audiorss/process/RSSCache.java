package fr.vpm.audiorss.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.vpm.audiorss.db.filter.ChannelFilter;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 12/03/16.
 */
public class RSSCache {

  /**
   * the items of feeds that are displayed
   */
  List<RSSItem> items;

  List<RSSChannel> feeds;

  ArrayList<SelectionFilter> itemFilters;

  Map<RSSItem, RSSChannel> channelsByItem;

  private static RSSCache instance;
  private boolean itemsAreSet;
  private boolean feedsAreSet;

  public RSSCache() {
    items = new ArrayList<>();
    feeds = new ArrayList<>();
    itemFilters = new ArrayList<>();
    channelsByItem = new HashMap<>();
  }

  public static RSSCache getInstance() {
    if (instance == null) {
      instance = new RSSCache();
    }
    return instance;
  }

  /**
   * Determines whether the other list of filters is similar to this instance's filters
   *
   * @param otherFilters other filters to compare with this instance's filters
   * @return whether the filters are the same
   */
  public boolean hasSimilarFilters(List<SelectionFilter> otherFilters) {
    Set<String> filterNames = getFilterNames(this.itemFilters);
    Set<String> otherFilterNames = getFilterNames(otherFilters);
    return filterNames.equals(otherFilterNames);
  }

  private Set<String> getFilterNames(List<SelectionFilter> filters) {
    Set<String> filterNames = new HashSet<>();
    for (SelectionFilter filter : filters) {
      if (filter instanceof ChannelFilter) {
        filterNames.add(filter.getClass().getName() + ",id=" + filter.getSelectionValues()[0]);
      } else {
        filterNames.add(filter.getClass().getName());
      }
    }
    return filterNames;
  }

  public RSSItem getFeedItem(String feedItemGuid) {
    int position = 0;
    while ((position < items.size()) && (!feedItemGuid.equals(items.get(position).getGuid()))){
      position++;
    }
    if ((position >= items.size()) || (!feedItemGuid.equals(items.get(position).getGuid()))) {
      position = -1;
    }
    return items.get(position);
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

  public void buildChannelsByItem(){
    if (isValid()) {
      channelsByItem.clear();
      for (RSSItem item : items) {
        for (RSSChannel channel : feeds) {
          if (channel.getId() == item.getChannelId()) {
            channelsByItem.put(item, channel);
          }
        }
      }
    }
  }

  public List<RSSItem> getItems() {
    return items;
  }

  public Map<RSSItem, RSSChannel> getChannelsByItem() {
    return channelsByItem;
  }

  public List<RSSChannel> getFeeds() {
    return feeds;
  }
}
