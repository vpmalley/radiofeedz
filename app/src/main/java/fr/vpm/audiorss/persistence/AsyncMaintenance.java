package fr.vpm.audiorss.persistence;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.vpm.audiorss.db.AsyncDbDeleteRSSItem;
import fr.vpm.audiorss.db.AsyncDbReadRSSItems;
import fr.vpm.audiorss.db.filter.MaintenanceFilter;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * This performs the asynchronous maintenance task to clean folders of icons.
 *
 * This should be called on a regular basis (e.g. at application opening).
 * This is not recommended to call it too often, but a date of last maintenance is stored (in memory) so that it is not cleaned too often.
 *
 * Created by vince on 10/12/14.
 */
public class AsyncMaintenance extends AsyncTask<File, Integer, File> {

  private static final int PICS_THRESHOLD = 500;
  private static final int ICONS_EXPIRY_TIME = 700000000; // icons older than this number of milliseconds are erased (about 8 days)
  private static final int ITEMS_EXPIRY_TIME = 180;

  private final Context context;

  public AsyncMaintenance(Context context) {
    this.context = context;
  }

  @Override
  protected File doInBackground(File... params) {

    cleanFolders(params);
    cleanItems();

    return null;
  }

  /**
   * Cleans the items in the Db after N days
   */
  private void cleanItems() {
    int itemsExpiryTime = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context).
            getString("pref_items_deletion", "180"));
    MaintenanceFilter maintenanceFilter = new MaintenanceFilter(itemsExpiryTime);
    List<SelectionFilter> filters = new ArrayList<SelectionFilter>();
    filters.add(maintenanceFilter);
    AsyncDbReadRSSItems asyncReader = new AsyncDbReadRSSItems(new AsyncCallbackListener<List<RSSItem>>() {
      @Override
      public void onPreExecute() {
        // do nothing
      }

      @Override
      public void onPostExecute(List<RSSItem> result) {
         new AsyncDbDeleteRSSItem(new DummyCallback<List<RSSItem>>(), context).
                executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, result.toArray(new RSSItem[result.size()]));
      }
    }, context, filters);
    asyncReader.forceReadAll();
    asyncReader.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
  }

  /**
   * Cleans the folders specified as params according to the thresholds defined as constants
   * @param params the folders to clean
   */
  private void cleanFolders(File[] params) {
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DAY_OF_YEAR, -1);
    FilePictureSaver fileSaver = new FilePictureSaver(context);
    for (File folder : params) {
      fileSaver.cleanFolder(folder, PICS_THRESHOLD, ICONS_EXPIRY_TIME);
    }
  }
}
