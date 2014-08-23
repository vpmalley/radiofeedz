package fr.vpm.audiorss.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import fr.vpm.audiorss.rss.RSSItem;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

  private static final String INTEGER_PRIMARY_KEY_AUTO = " INTEGER PRIMARY KEY AUTOINCREMENT, ";

  static final String TEXT_COLUMN = " TEXT NOT NULL";

  private static final String SEP_COLUMN = ", ";

  final static String _ID = "_id";

  final static String T_RSS_ITEM = "rssitem";
  final static String T_RSS_CHANNEL_ITEM = "rsschannelitem";

  static final String ITEM_ID_KEY = "item_id";

  static final String CHANNEL_ID_KEY = "channel_id";

  final static String[] COLS_RSS_CHANNEL_ITEM = { _ID, CHANNEL_ID_KEY, ITEM_ID_KEY };

  final static String[] COLS_RSS_ITEM = { _ID, CHANNEL_ID_KEY, RSSItem.AUTHOR_TAG, RSSItem.CAT_TAG,
      RSSItem.CHANNELTITLE_KEY, RSSItem.COMMENTS_TAG, RSSItem.DATE_TAG, RSSItem.DESC_TAG,
      RSSItem.GUID_TAG, RSSItem.LINK_TAG, RSSItem.LOCAL_MEDIA_KEY, RSSItem.MEDIA_KEY,
      RSSItem.TITLE_TAG, RSSItem.MEDIA_ID_KEY };

  final private static String T_CREATE_RSS_ITEM = "CREATE TABLE " + T_RSS_ITEM + " (" + _ID
      + INTEGER_PRIMARY_KEY_AUTO + CHANNEL_ID_KEY + TEXT_COLUMN + SEP_COLUMN + RSSItem.AUTHOR_TAG
      + TEXT_COLUMN + SEP_COLUMN + RSSItem.CAT_TAG + TEXT_COLUMN + SEP_COLUMN
      + RSSItem.CHANNELTITLE_KEY + TEXT_COLUMN + SEP_COLUMN + RSSItem.COMMENTS_TAG + TEXT_COLUMN
      + SEP_COLUMN + RSSItem.DATE_TAG + " TEXT" + SEP_COLUMN + RSSItem.DESC_TAG + TEXT_COLUMN
      + SEP_COLUMN + RSSItem.GUID_TAG + TEXT_COLUMN + SEP_COLUMN + RSSItem.LINK_TAG + TEXT_COLUMN
      + SEP_COLUMN + RSSItem.LOCAL_MEDIA_KEY + TEXT_COLUMN + SEP_COLUMN + RSSItem.MEDIA_KEY
      + TEXT_COLUMN + SEP_COLUMN + RSSItem.TITLE_TAG + TEXT_COLUMN + SEP_COLUMN + RSSItem.MEDIA_ID_KEY + " INTEGER )";

  final private static String T_CREATE_RSS_CHANNEL_ITEM = "CREATE TABLE " + T_RSS_CHANNEL_ITEM
      + " (" + _ID + INTEGER_PRIMARY_KEY_AUTO + CHANNEL_ID_KEY + TEXT_COLUMN + SEP_COLUMN
      + ITEM_ID_KEY + TEXT_COLUMN + ")";

  final private static String DB_NAME = "rss_db";
  final private static Integer DB_VERSION = 13;
  final private Context mContext;

  public DatabaseOpenHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
    this.mContext = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(DbRSSChannel.T_CREATE_RSS_CHANNEL);
    db.execSQL(T_CREATE_RSS_ITEM);
    db.execSQL(DbMedia.T_CREATE_MEDIA);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //deleteDatabase();
    db.execSQL("DROP TABLE IF EXISTS " + T_RSS_ITEM);
    db.execSQL("DROP TABLE IF EXISTS " + DbRSSChannel.T_RSS_CHANNEL);
    db.execSQL("DROP TABLE IF EXISTS " + DbMedia.T_MEDIA);
    onCreate(db);
  }

  void deleteDatabase() {
    mContext.deleteDatabase(DB_NAME);
  }
}