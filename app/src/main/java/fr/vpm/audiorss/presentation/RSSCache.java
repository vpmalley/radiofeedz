package fr.vpm.audiorss.presentation;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.vpm.audiorss.media.IconDisplay;
import fr.vpm.audiorss.preferences.MaxItemsPreference;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 28/03/16.
 */
public class RSSCache {

  private Context context;

  List<DisplayedRSSItem> displayedRSSItems = new ArrayList<>();

  String lastBuildDate = "";

  public RSSCache(Context context) {
    this.context = context;
  }

  public List<DisplayedRSSItem> getDisplayedRSSItems() {
    return displayedRSSItems;
  }

  public String getLastBuildDate() {
    return lastBuildDate;
  }

  public void build(List<RSSChannel> feeds, List<RSSItem> feedItems) {
    displayedRSSItems.clear();
    for (RSSItem item : feedItems) {
      for (RSSChannel feed : feeds) {
        if (feed.getTitle().equals(item.getChannelTitle())) {
          DisplayedRSSItem displayedRSSItem = new DisplayedRSSItem();
          displayedRSSItem.setRssItem(item);
          displayedRSSItem.setFeedTitle(feed.getShortenedTitle());
          displayedRSSItem.setIconDisplay(getIconDisplay(item, feed));
          displayedRSSItem.setMediaStatus(getMediaStatus(item));
          displayedRSSItems.add(displayedRSSItem);
        }
      }
    }
    for (RSSChannel feed : feeds) {
      if (lastBuildDate.compareTo(feed.getLastBuildDate()) < 0) {
        lastBuildDate = feed.getLastBuildDate();
      }
    }
    sortAndCapItems();
  }

  private DisplayedRSSItem.Media getMediaStatus(RSSItem item) {
    DisplayedRSSItem.Media status = DisplayedRSSItem.Media.NONE;
    if (item.getMedia() != null) {
      if (item.getMedia().isDownloaded()) {
        if (item.getMedia().isPicture()) {
          status = DisplayedRSSItem.Media.DOWNLOADED_PICTURE;
        } else {
          status = DisplayedRSSItem.Media.DOWNLOADED_AUDIO;
        }
      } else if (!item.getMedia().getMimeType().isEmpty()) {
        status = DisplayedRSSItem.Media.DOWNLOADABLE;
      }
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

  void sortAndCapItems() {
    Collections.sort(displayedRSSItems, new ItemComparator("pubDate DESC"));
    int actualLimit = Math.min(displayedRSSItems.size(), new MaxItemsPreference().get(context));
    List<DisplayedRSSItem> cappedItems = new ArrayList<>(displayedRSSItems.subList(0, actualLimit));
    displayedRSSItems.clear();
    displayedRSSItems.addAll(cappedItems);
  }

}
