package fr.vpm.audiorss.rss;


import android.app.DownloadManager;
import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.vpm.audiorss.media.Media;

public class RSSItem implements Serializable, Comparable<RSSItem> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public static final String TITLE_TAG = "title";
  public static final String LINK_TAG = "link";
  public static final String DESC_TAG = "description";
  public static final String AUTHOR_TAG = "author";
  public static final String CAT_TAG = "category";
  public static final String COMMENTS_TAG = "comments";
  public static final String ENC_TAG = "enclosure";
  public static final String GUID_TAG = "guid";
  public static final String DATE_TAG = "pubDate";

  public static final String MEDIA_KEY = "media";
  public static final String LOCAL_MEDIA_KEY = "localMedia";
  public static final String CHANNELTITLE_KEY = "channelTitle";
  public static final String MEDIA_ID_KEY = "mediaId";
  public static final String READ_KEY = "isRead";

  private long dbId = -1;

  private String channelTitle = "";

  private String title = "";

  private String link = "";

  private String description = "";

  private String authorAddress = "";

  private String category = "";

  private String comments = "";

  private String mediaUrl = "";

  private String guid = "";

  private String pubDate = "";

  private boolean isRead = false;

  private Media media = null;

  private static final String PREF_FEED_ORDERING = "pref_feed_ordering";

  public String getId() {
    return guid;
  }

  public RSSItem(String feedTitle, String title, String link, String description,
                 String authorAddress, String category, String comments, Media media, String guid,
                 String pubDate, boolean isRead) {
    super();
    this.channelTitle = feedTitle;
    this.title = title;
    this.link = link;
    this.description = description;
    this.authorAddress = authorAddress;
    this.category = category;
    this.comments = comments;
    this.guid = guid;
    this.pubDate = pubDate;
    this.media = media;
    this.isRead = isRead;
  }

  public String getDate() {
    return pubDate;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getMediaUrl() {
    return mediaUrl;
  }

  public String getLink() {
    return link;
  }

  public long getDbId() {
    return dbId;
  }

  public void setDbId(long dbId) {
    this.dbId = dbId;
  }

  public String getChannelTitle() {
    return channelTitle;
  }

  public void setChannelTitle(String channelTitle) {
    this.channelTitle = channelTitle;
  }

  public String getAuthorAddress() {
    return authorAddress;
  }

  public void setAuthorAddress(String authorAddress) {
    this.authorAddress = authorAddress;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getGuid() {
    return guid;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public String getPubDate() {
    return pubDate;
  }

  public void setPubDate(String pubDate) {
    this.pubDate = pubDate;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setMediaUrl(String mediaUrl) {
    this.mediaUrl = mediaUrl;
  }

  public boolean isRead(){
    return isRead;
  }

  public void setRead(boolean isRead){
    this.isRead = isRead;
  }

  public void downloadMedia(Context context) {
    if (media == null) {
      Log.w("media", "media is not expected to be null here");
      media = new Media("emission " + channelTitle + "-" + title, channelTitle, mediaUrl, "");
    }
    media.download(context, DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
  }

  public Media getMedia() {
    return media;
  }

  public void setMedia(Media media) {
    this.media = media;
  }

  @Override
  public int compareTo(RSSItem other) {
    RSSItem lhs = this;
    RSSItem rhs = (RSSItem) other;
    //SharedPreferences sharedPref = PreferenceManager
    //      .getDefaultSharedPreferences(FeedsActivity.this);
    //String ordering = sharedPref.getString(PREF_FEED_ORDERING, "reverse_time");
    String ordering = "reverse_time";

    int comparison = 0;
    Date lhsDate = null;
    Date rhsDate = null;
    try {
      lhsDate = new SimpleDateFormat(RSSChannel.DB_DATE_PATTERN, Locale.US).parse(lhs.getDate());
      rhsDate = new SimpleDateFormat(RSSChannel.DB_DATE_PATTERN, Locale.US).parse(rhs.getDate());
    } catch (ParseException e) {
      Log.e("Exception", e.toString());
    }

    // int comparisonByDate = lhs.getDate().compareTo(rhs.getDate());
    int comparisonByDate = 0;
    if ((lhsDate != null) && (rhsDate != null)) {
      comparisonByDate = lhsDate.compareTo(rhsDate);
    }
    int comparisonByName = lhs.getTitle().compareTo(rhs.getTitle());

    if (ordering.contains("alpha")) {
      comparison = comparisonByName;
    } else {
      comparison = comparisonByDate;
    }

    int factor = 1;
    if (ordering.contains("reverse")) {
      factor = -1;
    }

    if (comparison == 0) {
      comparison = comparisonByName + comparisonByDate;
    }

    return factor * comparison;

  }

  @Override
  public String toString() {
    return this.channelTitle + " : " + this.title;
  }

}
