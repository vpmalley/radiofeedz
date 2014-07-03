package fr.vpm.audiorss.rss;

import java.io.Serializable;

import android.content.Context;
import fr.vpm.audiorss.media.Media;

public class RSSItem implements Serializable {

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

  long dbId = -1;

  String channelTitle = "";

  String title = "";

  String link = "";

  String description = "";

  String authorAddress = "";

  String category = "";

  String comments = "";

  String mediaUrl = "";

  String guid = "";

  String pubDate = "";

  Media media = null;

  public String getId() {
    return guid;
  }

  public RSSItem(String feedTitle, String title, String link, String description,
      String authorAddress, String category, String comments, String mediaUrl, String guid,
      String pubDate) {
    super();
    this.channelTitle = feedTitle;
    this.title = title;
    this.link = link;
    this.description = description;
    this.authorAddress = authorAddress;
    this.category = category;
    this.comments = comments;
    this.mediaUrl = mediaUrl;
    this.guid = guid;
    this.pubDate = pubDate;
    this.media = new Media(channelTitle, title, mediaUrl);
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

  public void downloadMedia(Context context) {
    media.download(context);
  }

  public Media getMedia() {
    return media;
  }

  public void setMedia(Media media) {
    this.media = media;
  }

  @Override
  public String toString() {
    return this.channelTitle + " : " + this.title;
  }

}
