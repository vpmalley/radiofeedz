package fr.vpm.audiorss.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class DbMedia {

  private static final String COMMA = ",";

  public final static String NAME_KEY = "name";

  public final static String TITLE_KEY = "title";

  public final static String INET_URL_KEY = "inet_url";

  public final static String DEVICE_URI_KEY = "device_uri";

  public final static String DL_ID_KEY = "download_id";

  public final static String IS_DL_KEY = "is_downloaded";

  public final static String MIME_KEY = "mime_type";

  public final static String[] COLS_MEDIA = {DatabaseOpenHelper._ID, NAME_KEY, TITLE_KEY,
          INET_URL_KEY, DEVICE_URI_KEY, DL_ID_KEY, IS_DL_KEY, MIME_KEY};

  public final static String T_MEDIA = "media";

  final static String T_CREATE_MEDIA = "CREATE TABLE " + T_MEDIA + " (" + DatabaseOpenHelper._ID
          + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME_KEY + DatabaseOpenHelper.TEXT_COLUMN + COMMA
          + TITLE_KEY + " TEXT" + COMMA + INET_URL_KEY + " TEXT" + COMMA
          + DEVICE_URI_KEY + " TEXT" + COMMA + DL_ID_KEY + " INTEGER" + COMMA
          + IS_DL_KEY + " INTEGER" + COMMA + MIME_KEY + " TEXT" + ")";

  private SQLiteDatabase mDb = null;

  public DbMedia(SQLiteDatabase db) {
    mDb = db;
  }

  /**
   * Specifically for usage when not through a DbRSSChannel, as the DB is opened by DbRSSChannel.
   * The DB must be closed with a call to closeDb() once all operations are run.
   * @param context The current Android context
   * @param writable whether the operations run after this call will write to the DB
   */
  public DbMedia(Context context, boolean writable){
    DatabaseOpenHelper dbHelper = DatabaseOpenHelper.getInstance(context);
    //new DatabaseOpenHelper(context);
    if (writable){
      mDb = dbHelper.getWritableDatabase();
    } else {
      mDb = dbHelper.getReadableDatabase();
    }
  }

  /**
   * Closes the DB once all operations are run. Must be call if and only if the
   * constructor DbMedia(Context, boolean) has been called.
   */
  public void closeDb() {
    //mDb.close();
  }

  public List<Media> readAll() throws ParseException {
    if (!mDb.isOpen()) {
      throw new IllegalArgumentException("Database is not open to read a media");
    }
    Cursor c = mDb.query(T_MEDIA, COLS_MEDIA, null, null, null, null, null, "5000");
    // the limit is to avoid OutOfMemoryError which is not supposed to happen in the first place

    List<Media> medias = new ArrayList<>();
    if (c.getCount() > 0) {
      c.moveToFirst();
      for (int i = 0; i < c.getCount(); i++) {
        medias.add(mediaFromCursorEntry(c));
        c.moveToNext();
      }
    }
    c.close();
    return medias;
  }

  public Media readById(long id) throws ParseException {
    Media media = null;
    if (!mDb.isOpen()) {
      throw new IllegalArgumentException("Database is not open to read a media");
    }
    Cursor c = mDb.query(T_MEDIA, COLS_MEDIA, DatabaseOpenHelper._ID + "=?",
            new String[]{String.valueOf(id)}, null, null, null);
    if (c.getCount() > 0) {
      c.moveToFirst();
      media = mediaFromCursorEntry(c);
    }
    c.close();

    return media;
  }

  public Media add(Media media) {
    ContentValues mediaValues = createContentValues(media);
    long id = mDb.insert(T_MEDIA, null, mediaValues);
    media.setId(id);
    return media;
  }

  public Media update(Media media) {
    ContentValues mediaValues = createContentValues(media);
    mediaValues.put(DatabaseOpenHelper._ID, media.getId());
    mDb.update(T_MEDIA, mediaValues, DatabaseOpenHelper._ID + "=?",
            new String[]{String.valueOf(media.getId())});
    return media;
  }

  public void deleteById(long id) {
    mDb.delete(T_MEDIA, DatabaseOpenHelper._ID + "=?", new String[]{String.valueOf(id)});
  }

  public int getMediaCount(){
    Cursor c = mDb.rawQuery("SELECT COUNT(*) AS nbitems FROM " + DbMedia.T_MEDIA, new String[0]);
    c.moveToFirst();
    return c.getInt(c.getColumnIndex("nbitems"));
  }

  public void deleteOrphans(){
    StringBuilder rawQueryBuilder = new StringBuilder();
    rawQueryBuilder.append("SELECT ");
    rawQueryBuilder.append(DbMedia.T_MEDIA);
    rawQueryBuilder.append(".");
    rawQueryBuilder.append(DatabaseOpenHelper._ID);
    rawQueryBuilder.append(", ");
    rawQueryBuilder.append(INET_URL_KEY);
    rawQueryBuilder.append(" FROM ");
    rawQueryBuilder.append(DbMedia.T_MEDIA);

    rawQueryBuilder.append(" LEFT JOIN ");
    rawQueryBuilder.append(DatabaseOpenHelper.T_RSS_ITEM);
    rawQueryBuilder.append(" ON ");
    rawQueryBuilder.append(DbMedia.T_MEDIA);
    rawQueryBuilder.append(".");
    rawQueryBuilder.append(DatabaseOpenHelper._ID);
    rawQueryBuilder.append("=");
    rawQueryBuilder.append(DatabaseOpenHelper.T_RSS_ITEM);
    rawQueryBuilder.append(".");
    rawQueryBuilder.append(RSSItem.MEDIA_ID_KEY);

    rawQueryBuilder.append(", ");
    rawQueryBuilder.append(DbRSSChannel.T_RSS_CHANNEL);
    rawQueryBuilder.append(" ON ");
    rawQueryBuilder.append(DbMedia.T_MEDIA);
    rawQueryBuilder.append(".");
    rawQueryBuilder.append(DatabaseOpenHelper._ID);
    rawQueryBuilder.append("=");
    rawQueryBuilder.append(DbRSSChannel.T_RSS_CHANNEL);
    rawQueryBuilder.append(".");
    rawQueryBuilder.append(RSSChannel.IMAGE_ID_TAG);

    rawQueryBuilder.append(" WHERE ");
    rawQueryBuilder.append(DatabaseOpenHelper.T_RSS_ITEM);
    rawQueryBuilder.append(".");
    rawQueryBuilder.append(DatabaseOpenHelper._ID);
    rawQueryBuilder.append(" IS NULL");
    rawQueryBuilder.append(" OR ");
    rawQueryBuilder.append(DbRSSChannel.T_RSS_CHANNEL);
    rawQueryBuilder.append(".");
    rawQueryBuilder.append(DatabaseOpenHelper._ID);
    rawQueryBuilder.append(" IS NULL;");

    Log.d("orphans-filter", rawQueryBuilder.toString());
    Cursor c = mDb.rawQuery(rawQueryBuilder.toString(), new String[0]);
    Log.d("orphans", "query returned " + c.getCount());
    c.moveToFirst();
    for (int i = 0; i < c.getCount(); i++) {
      deleteById(c.getLong(c.getColumnIndex(DatabaseOpenHelper._ID)));
      c.moveToNext();
    }
  }

  ContentValues createContentValues(Media media) {
    ContentValues channelValues = new ContentValues();
    channelValues.put(NAME_KEY, media.getName());
    channelValues.put(TITLE_KEY, media.getNotificationTitle());
    channelValues.put(INET_URL_KEY, media.getInetUrl());
    channelValues.put(DEVICE_URI_KEY, media.getDeviceUri());
    channelValues.put(DL_ID_KEY, media.getDownloadId());
    channelValues.put(IS_DL_KEY, media.isDownloaded());
    channelValues.put(MIME_KEY, media.getMimeType());
    return channelValues;
  }

  Media mediaFromCursorEntry(Cursor c) {
    long id = c.getLong(c.getColumnIndex(DatabaseOpenHelper._ID));
    String name = c.getString(c.getColumnIndex(NAME_KEY));
    String notificationTitle = c.getString(c.getColumnIndex(TITLE_KEY));
    String inetUrl = c.getString(c.getColumnIndex(INET_URL_KEY));
    String deviceUri = c.getString(c.getColumnIndex(DEVICE_URI_KEY));
    long downloadId = c.getLong(c.getColumnIndex(DL_ID_KEY));
    boolean isDownloaded = c.getInt(c.getColumnIndex(IS_DL_KEY)) > 0;
    String mimeType = c.getString(c.getColumnIndex(MIME_KEY));
    return new Media(id, name, notificationTitle, inetUrl, deviceUri, downloadId, isDownloaded, mimeType);
  }
}
