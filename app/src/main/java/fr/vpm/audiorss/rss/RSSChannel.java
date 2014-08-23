package fr.vpm.audiorss.rss;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.DownloadManager;
import android.content.Context;
import android.util.Log;
import fr.vpm.audiorss.db.DbRSSChannel;
import fr.vpm.audiorss.media.Media;

public class RSSChannel implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static final String TITLE_TAG = "title";
  public static final String LINK_TAG = "link";
  public static final String DESC_TAG = "description";
  public static final String DATE_TAG = "lastBuildDate";
  public static final String CAT_TAG = "category";
  public static final String IMAGE_TAG = "image";
  public static final String LOCAL_IMAGE_TAG = "localImage";
  public static final String IMAGE_ID_TAG = "imageId";

  public static final String URL_KEY = "url";
  public static final String TAGS_KEY = "tags";

  public static final String DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss ZZZZZ";

  public static List<RSSChannel> allChannels = new ArrayList<RSSChannel>();

  /**
   * a Map with item id to item
   */
  Map<String, RSSItem> latestItems;

  long id = -1;

  String url = "";

  String title = "";

  String link = "";

  String description = "";

  String lastBuildDate = "";

  String category = "";

  Media image = null;

  List<String> tags;

  public RSSChannel(String rssUrl, String title, String link, String description, String category,
      String imageUrl) {
    super();
    Log.d("RSSChannel", "creating " + rssUrl);
    latestItems = new HashMap<String, RSSItem>();
    tags = new ArrayList<String>();
    this.url = rssUrl;
    this.title = title;
    this.link = link;
    this.description = description;
    this.category = category;
    this.image = new Media(title, "media-miniature", imageUrl);
  }

  public void update(String lastBuildDate, Map<String, RSSItem> items) {
    this.lastBuildDate = lastBuildDate;
    int beforeSize = latestItems.size();
    latestItems.putAll(items);
    Log.d("RSSChannel", "update channel " + getUrl() + " at " + lastBuildDate + "from "
        + beforeSize + " items to " + latestItems.size());
  }

  public void addTag(String tag) {
    tags.add(tag);
  }

  @Override
  public boolean equals(Object o) {
    boolean result = false;
    if (o instanceof RSSChannel) {
      RSSChannel channel = (RSSChannel) o;
      result &= this.title.equals(channel.title);
      result &= this.link.equals(channel.link);
      result &= this.description.equals(channel.description);
    } else {
      result = super.equals(o);
    }
    return result;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public String getTitle() {
    return title;
  }

  public String getLastBuildDate() {
    return lastBuildDate;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setLastBuildDate(String lastBuildDate) {
    this.lastBuildDate = lastBuildDate;
  }

  public void saveToDb(Context context) throws ParseException {
    DbRSSChannel dbUpdater = new DbRSSChannel(context);
    RSSChannel existingChannel = dbUpdater.readByUrl(getUrl());
    if (existingChannel != null) {
      dbUpdater.update(existingChannel, this);
    } else {
      downloadImage(context);
      dbUpdater.add(this);
    }
    dbUpdater.closeDb();
  }

  public void setImage(Media image) {
    this.image = image;
  }

  public Media getImage() {
    return image;
  }

  public void downloadImage(Context context) {
    image.download(context, DownloadManager.Request.VISIBILITY_HIDDEN);
  }

  public Collection<RSSItem> getItems() {
    return latestItems.values();
  }

  public Map<String, RSSItem> getMappedItems() {
    return Collections.unmodifiableMap(latestItems);
  }
}
