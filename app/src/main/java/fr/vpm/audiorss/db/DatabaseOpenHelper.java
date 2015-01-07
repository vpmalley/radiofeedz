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

  public static final String CHANNEL_ID_KEY = "channel_id";

  final static String[] COLS_RSS_CHANNEL_ITEM = {_ID, CHANNEL_ID_KEY, ITEM_ID_KEY};

  final static String[] COLS_RSS_ITEM = {T_RSS_ITEM + "." + _ID, CHANNEL_ID_KEY, RSSItem.AUTHOR_TAG, RSSItem.CAT_TAG,
      RSSItem.CHANNELTITLE_KEY, RSSItem.COMMENTS_TAG, RSSItem.DATE_TAG, RSSItem.DESC_TAG,
      RSSItem.GUID_TAG, RSSItem.LINK_TAG, RSSItem.LOCAL_MEDIA_KEY, RSSItem.MEDIA_KEY,
      T_RSS_ITEM + "." + RSSItem.TITLE_TAG, RSSItem.MEDIA_ID_KEY, RSSItem.READ_KEY, RSSItem.ARCHIVED_KEY};

  final private static String T_CREATE_RSS_ITEM = "CREATE TABLE " + T_RSS_ITEM + " (" + _ID
      + INTEGER_PRIMARY_KEY_AUTO + CHANNEL_ID_KEY + TEXT_COLUMN + SEP_COLUMN + RSSItem.AUTHOR_TAG
      + TEXT_COLUMN + SEP_COLUMN + RSSItem.CAT_TAG + TEXT_COLUMN + SEP_COLUMN
      + RSSItem.CHANNELTITLE_KEY + TEXT_COLUMN + SEP_COLUMN + RSSItem.COMMENTS_TAG + TEXT_COLUMN
      + SEP_COLUMN + RSSItem.DATE_TAG + " TEXT" + SEP_COLUMN + RSSItem.DESC_TAG + TEXT_COLUMN
      + SEP_COLUMN + RSSItem.GUID_TAG + TEXT_COLUMN + SEP_COLUMN + RSSItem.LINK_TAG + TEXT_COLUMN
      + SEP_COLUMN + RSSItem.LOCAL_MEDIA_KEY + TEXT_COLUMN + SEP_COLUMN + RSSItem.MEDIA_KEY
      + TEXT_COLUMN + SEP_COLUMN + RSSItem.TITLE_TAG + TEXT_COLUMN + SEP_COLUMN + RSSItem.MEDIA_ID_KEY + " INTEGER" +
      SEP_COLUMN + RSSItem.READ_KEY + " INTEGER" + SEP_COLUMN + RSSItem.ARCHIVED_KEY + " INTEGER )";

  final private static String T_CREATE_RSS_CHANNEL_ITEM = "CREATE TABLE " + T_RSS_CHANNEL_ITEM
      + " (" + _ID + INTEGER_PRIMARY_KEY_AUTO + CHANNEL_ID_KEY + TEXT_COLUMN + SEP_COLUMN
      + ITEM_ID_KEY + TEXT_COLUMN + ")";

  final private static String I_CREATE_RSSITEM_RECENT = "CREATE INDEX IF NOT EXISTS rssitem_recent_index ON " + T_RSS_ITEM +
          " (" + RSSItem.ARCHIVED_KEY + ", " + RSSItem.DATE_TAG + " DESC);";
  final private static String I_CREATE_RSSITEM_CHANNEL = "CREATE INDEX IF NOT EXISTS rssitem_channel_index ON " + T_RSS_ITEM +
          " (" + CHANNEL_ID_KEY + ", " + RSSItem.ARCHIVED_KEY + ", " + RSSItem.DATE_TAG + " DESC);";
  final private static String I_DROP_RSSITEM_RECENT = "DROP INDEX IF EXISTS rssitem_recent_index;";
  final private static String I_DROP_RSSITEM_CHANNEL = "DROP INDEX IF EXISTS rssitem_channel_index;";

  final private static String DB_NAME = "rss_db";
  final private static Integer DB_VERSION = 18;
  final private Context mContext;

  private static DatabaseOpenHelper sHelper;

  private DatabaseOpenHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
    this.mContext = context;
  }

  public static DatabaseOpenHelper getInstance(Context context){
    if (sHelper == null) {
      sHelper = new DatabaseOpenHelper(context);
    }
    return sHelper;
  }


  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(DbRSSChannel.T_CREATE_RSS_CHANNEL);
    db.execSQL(T_CREATE_RSS_ITEM);
    db.execSQL(DbMedia.T_CREATE_MEDIA);
    db.execSQL(I_CREATE_RSSITEM_RECENT);
    db.execSQL(I_CREATE_RSSITEM_CHANNEL);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    /*
    db.execSQL("DROP TABLE IF EXISTS " + T_RSS_ITEM);
    db.execSQL("DROP TABLE IF EXISTS " + DbRSSChannel.T_RSS_CHANNEL);
    db.execSQL("DROP TABLE IF EXISTS " + DbMedia.T_MEDIA);
    onCreate(db);
    */
    db.execSQL(I_DROP_RSSITEM_RECENT);
    db.execSQL(I_DROP_RSSITEM_CHANNEL);
    db.execSQL(I_CREATE_RSSITEM_RECENT);
    db.execSQL(I_CREATE_RSSITEM_CHANNEL);
  }

  void deleteDatabase() {
    mContext.deleteDatabase(DB_NAME);
  }
}