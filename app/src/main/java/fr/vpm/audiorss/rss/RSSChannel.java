package fr.vpm.audiorss.rss;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.db.AsyncDbSaveRSSChannel;
import fr.vpm.audiorss.db.LoadDataRefreshViewCallback;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.media.AsyncPictureLoader;
import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.media.PictureLoadedListener;
import fr.vpm.audiorss.persistence.FilePictureSaver;

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

  public static final String RSS_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss ZZZZZ";
  public static final String DISPLAY_PATTERN = "EEEE, dd MMMM yyyy - HH:mm";
  public static final String DB_DATE_PATTERN = "yyyy-MM-dd-HH:mm:ss-ZZZZZ"; // should allow sorting date

  public static List<RSSChannel> allChannels = new ArrayList<RSSChannel>();

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

  public RSSChannel(String rssUrl, String title, String link, String description, String category, Media image) {
    super();
    Log.d("RSSChannel", "creating " + rssUrl);
    this.latestItems = new HashMap<String, RSSItem>();
    this.tags = new ArrayList<String>();
    this.url = rssUrl;
    this.title = title;
    this.link = link;
    this.description = description;
    this.category = category;
    this.image = image;
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
    boolean result = true;
    if (o instanceof RSSChannel) {
      RSSChannel channel = (RSSChannel) o;
      result = this.title.equals(channel.title);
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

  public void saveToDb(ProgressListener progressListener, FeedsActivity<List<RSSChannel>> activity) throws ParseException {
    LoadDataRefreshViewCallback callback = new LoadDataRefreshViewCallback(progressListener, activity);
    AsyncDbSaveRSSChannel asyncDbUpdater = new AsyncDbSaveRSSChannel(callback, activity.getContext());
    asyncDbUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
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

  public Bitmap getBitmap(Context context, List<PictureLoadedListener> pictureLoadedListeners){
    Bitmap b = null;
    FilePictureSaver pictureRetriever = new FilePictureSaver(context);
    if (getImage() != null){
      if (pictureRetriever.exists(getImage().getName())){
        b = pictureRetriever.retrieve(getImage().getName());
      } else if (new DefaultNetworkChecker().checkNetwork(context)) {
        AsyncPictureLoader pictureLoader = new AsyncPictureLoader(pictureLoadedListeners, 300, 200, context);
        pictureLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getImage());
      }
    }
    return b;
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
}
