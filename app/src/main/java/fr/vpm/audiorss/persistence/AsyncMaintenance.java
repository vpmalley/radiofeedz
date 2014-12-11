package fr.vpm.audiorss.persistence;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.util.Calendar;

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
  private static final int EXPIRY_TIME = 700000000; // icons older than this number of milliseconds are erased (about 8 days)

  private final Context context;

  public AsyncMaintenance(Context context) {
    this.context = context;
  }

  @Override
  protected File doInBackground(File... params) {
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DAY_OF_YEAR, -1);
    FilePictureSaver fileSaver = new FilePictureSaver(context);
    for (File folder : params) {
      fileSaver.cleanFolder(folder, PICS_THRESHOLD, EXPIRY_TIME);
    }
    return null;
  }
}
