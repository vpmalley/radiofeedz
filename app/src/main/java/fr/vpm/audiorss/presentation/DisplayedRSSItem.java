package fr.vpm.audiorss.presentation;

import fr.vpm.audiorss.media.IconDisplay;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 28/03/16.
 */
public class DisplayedRSSItem {

  public enum Media {
    DOWNLOADABLE,
    DOWNLOADED_AUDIO,
    DOWNLOADED_PICTURE,
    NONE
  }

  RSSItem rssItem;

  IconDisplay iconDisplay;

  String feedTitle;

  Media mediaStatus;


  public RSSItem getRssItem() {
    return rssItem;
  }

  public void setRssItem(RSSItem rssItem) {
    this.rssItem = rssItem;
  }

  public IconDisplay getIconDisplay() {
    return iconDisplay;
  }

  public void setIconDisplay(IconDisplay iconDisplay) {
    this.iconDisplay = iconDisplay;
  }

  public String getFeedTitle() {
    return feedTitle;
  }

  public void setFeedTitle(String feedTitle) {
    this.feedTitle = feedTitle;
  }

  public Media getMediaStatus() {
    return mediaStatus;
  }

  public void setMediaStatus(Media mediaStatus) {
    this.mediaStatus = mediaStatus;
  }
}
