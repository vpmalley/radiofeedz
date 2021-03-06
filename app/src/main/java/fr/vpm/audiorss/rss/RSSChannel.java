package fr.vpm.audiorss.rss;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.vpm.audiorss.db.AsyncDbSaveRSSChannel;
import fr.vpm.audiorss.db.DbRSSChannel;
import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DateUtils;

/**
 * The representation of a RSS feed
 */
public class RSSChannel implements Parcelable {

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
  public static final String NEXT_REFRESH_KEY = "nextRefresh";

  public static List<RSSChannel> allChannels = new ArrayList<RSSChannel>();

  private static final int SHORT_TITLE_LENGTH = 14;

  /**
   * a Map with item id to item
   */
  private final Map<String, RSSItem> latestItems;

  private long id = -1;

  private String url = "";

  private String title = "";

  private String link = "";

  private String description = "";

  private String lastBuildDate = "";

  private String category = "";

  private Media image = null;

  private ArrayList<String> tags;

  private String nextRefresh;

  /**
   * incomplete if not constructed with all data
   */
  private final boolean incomplete;

  public RSSChannel(String rssUrl) {
    super();
    this.incomplete = true;
    this.latestItems = new HashMap<String, RSSItem>();
    this.tags = new ArrayList<String>();
    this.url = rssUrl;
  }

  public RSSChannel(String rssUrl, String title, String link, String description, String category, Media image) {
    super();
    this.incomplete = false;
    this.latestItems = new HashMap<String, RSSItem>();
    this.tags = new ArrayList<String>();
    this.url = rssUrl;
    this.title = title;
    this.link = link;
    this.description = description;
    this.category = category;
    this.image = image;
  }

  public static RSSChannel fromDbById(long id, Context context){
    RSSChannel channel = null;
    DbRSSChannel dbRSSChannel = new DbRSSChannel(context, false);
    try {
      channel = dbRSSChannel.readById(id, false, false);
    } catch (ParseException e) {
      Log.e("dbIssue", e.getMessage());
    } finally {
      dbRSSChannel.closeDb();
    }
    return channel;
  }

  public void update(String lastBuildDate, Map<String, RSSItem> items) {
    this.lastBuildDate = lastBuildDate;
    int beforeSize = latestItems.size();
    for (Map.Entry<String, RSSItem> item : items.entrySet()) {
      if (!latestItems.containsKey(item.getKey())){
        latestItems.put(item.getKey(), item.getValue());
      }
    }
  }

  public void addTag(String tag) {
    tags.add(tag);
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

  public String getShortenedTitle() {
    String shortTitle = title;
    if ((title != null) && (title.length() > SHORT_TITLE_LENGTH)) {
      shortTitle = title.substring(0, SHORT_TITLE_LENGTH);
      int lastSpace = shortTitle.lastIndexOf(' ');
      if (lastSpace > -1) {
        shortTitle = shortTitle.substring(0, lastSpace);
      }
      shortTitle += " ...";
    }
    return shortTitle;
  }

  public String getLastBuildDate() {
    return lastBuildDate;
  }

  /**
   * Determine whether to refresh this feed
   * @return whether to refresh this feed
   */
  public boolean shouldRefresh() {
    boolean shouldRefresh = true;
    try {
      if (nextRefresh != null) {
        Date whenToRefresh = DateUtils.parseDBDate(nextRefresh);
        shouldRefresh = whenToRefresh.before(Calendar.getInstance().getTime());
        if (shouldRefresh) {
          Calendar twentyMinutesAgo = Calendar.getInstance();
          twentyMinutesAgo.add(Calendar.MINUTE, -20);
          Date lastBuildDate = DateUtils.parseDBDate(getLastBuildDate(), twentyMinutesAgo.getTime());
          shouldRefresh = lastBuildDate.before(twentyMinutesAgo.getTime());
        }
      }
    } catch (ParseException e) {
      Log.w("dateParsing", e.toString());
    }
    return shouldRefresh;
  }

  public String getLink() {
    return link;
  }

  public String getDescription() {
    return description;
  }

  public String getCategory() {
    return category;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getNextRefresh() {
    return nextRefresh;
  }

  public void setNextRefresh(String nextRefresh) {
    this.nextRefresh = nextRefresh;
  }

  public void asyncSaveToDb(Context context) {
    AsyncDbSaveRSSChannel asyncDbUpdater = new AsyncDbSaveRSSChannel(new AsyncCallbackListener.DummyCallback<List<RSSChannel>>(), context);
    asyncDbUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
  }

  public RSSChannel saveToDb(Context context) {
    RSSChannel persistedChannel = null;
    DbRSSChannel dbUpdater = new DbRSSChannel(context, true);
    try {
      RSSChannel existingChannel = dbUpdater.readByUrl(getUrl(), true, true);
      if (existingChannel != null) {
        persistedChannel = dbUpdater.update(existingChannel, this);
      } else { // new channel
        persistedChannel = dbUpdater.add(this);
      }
    } catch (ParseException e) {
      Log.e("DbIssue", "Could not add the feed to the DB");
    } finally {
      dbUpdater.closeDb();
    }
    return persistedChannel;
  }

  public void setImage(Media image) {
    this.image = image;
  }

  public Media getImage() {
    return image;
  }

  public Collection<RSSItem> getItems() {
    return latestItems.values();
  }

  public Map<String, RSSItem> getMappedItems() {
    return Collections.unmodifiableMap(latestItems);
  }

  @Override
  public String toString() {
    return getTitle();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    Bundle b = new Bundle();
    b.putLong("_ID", id);
    b.putString(URL_KEY, url);
    b.putString(TITLE_TAG, title);
    b.putString(LINK_TAG, link);
    b.putString(DESC_TAG, description);
    b.putString(DATE_TAG, lastBuildDate);
    b.putString(CAT_TAG, category);
    b.putParcelable(IMAGE_TAG, image);
    b.putStringArrayList(TAGS_KEY, tags);
    b.putString(NEXT_REFRESH_KEY, nextRefresh);
    parcel.writeBundle(b);
  }

  private RSSChannel(Parcel in) {
    Bundle b = in.readBundle(RSSChannel.class.getClassLoader());
    // without setting the classloader, it fails on BadParcelableException : ClassNotFoundException when
    // unmarshalling Media class
    latestItems = new HashMap<String, RSSItem>();
    id = b.getLong("_ID");
    url = b.getString(URL_KEY);
    title = b.getString(TITLE_TAG);
    link = b.getString(LINK_TAG);
    description = b.getString(DESC_TAG);
    lastBuildDate = b.getString(DATE_TAG);
    category = b.getString(CAT_TAG);
    image = b.getParcelable(IMAGE_TAG);
    tags = b.getStringArrayList(TAGS_KEY);
    nextRefresh = b.getString(NEXT_REFRESH_KEY);
    incomplete = false;
  }

  public static final Parcelable.Creator<RSSChannel> CREATOR
      = new Parcelable.Creator<RSSChannel>() {
    public RSSChannel createFromParcel(Parcel in) {
      return new RSSChannel(in);
    }

    public RSSChannel[] newArray(int size) {
      return new RSSChannel[size];
    }
  };

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    RSSChannel that = (RSSChannel) o;

    return new EqualsBuilder()
        .append(title, that.title)
        .append(link, that.link)
        .append(description, that.description)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(title)
        .append(link)
        .append(description)
        .toHashCode();
  }
}
