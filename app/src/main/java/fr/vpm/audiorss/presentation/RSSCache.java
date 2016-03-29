package fr.vpm.audiorss.presentation;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import fr.vpm.audiorss.media.IconDisplay;
import fr.vpm.audiorss.process.ItemComparator;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 28/03/16.
 */
public class RSSCache {

  List<DisplayedRSSItem> displayedRSSItems = new ArrayList<>();

  String lastBuildDate = "";

  public List<DisplayedRSSItem> getDisplayedRSSItems() {
    return displayedRSSItems;
  }

  public String getLastBuildDate() {
    return lastBuildDate;
  }

  public void buildFromDBCache(DBCache dbCache) {
    displayedRSSItems.clear();
    for (RSSItem item : dbCache.getItems()) {
      for (RSSChannel feed : dbCache.getFeeds()) {
        if (feed.getId() == item.getChannelId()) {
          DisplayedRSSItem displayedRSSItem = new DisplayedRSSItem();
          displayedRSSItem.setRssItem(item);
          displayedRSSItem.setFeedTitle(feed.getShortenedTitle());
          displayedRSSItem.setIconDisplay(getIconDisplay(item, feed));
          displayedRSSItem.setMediaStatus(getMediaStatus(item));
          displayedRSSItems.add(displayedRSSItem);
        }
      }
    }
    for (RSSChannel feed : dbCache.getFeeds()) {
      if (lastBuildDate.compareTo(feed.getLastBuildDate()) < 0) {
        lastBuildDate = feed.getLastBuildDate();
      }
    }
  }

  public void buildFromFeeds(List<RSSChannel> newFeeds, Context context) {
    for (RSSChannel feed: newFeeds) {
      for (RSSItem item : feed.getItems()) {
        DisplayedRSSItem displayedRSSItem = new DisplayedRSSItem();
        displayedRSSItem.setRssItem(item);
        displayedRSSItem.setFeedTitle(feed.getShortenedTitle());
        displayedRSSItem.setIconDisplay(getIconDisplay(item, feed));
        displayedRSSItem.setMediaStatus(getMediaStatus(item));
        displayedRSSItems.add(displayedRSSItem);
      }
      if (lastBuildDate.compareTo(feed.getLastBuildDate()) < 0) {
        lastBuildDate = feed.getLastBuildDate();
      }
    }
    sortAndCapItems(context);
  }

  private DisplayedRSSItem.Media getMediaStatus(RSSItem item) {
    DisplayedRSSItem.Media status;
    if (item.getMedia() != null) {
      if (item.getMedia().isDownloaded()) {
        if (item.getMedia().isPicture()) {
          status = DisplayedRSSItem.Media.DOWNLOADED_PICTURE;
        } else {
          status = DisplayedRSSItem.Media.DOWNLOADED_AUDIO;
        }
      } else {
        status = DisplayedRSSItem.Media.DOWNLOADABLE;
      }
    } else {
      status = DisplayedRSSItem.Media.NONE;
    }
    return status;
  }

  public IconDisplay getIconDisplay(RSSItem rssItem, RSSChannel rssChannel) {
    IconDisplay iconDisplay = null;
    if ((rssItem.getMedia() != null) && (rssItem.getMedia().isPicture()) &&
        (rssItem.getMedia().getDistantUrl() != null) && (!rssItem.getMedia().getDistantUrl().isEmpty())){
      iconDisplay = new IconDisplay(rssItem.getMedia());
    } else if ((rssChannel != null) && (rssChannel.getImage() != null) &&
        (rssChannel.getImage().getDistantUrl() != null) && (!rssChannel.getImage().getDistantUrl().isEmpty())){
      iconDisplay = new IconDisplay(rssChannel.getImage());
    }
    return iconDisplay;
  }

  void sortAndCapItems(Context context) {
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    String ordering = sharedPrefs.getString("pref_feed_ordering", "pubDate DESC");
    Collections.sort(displayedRSSItems, new ItemComparator(ordering));
    String limit = sharedPrefs.getString("pref_disp_max_items", "80");
    if (limit == null || !Pattern.compile("\\d+").matcher(limit).matches()){
      limit = "80";
    }
    int actualLimit = Math.min(displayedRSSItems.size(), Integer.valueOf(limit));
    List<DisplayedRSSItem> cappedItems = new ArrayList<>(displayedRSSItems.subList(0, actualLimit));
    displayedRSSItems.clear();
    displayedRSSItems.addAll(cappedItems);
  }

}
