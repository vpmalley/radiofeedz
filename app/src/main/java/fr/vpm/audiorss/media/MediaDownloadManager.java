package fr.vpm.audiorss.media;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.db.AsyncDbSaveMedia;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 05/04/16.
 */
public class MediaDownloadManager {

  private Context context;

  public MediaDownloadManager(Context context) {
    this.context = context;
  }

  public void downloadMedia(RSSItem rssItem) {
    if (rssItem.getMedia() != null) {
      download(rssItem.getMedia());
    }
  }

  public void deleteMedia(RSSItem rssItem) {
    if (rssItem.getMedia() != null) {
      boolean deleted = getDownloadFile(rssItem.getMedia()).delete();
      if (deleted) {
        rssItem.getMedia().setIsDownloaded(false);
        new AsyncDbSaveMedia(new AsyncCallbackListener.DummyCallback(), context).execute(rssItem.getMedia());
      }
    }
  }

  /**
   * Determines whether the media is downloaded and available for play/display on the device
   * @return whether the file is downloaded
   */
  public boolean checkMediaIsDownloaded(Media media){
    media.setIsDownloaded(getDownloadFile(media).exists());
    new AsyncDbSaveMedia(new AsyncCallbackListener.DummyCallback<List<Media>>(), context).
        executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, media);
    return media.isDownloaded();
  }

  private void download(Media media) {

    DownloadManager.Request r = new DownloadManager.Request(Uri.parse(media.getDistantUrl()));

    r.setDestinationUri(Uri.fromFile(getDownloadFile(media)));

    // When downloading music and videos they will be listed in the player
    // (Seems to be available since Honeycomb only)
    r.allowScanningByMediaScanner();

    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

    int networkFlags = retrieveNetworkFlags(context);
    r.setAllowedNetworkTypes(networkFlags);

    if (checkNetwork(networkFlags, context)) {
      r.setTitle(media.getNotificationTitle());
      r.setDescription(media.getName());

      DownloadManager dm = (DownloadManager) context.getSystemService(Activity.DOWNLOAD_SERVICE);

      media.setDownloadId(dm.enqueue(r));
    }

    MediaBroadcastReceiver.addMedia(media);
  }

  private boolean checkNetwork(int networkFlags, Context context) {
    if (0 == networkFlags) {
      Toast
          .makeText(
              context,
              context.getResources().getString(R.string.disabled_download),
              Toast.LENGTH_LONG).show();
    }
    return 0 != networkFlags;
  }

  private int retrieveNetworkFlags(Context context) {
    int networkFlags = 0;
    NetworkChecker networkChecker = new DefaultNetworkChecker();
    if (networkChecker.isDownloadOverWifiAllowed(context)) {
      networkFlags += DownloadManager.Request.NETWORK_WIFI;
    }
    if (networkChecker.isDownloadOverMobileAllowed(context)) {
      networkFlags += DownloadManager.Request.NETWORK_MOBILE;
    }
    return networkFlags;
  }

  public File getDownloadFile(Media media) {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    String storageMediaRoot = sharedPref.getString("pref_storage_root",
        Environment.getExternalStorageDirectory().getPath());
    if ((storageMediaRoot != null) && (storageMediaRoot.isEmpty())){
      storageMediaRoot = Environment.getExternalStorageDirectory().getPath();
    }
    String storageMediaDir = sharedPref.getString("pref_download_podcasts_folder", "music/Podcasts");
    if (media.getDownloadFolder().equals(Media.Folder.EXTERNAL_DOWNLOADS_PICTURES)){
      storageMediaDir = sharedPref.getString("pref_download_pictures_folder", "Pictures/RadioFeedz");
    }

    StringBuilder dirPathBuilder = new StringBuilder();
    dirPathBuilder.append('/');
    dirPathBuilder.append(storageMediaRoot);
    dirPathBuilder.append('/');
    dirPathBuilder.append(storageMediaDir);
    dirPathBuilder.append('/');
    String dirPath = dirPathBuilder.toString().replace("//", "/");
    File dirFile = new File(dirPath);
    dirFile.mkdirs();

    return new File(dirFile, media.getFileName());
  }
}
