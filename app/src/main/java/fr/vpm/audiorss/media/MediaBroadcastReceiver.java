package fr.vpm.audiorss.media;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.db.AsyncDbSaveMedia;
import fr.vpm.audiorss.process.AsyncCallbackListener;

/**
* Created by vince on 24/12/14.
*/
public class MediaBroadcastReceiver extends BroadcastReceiver {

  private static List<Media> medias = new ArrayList<>();
  private static List<MediaDownloadListener> mediaDownloadListeners = new ArrayList<>();

  /**
   * Synchronized getter to limit risk of simultaneours reading/writing
   * @return the medias watched
   */
  public static synchronized List<Media> getMedias() {
    return medias;
  }

  public static void addMedia(Media media){
    getMedias().add(media);
  }

  /**
   * Synchronized getter to limit risk of simultaneours reading/writing
   * @return the listeners to contact
   */
  public static synchronized List<MediaDownloadListener> getListeners() {
    return mediaDownloadListeners;
  }

  public static void addListener(MediaDownloadListener listener){
    getListeners().add(listener);
  }
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d("BReceiver", "A download is complete");
    long fileId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

    for (Media media : getMedias()) {
      if (media.getDownloadId() == fileId) {

        // query the status of the file
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(fileId);
        DownloadManager dm = (DownloadManager) context.getSystemService(Activity.DOWNLOAD_SERVICE);
        Cursor c = dm.query(query);
        c.moveToFirst();

        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

        if (DownloadManager.STATUS_SUCCESSFUL == status) {
          media.setDeviceUri(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
          media.isDownloaded(context, true);

          new AsyncDbSaveMedia(new AsyncCallbackListener.DummyCallback<List<Media>>(), context).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, media);
          for (MediaDownloadListener listener : getListeners()) {
            listener.onMediaDownloaded();
          }
          getListeners().clear();
        }
        Log.d("BReceiver", "The status for " + media.getId() + " is " + status + ". It is located at "
                + media.getDeviceUri());
        medias.remove(media);
      }
    }

  }

}
