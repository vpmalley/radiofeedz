package fr.vpm.audiorss.db;

import java.text.ParseException;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import fr.vpm.audiorss.media.Media;

public class DbMedia {

  private static final String COMMA = ",";

  private final static String NAME_KEY = "name";

  private final static String TITLE_KEY = "title";

  private final static String INET_URL_KEY = "inet_url";

  private final static String DEVICE_URI_KEY = "device_uri";

  private final static String DL_ID_KEY = "download_id";

  private final static String IS_DL_KEY = "is_downloaded";

  private final static String[] COLS_MEDIA = { DatabaseOpenHelper._ID, NAME_KEY, TITLE_KEY,
      INET_URL_KEY, DEVICE_URI_KEY, DL_ID_KEY, IS_DL_KEY };

  final static String T_MEDIA = "media";

  final static String T_CREATE_MEDIA = "CREATE TABLE " + T_MEDIA + " (" + DatabaseOpenHelper._ID
      + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME_KEY + DatabaseOpenHelper.TEXT_COLUMN + COMMA
      + TITLE_KEY + " TEXT" + COMMA + INET_URL_KEY + DatabaseOpenHelper.TEXT_COLUMN + COMMA
      + DEVICE_URI_KEY + DatabaseOpenHelper.TEXT_COLUMN + COMMA + DL_ID_KEY + " INTEGER" + COMMA
      + IS_DL_KEY + " INTEGER" + ")";

  private SQLiteDatabase mDb = null;

  public DbMedia(SQLiteDatabase db) {
    mDb = db;
  }

  public Media readById(long id) throws ParseException {
    Media media = null;
    if (!mDb.isOpen()) {
      throw new IllegalArgumentException("Database is not open to read a media");
    }
    Cursor c = mDb.query(T_MEDIA, COLS_MEDIA, DatabaseOpenHelper._ID + "=?",
        new String[] { String.valueOf(id) }, null, null, null);
    if (c.getCount() > 0) {
      c.moveToFirst();
      media = mediaFromCursorEntry(c);
    }
    c.close();

    return media;
  }

  public Media add(Media media) {
    Log.d("dbInsert", "media " + media.getInetUrl());

    ContentValues mediaValues = createContentValues(media);
    long id = mDb.insert(T_MEDIA, null, mediaValues);
    media.setId(id);
    return media;
  }

  public Media update(Media media) {
    ContentValues mediaValues = createContentValues(media);
    mediaValues.put(DatabaseOpenHelper._ID, media.getId());
    mDb.update(T_MEDIA, mediaValues, DatabaseOpenHelper._ID + "=?",
        new String[] { String.valueOf(media.getId()) });
    return media;
  }

  public void deleteById(long id) {
    mDb.delete(T_MEDIA, DatabaseOpenHelper._ID + "=?", new String[] { String.valueOf(id) });
  }

  ContentValues createContentValues(Media media) {
    ContentValues channelValues = new ContentValues();
    channelValues.put(NAME_KEY, media.getName());
    channelValues.put(TITLE_KEY, media.getNotificationTitle());
    channelValues.put(INET_URL_KEY, media.getInetUrl());
    channelValues.put(DEVICE_URI_KEY, media.getDeviceUri());
    channelValues.put(DL_ID_KEY, media.getDownloadId());
    channelValues.put(IS_DL_KEY, media.isDownloaded());
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
    return new Media(id, name, notificationTitle, inetUrl, deviceUri, downloadId, isDownloaded);
  }
}
