package fr.vpm.audiorss.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import fr.vpm.audiorss.db.AsyncDbDeleteRSSItem;
import fr.vpm.audiorss.db.AsyncDbReadRSSItems;
import fr.vpm.audiorss.db.DatabaseOpenHelper;
import fr.vpm.audiorss.db.DbMedia;
import fr.vpm.audiorss.db.DbRSSChannel;
import fr.vpm.audiorss.db.filter.MaintenanceFilter;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.media.MediaDownloadManager;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DateUtils;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * This performs the asynchronous maintenance task to clean folders of icons.
 *
 * This should be called on a regular basis (e.g. at application opening).
 * This is not recommended to call it too often, but a date of last maintenance is stored (in memory) so that it is not cleaned too often.
 *
 * Created by vince on 10/12/14.
 */
public class AsyncMaintenance {

  public static final String MAINTENANCE_TAG = "maintenance";

  private final Context context;

  private int totalSize;

  public AsyncMaintenance(Context context) {
    this.context = context;
  }

  /**
   * Cleans the items in the Db after N days
   */
  public void cleanItems() {
    String itemsExpiryTime = PreferenceManager.getDefaultSharedPreferences(context).
            getString("pref_items_deletion", "2");
    if (!Pattern.compile("\\d+").matcher(itemsExpiryTime).matches()){
      itemsExpiryTime = "2";
    }
    MaintenanceFilter maintenanceFilter = new MaintenanceFilter(Integer.valueOf(itemsExpiryTime));
    List<SelectionFilter> filters = new ArrayList<SelectionFilter>();
    filters.add(maintenanceFilter);
    AsyncDbReadRSSItems asyncReader = new AsyncDbReadRSSItems(new AsyncCallbackListener<List<RSSItem>>() {
      @Override
      public void onPreExecute() {
        // do nothing
      }

      @Override
      public void onPostExecute(List<RSSItem> result) {
        Log.d("items", "there are " + result.size());
        new AsyncDbDeleteRSSItem(new DummyCallback<List<RSSItem>>(), context).
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, result.toArray(new RSSItem[result.size()]));
      }
    }, context, filters);
    asyncReader.forceReadAll();
    asyncReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  /**
   * Refreshes all the media items for which a file exists on the filesystem
   */
  public void refreshDownloadedPodcasts() {
    DbMedia dbUpdater = new DbMedia(context, true);
    List<Media> medias = new ArrayList<>();
    try {
      medias = dbUpdater.readAll();
      Log.d("medias", "there are " + medias.size());
    } catch (ParseException e) {
      Log.w("db", "Failed retrieving the medias: " + e.getMessage());
    }
    for (Media media : medias) {
      boolean beforeUpdate = media.isDownloaded();
      if (beforeUpdate != new MediaDownloadManager(context).checkMediaIsDownloaded(media)) {
        dbUpdater.update(media);
      }
    }
    dbUpdater.closeDb();
  }

  private void analyzeData() {
    SQLiteDatabase db = DatabaseOpenHelper.getInstance(context).getReadableDatabase();

    long size = new File(db.getPath()).length();
    Log.d(MAINTENANCE_TAG, String.valueOf(size) + " bytes");

    Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseOpenHelper.T_RSS_ITEM, null);
    c.moveToFirst();
    Log.d(MAINTENANCE_TAG, String.valueOf(c.getInt(0)) + " items");
    c.close();

    Calendar filterDate = Calendar.getInstance();
    filterDate.add(Calendar.DAY_OF_YEAR, -2);
    String twoDaysAgo = DateUtils.formatDBDate(filterDate.getTime());
    c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseOpenHelper.T_RSS_ITEM + " WHERE " + RSSItem.DATE_TAG + "<'" +twoDaysAgo + "'", null);
    c.moveToFirst();
    Log.d(MAINTENANCE_TAG, String.valueOf(c.getInt(0)) + " items last 2 days");
    c.close();

    c = db.rawQuery("SELECT COUNT(*) FROM " + DbRSSChannel.T_RSS_CHANNEL, null);
    c.moveToFirst();
    Log.d(MAINTENANCE_TAG, String.valueOf(c.getInt(0)) + " channels");
    c.close();
    c = db.rawQuery("SELECT COUNT(*) FROM " + DbMedia.T_MEDIA, null);
    c.moveToFirst();
    Log.d(MAINTENANCE_TAG, String.valueOf(c.getInt(0)) + " medias");
    c.close();
    new DbMedia(db).deleteOrphans();


    totalSize = 0;
    browseFiles(context.getFilesDir());
    Log.d(MAINTENANCE_TAG, totalSize + " total bytes of regular files");
    totalSize = 0;
    browseFiles(context.getCacheDir());
    Log.d(MAINTENANCE_TAG, totalSize + " total bytes of cache files");
    totalSize = 0;
    browseFiles(context.getExternalCacheDir());
    Log.d(MAINTENANCE_TAG, totalSize + " total bytes of external cache files");
  }

  private void browseFiles(File dir) {
    Log.d(MAINTENANCE_TAG, dir.getName() + " has " + dir.list().length + " files");
    for (File f: dir.listFiles()) {
      totalSize += f.length();
      Log.d(MAINTENANCE_TAG, dir.getName() + "/" + f.getName() + " weighs " + f.length());
      if (f.isDirectory()) {
        browseFiles(f);
      }
    }
  }
}
