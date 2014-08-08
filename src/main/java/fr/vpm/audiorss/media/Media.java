package fr.vpm.audiorss.media;

import junit.framework.Assert;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Media implements Downloadable {

  // db id
  long id;

  // media info

  String name;

  String notificationTitle;

  // online info

  String inetUrl;

  // device info

  String deviceUri;

  long downloadId;

  boolean isDownloaded = false;

  public Media(String name, String title, String url) {
    this.id = -1;
    this.name = name;
    this.notificationTitle = title;
    this.inetUrl = url;
  }

  public Media(long id, String name, String title, String url, String deviceUri, long downloadId,
      boolean isDownloaded) {
    this(name, title, url);
    this.id = id;
    this.deviceUri = deviceUri;
    this.downloadId = downloadId;
    this.isDownloaded = isDownloaded;
  }

  @Override
  public void download(final Context context) {

    // retrieve download folder from the preferences
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    String downloadFolder = sharedPref.getString("pref_download_folder",
        Environment.DIRECTORY_PODCASTS);

    DownloadManager.Request r = new DownloadManager.Request(Uri.parse(inetUrl));

    // This put the download in the same Download dir the browser uses
    r.setDestinationInExternalPublicDir(downloadFolder, name);

    // When downloading music and videos they will be listed in the player
    // (Seems to be available since Honeycomb only)
    r.allowScanningByMediaScanner();

    // Notify user when download is completed
    // (Seems to be available since Honeycomb only)
    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

    int networkFlags = retrieveNetworkFlags(sharedPref);
    r.setAllowedNetworkTypes(networkFlags);

    if (checkNetwork(networkFlags, context)) {
      r.setTitle(notificationTitle);
      r.setDescription(name);

      DownloadManager dm = (DownloadManager) context.getSystemService(Activity.DOWNLOAD_SERVICE);

      downloadId = dm.enqueue(r);
    }

    BroadcastReceiver downloadFinished = new MediaBroadcastReceiver();
    context.registerReceiver(downloadFinished, new IntentFilter(
        DownloadManager.ACTION_DOWNLOAD_COMPLETE));
  }

  private boolean checkNetwork(int networkFlags, Context context) {
    // Start download
    if (0 == networkFlags) {
      Toast
          .makeText(
              context,
              "Downloads are disabled on Wifi and mobile networks, the download will not start. Check Settings.",
              Toast.LENGTH_LONG).show();
    }
    return (!(0 == networkFlags));
  }

  private int retrieveNetworkFlags(SharedPreferences sharedPref) {
    int networkFlags = 0;
    if (sharedPref.getBoolean("pref_wifi_network_enabled", true)) {
      networkFlags += DownloadManager.Request.NETWORK_WIFI;
    }

    if (sharedPref.getBoolean("pref_mobile_network_enabled", true)) {
      networkFlags += DownloadManager.Request.NETWORK_MOBILE;
    }

    return networkFlags;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  /**
   * Whether the media is downloaded
   */
  @Override
  public boolean isDownloaded() {
    return isDownloaded;
  }

  public String getName() {
    return name;
  }

  public String getNotificationTitle() {
    return notificationTitle;
  }

  @Override
  public String getDistantUrl() {
    return inetUrl;
  }

  public String getInetUrl() {
    return inetUrl;
  }

  public String getDeviceUri() {
    return deviceUri;
  }

  @Override
  public long getDownloadId() {
    return downloadId;
  }

  private class MediaBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d("BReceiver", "A download is complete");
      long fileId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
      Assert.assertEquals(downloadId, fileId);

      // query the status of the file
      DownloadManager.Query query = new DownloadManager.Query();
      query.setFilterById(fileId);
      DownloadManager dm = (DownloadManager) context.getSystemService(Activity.DOWNLOAD_SERVICE);
      Cursor c = dm.query(query);
      c.moveToFirst();

      long id = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
      Assert.assertEquals(downloadId, id);
      int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

      if (DownloadManager.STATUS_SUCCESSFUL == status) {
        deviceUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
        isDownloaded = true;

      }
      Log.d("BReceiver", "The status for " + id + " is " + status + ". It is located at "
          + deviceUri);
    }

  }

}
