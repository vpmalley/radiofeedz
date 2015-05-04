package fr.vpm.audiorss.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import fr.vpm.audiorss.db.filter.ChannelFilter;
import fr.vpm.audiorss.db.filter.ConjunctionFilter;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class DbRSSChannel implements DbItem<RSSChannel> {

  private static final String COMMA = ",";
  final static String T_RSS_CHANNEL = "rsschannel";
  final static String[] COLS_RSS_CHANNEL = {DatabaseOpenHelper._ID, RSSChannel.CAT_TAG,
          RSSChannel.DATE_TAG, RSSChannel.DESC_TAG, RSSChannel.IMAGE_TAG, RSSChannel.LINK_TAG, RSSChannel.IMAGE_ID_TAG,
          RSSChannel.LOCAL_IMAGE_TAG, RSSChannel.TAGS_KEY, RSSChannel.TITLE_TAG, RSSChannel.URL_KEY, RSSChannel.NEXT_REFRESH_KEY};

  final static String T_CREATE_RSS_CHANNEL = "CREATE TABLE " + T_RSS_CHANNEL + " ("
          + DatabaseOpenHelper._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + RSSChannel.CAT_TAG
          + DatabaseOpenHelper.TEXT_COLUMN + COMMA + RSSChannel.DATE_TAG + " TEXT" + COMMA
          + RSSChannel.DESC_TAG + DatabaseOpenHelper.TEXT_COLUMN + COMMA + RSSChannel.IMAGE_TAG
          + " TEXT" + COMMA + RSSChannel.LINK_TAG
          + DatabaseOpenHelper.TEXT_COLUMN + COMMA + RSSChannel.IMAGE_ID_TAG
          + " INTEGER" + COMMA + RSSChannel.LOCAL_IMAGE_TAG
          + " TEXT" + COMMA + RSSChannel.TAGS_KEY
          + DatabaseOpenHelper.TEXT_COLUMN + COMMA + RSSChannel.TITLE_TAG
          + DatabaseOpenHelper.TEXT_COLUMN + COMMA + RSSChannel.URL_KEY
          + DatabaseOpenHelper.TEXT_COLUMN + COMMA + RSSChannel.NEXT_REFRESH_KEY
          + DatabaseOpenHelper.TEXT_COLUMN + ")";


  private SQLiteDatabase mDb;

  private final SharedPreferences sharedPrefs;

  public DbRSSChannel(Context context, boolean writable) {
    if (writable){
      mDb = DatabaseOpenHelper.getInstance(context).getWritableDatabase();
    } else {
      mDb = DatabaseOpenHelper.getInstance(context).getReadableDatabase();
    }
    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
  }

  @Override
  public RSSChannel readById(long id, boolean readItems, boolean readAllItems) throws ParseException {
    RSSChannel channel = null;
    Cursor c = mDb.query(T_RSS_CHANNEL, COLS_RSS_CHANNEL, DatabaseOpenHelper._ID + "=?",
            new String[]{String.valueOf(id)}, null, null, null);
    if (c.getCount() > 0) {
      c.moveToFirst();
      channel = channelFromCursorEntry(c);
      if (readItems) {
        Map<String, RSSItem> items = readItemsByChannelId(id, readAllItems, new ArrayList<SelectionFilter>());
        channel.update(channel.getLastBuildDate(), items);
      }
    }
    c.close();
    return channel;
  }

  public RSSChannel readByUrl(String url, boolean readItems, boolean readAllItems) throws ParseException {
    RSSChannel channel = null;
    Cursor c = mDb.query(T_RSS_CHANNEL, COLS_RSS_CHANNEL, RSSChannel.URL_KEY + "=?",
            new String[]{url}, null, null, null);
    if (c.getCount() > 0) {
      c.moveToFirst();
      channel = channelFromCursorEntry(c);
      if (readItems) {
        Map<String, RSSItem> items = readItemsByChannelId(channel.getId(), readAllItems, new ArrayList<SelectionFilter>());
        channel.update(channel.getLastBuildDate(), items);
      }
    }
    c.close();
    return channel;
  }

  public List<RSSChannel> readAll(boolean readItems) throws ParseException {
    Cursor c = mDb.query(T_RSS_CHANNEL, COLS_RSS_CHANNEL, null, null, null, null,
        RSSChannel.TITLE_TAG);
    List<RSSChannel> channels = new ArrayList<RSSChannel>();
    c.moveToFirst();
    for (int i = 0; i < c.getCount(); i++) {
      long id = c.getLong(c.getColumnIndex(DatabaseOpenHelper._ID));
      RSSChannel channel = channelFromCursorEntry(c);
      if (readItems) {
        Map<String, RSSItem> items = readItemsByChannelId(id, false, new ArrayList<SelectionFilter>());
        channel.update(channel.getLastBuildDate(), items);
      }
      channels.add(channel);
      c.moveToNext();
    }
    c.close();
    return channels;
  }

  public RSSChannel add(RSSChannel channel) throws ParseException {
    ContentValues channelValues = createContentValues(channel);
    long id = mDb.insert(T_RSS_CHANNEL, null, channelValues);
    channel.setId(id);

    bulkAddOrUpdateItems(channel);
    return channel;
  }

  public RSSChannel update(RSSChannel existingChannel, RSSChannel channel) throws ParseException {
    existingChannel.update(channel.getLastBuildDate(), channel.getMappedItems());
    existingChannel.setNextRefresh(channel.getNextRefresh());
    if (existingChannel.getImage() == null){
      existingChannel.setImage(channel.getImage());
    }
    ContentValues channelValues = createContentValues(existingChannel);
    channelValues.put(DatabaseOpenHelper._ID, existingChannel.getId());
    mDb.update(T_RSS_CHANNEL, channelValues, DatabaseOpenHelper._ID + "=?",
            new String[]{String.valueOf(existingChannel.getId())});

    bulkAddOrUpdateItems(existingChannel);
    return existingChannel;
  }

  /**
   * Wraps into a transaction the items insertions
   * @param channel the channel for which to insert or update the items
   */
  private void bulkAddOrUpdateItems(RSSChannel channel) {
    try {
      mDb.beginTransaction();
      for (RSSItem item : channel.getItems()) {
        if (item.getDbId() <= -1) {
          addOrUpdate(item, channel.getId());
        }
      }
      mDb.setTransactionSuccessful();
    } finally {
      mDb.endTransaction();
    }
  }

  public ContentValues createContentValues(RSSChannel channel) {
    ContentValues channelValues = new ContentValues();
    channelValues.put(RSSChannel.CAT_TAG, channel.getCategory());
    channelValues.put(RSSChannel.DATE_TAG, channel.getLastBuildDate());
    channelValues.put(RSSChannel.DESC_TAG, channel.getDescription());
    channelValues.put(RSSChannel.LINK_TAG, channel.getLink());
    if (channel.getImage() != null) {
      Media newImage;
      if (channel.getImage().getId() > -1){
        newImage = new DbMedia(mDb).update(channel.getImage());
      } else {
        newImage = new DbMedia(mDb).add(channel.getImage());
      }
      channelValues.put(RSSChannel.IMAGE_ID_TAG, newImage.getId());
    }
    channelValues.put(RSSChannel.TAGS_KEY, TextUtils.join(COMMA, channel.getTags()));
    channelValues.put(RSSChannel.TITLE_TAG, channel.getTitle());
    channelValues.put(RSSChannel.URL_KEY, channel.getUrl());
    channelValues.put(RSSChannel.NEXT_REFRESH_KEY, channel.getNextRefresh());
    return channelValues;
  }

  public RSSItem addOrUpdate(RSSItem item, long channelId) {
    ContentValues itemValues = new ContentValues();
    itemValues.put(RSSItem.AUTHOR_TAG, item.getAuthorAddress());
    itemValues.put(RSSItem.CAT_TAG, item.getCategory());
    itemValues.put(RSSItem.CHANNELTITLE_KEY, item.getChannelTitle());
    itemValues.put(RSSItem.COMMENTS_TAG, item.getComments());
    itemValues.put(RSSItem.DATE_TAG, item.getDate());
    itemValues.put(RSSItem.DESC_TAG, item.getDescription());
    itemValues.put(RSSItem.GUID_TAG, item.getGuid());
    itemValues.put(RSSItem.LINK_TAG, item.getLink());
    itemValues.put(RSSItem.LOCAL_MEDIA_KEY, "");
    itemValues.put(RSSItem.READ_KEY, item.isRead());
    itemValues.put(RSSItem.ARCHIVED_KEY, item.isArchived());
    itemValues.put(RSSItem.MEDIA_KEY, item.getMediaUrl());
    if (item.getMedia() != null) {
      Media newMedia;
      if (item.getMedia().getId() > -1){
        newMedia = new DbMedia(mDb).update(item.getMedia());
      } else {
        newMedia = new DbMedia(mDb).add(item.getMedia());
      }
      itemValues.put(RSSItem.MEDIA_ID_KEY, newMedia.getId());
      item.setMedia(newMedia);
    }

    itemValues.put(RSSItem.TITLE_TAG, item.getTitle());
    if (channelId > -1) {
      itemValues.put(DatabaseOpenHelper.CHANNEL_ID_KEY, channelId);
    }
    if (item.getDbId() > -1) {
      itemValues.put(DatabaseOpenHelper._ID, item.getDbId());

      mDb.update(DatabaseOpenHelper.T_RSS_ITEM, itemValues, DatabaseOpenHelper._ID + "=?",
              new String[]{String.valueOf(item.getDbId())});
    } else {
      long id = mDb.insert(DatabaseOpenHelper.T_RSS_ITEM, null, itemValues);
      item.setDbId(id);
    }
    return item;
  }

  public void deleteById(long id) {
    mDb.delete(T_RSS_CHANNEL, DatabaseOpenHelper._ID + "=?", new String[]{String.valueOf(id)});
    mDb.delete(DatabaseOpenHelper.T_RSS_ITEM, DatabaseOpenHelper.CHANNEL_ID_KEY + "=?",
            new String[]{String.valueOf(id)});
  }

  RSSChannel channelFromCursorEntry(Cursor c) throws ParseException {
    String category = c.getString(c.getColumnIndex(RSSChannel.CAT_TAG));
    String lastBuildDate = c.getString(c.getColumnIndex(RSSChannel.DATE_TAG));
    String description = c.getString(c.getColumnIndex(RSSChannel.DESC_TAG));
    String link = c.getString(c.getColumnIndex(RSSChannel.LINK_TAG));
    long imageId = c.getLong(c.getColumnIndex(RSSChannel.IMAGE_ID_TAG));
    Media image = new DbMedia(mDb).readById(imageId);
    String tags = c.getString(c.getColumnIndex(RSSChannel.TAGS_KEY));
    String title = c.getString(c.getColumnIndex(RSSChannel.TITLE_TAG));
    String url = c.getString(c.getColumnIndex(RSSChannel.URL_KEY));
    String nextRefresh = c.getString(c.getColumnIndex(RSSChannel.NEXT_REFRESH_KEY));
    RSSChannel channel = new RSSChannel(url, title, link, description, category, image);
    channel.setNextRefresh(nextRefresh);
    String[] tagList = tags.split(COMMA);
    for (String tag : tagList) {
      channel.addTag(tag);
    }
    channel.setId(c.getLong(c.getColumnIndex(DatabaseOpenHelper._ID)));
    channel.update(lastBuildDate, new HashMap<String, RSSItem>());
    return channel;
  }

  Map<String, RSSItem> readItemsByChannelId(long id, boolean readAllItems, List<SelectionFilter> filters) throws ParseException {
    filters.add(new ChannelFilter(id));
    return readItems(readAllItems, filters);
  }

  Map<String, RSSItem> readItems(boolean readAll, List<SelectionFilter> filters) throws ParseException {
    Map<String, RSSItem> items = new HashMap<String, RSSItem>();
    Cursor itemsC = readItemsAsCursor(readAll, filters);
    if (itemsC.getCount() > 0) {
      itemsC.moveToFirst();
      for (int i = 0; i < itemsC.getCount(); i++) {
        RSSItem item = itemFromCursor(itemsC);
        items.put(item.getId(), item);
        itemsC.moveToNext();
      }
    }
    itemsC.close();
    return items;
  }

  List<RSSItem> readItemsAsList(boolean readAll, List<SelectionFilter> filters) throws ParseException {
    List<RSSItem> items = new ArrayList<>();
    Cursor itemsC = readItemsAsCursor(readAll, filters);
    if (itemsC.getCount() > 0) {
      itemsC.moveToFirst();
      for (int i = 0; i < itemsC.getCount(); i++) {
        items.add(itemFromCursor(itemsC));
        itemsC.moveToNext();
      }
    }
    itemsC.close();
    return items;
  }

  private Cursor readItemsAsCursor(boolean readAll, List<SelectionFilter> filters) {
    ConjunctionFilter filter = new ConjunctionFilter(filters);
    String limit = "10000"; // this is a high limit to avoid OutOfMemoryError
    if (!readAll) {
      limit = sharedPrefs.getString("pref_disp_max_items", "80");
      if (!Pattern.compile("\\d+").matcher(limit).matches()){
        limit = "80";
      }
    }
    String ordering = sharedPrefs.getString("pref_feed_ordering", "pubDate DESC");
    return queryItems(filter, limit, ordering);
  }

  private Cursor queryItems(ConjunctionFilter filter, String limit, String ordering) {
    StringBuilder rawQueryBuilder = new StringBuilder();
    rawQueryBuilder.append("SELECT ");
    rawQueryBuilder.append(StringUtils.join(DatabaseOpenHelper.COLS_RSS_ITEM, ", "));
    rawQueryBuilder.append(" FROM ");
    rawQueryBuilder.append(DatabaseOpenHelper.T_RSS_ITEM);
    rawQueryBuilder.append(", ");
    rawQueryBuilder.append(DbMedia.T_MEDIA);
    rawQueryBuilder.append(" WHERE ");
    rawQueryBuilder.append(DbMedia.T_MEDIA);
    rawQueryBuilder.append("._id=");
    rawQueryBuilder.append(DatabaseOpenHelper.T_RSS_ITEM);
    rawQueryBuilder.append(".");
    rawQueryBuilder.append(RSSItem.MEDIA_ID_KEY);
    if (!filter.getSelectionQuery().isEmpty()) {
      rawQueryBuilder.append(" AND ");
    }
    rawQueryBuilder.append(filter.getSelectionQuery());
    if (ordering != null) {
      rawQueryBuilder.append(" ORDER BY ");
      rawQueryBuilder.append(ordering);
    }
    if (limit != null) {
      rawQueryBuilder.append(" LIMIT ");
      rawQueryBuilder.append(limit);
    }
    Log.d("filters", rawQueryBuilder.toString());
    Cursor c = mDb.rawQuery(rawQueryBuilder.toString(), filter.getSelectionValues());
    return c;
  }

  public void deleteItemAndItsMedia(RSSItem rssItem) {
    DbMedia dbMediaUpdater = new DbMedia(mDb);
    dbMediaUpdater.deleteById(rssItem.getMedia().getId());
    mDb.delete(DatabaseOpenHelper.T_RSS_ITEM, DatabaseOpenHelper._ID + "=?", new String[]{String.valueOf(rssItem.getDbId())});
  }

  RSSItem itemFromCursor(Cursor c) throws ParseException {
    String authorAddress = c.getString(c.getColumnIndex(RSSItem.AUTHOR_TAG));
    String category = c.getString(c.getColumnIndex(RSSItem.CAT_TAG));
    String channelTitle = c.getString(c.getColumnIndex(RSSItem.CHANNELTITLE_KEY));
    String comments = c.getString(c.getColumnIndex(RSSItem.COMMENTS_TAG));
    String pubDate = c.getString(c.getColumnIndex(RSSItem.DATE_TAG));
    String description = c.getString(c.getColumnIndex(RSSItem.DESC_TAG));
    String guid = c.getString(c.getColumnIndex(RSSItem.GUID_TAG));
    String link = c.getString(c.getColumnIndex(RSSItem.LINK_TAG));
    boolean isRead = c.getInt(c.getColumnIndex(RSSItem.READ_KEY)) > 0;
    boolean isDeleted = c.getInt(c.getColumnIndex(RSSItem.ARCHIVED_KEY)) > 0;
    String mediaUrl = c.getString(c.getColumnIndex(RSSItem.MEDIA_KEY));
    long mediaId = c.getLong(c.getColumnIndex(RSSItem.MEDIA_ID_KEY));
    String title = c.getString(c.getColumnIndex(RSSItem.TITLE_TAG));
    long channelId = c.getLong(c.getColumnIndex(DatabaseOpenHelper.CHANNEL_ID_KEY));
    Media media = new DbMedia(mDb).readById(mediaId);
    RSSItem item = new RSSItem(channelTitle, title, link, description, authorAddress, category,
            comments, media, guid, pubDate, isRead, channelId, isDeleted);
    item.setDbId(c.getLong(c.getColumnIndex(DatabaseOpenHelper._ID)));
    return item;
  }

  public int getItemCount(){
    Cursor c = mDb.rawQuery("SELECT COUNT(*) AS nbitems FROM " + DatabaseOpenHelper.T_RSS_ITEM, new String[0]);
    c.moveToFirst();
    return c.getInt(c.getColumnIndex("nbitems"));
  }

  public void closeDb() {
    //mDb.close();
  }
}
