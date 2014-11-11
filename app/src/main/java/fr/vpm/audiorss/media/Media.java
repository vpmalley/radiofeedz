package fr.vpm.audiorss.media;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import junit.framework.Assert;

import java.io.File;

import fr.vpm.audiorss.db.DbMedia;

public class Media implements Downloadable, Parcelable {

  // db id
  private long id;

  // media info

  private final String name;

  private final String notificationTitle;

  // online info

  private final String inetUrl;

  private final String mimeType;

  // device info

  private String deviceUri;

  private long downloadId;

  private boolean isDownloaded = false;

  public Media(String name, String title, String url, String mimeType) {
    this.id = -1;
    this.name = name;
    this.notificationTitle = title;
    this.inetUrl = url;
    this.mimeType = mimeType;
  }

  public Media(long id, String name, String title, String url, String deviceUri, long downloadId,
               boolean isDownloaded, String mimeType) {
    this(name, title, url, mimeType);
    this.id = id;
    this.deviceUri = deviceUri;
    this.downloadId = downloadId;
    this.isDownloaded = isDownloaded;
  }

  @Override
  public void download(final Context context, int visibility) {

    // retrieve download folder from the preferences
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    String downloadFolder = getDownloadFolder(sharedPref);

    DownloadManager.Request r = new DownloadManager.Request(Uri.parse(inetUrl));

    // transforming the name for saving the file.
    String downloadName = getFileName();
    r.setDestinationInExternalPublicDir(downloadFolder, downloadName);

    // When downloading music and videos they will be listed in the player
    // (Seems to be available since Honeycomb only)
    r.allowScanningByMediaScanner();

    r.setNotificationVisibility(visibility);

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

  public String getDownloadFolder(SharedPreferences sharedPref) {
    return sharedPref.getString("pref_download_folder",
          Environment.DIRECTORY_PODCASTS);
  }

  public String getFileName() {
    String typeExtension = inetUrl.substring(inetUrl.lastIndexOf('.'));
    return name.replace(' ', '_') + typeExtension;
  }

  public File getMediaFile(Context context) {
    SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String storageMediaRoot = defaultSharedPreferences.getString("pref_storage_root",
        Environment.getExternalStorageDirectory().getPath());
    if (storageMediaRoot.isEmpty()){
      storageMediaRoot = Environment.getExternalStorageDirectory().getPath();
    }
    StringBuilder mediaFilePathBuilder = new StringBuilder();
    mediaFilePathBuilder.append('/');
    mediaFilePathBuilder.append(storageMediaRoot);
    mediaFilePathBuilder.append('/');
    mediaFilePathBuilder.append(getDownloadFolder(defaultSharedPreferences));
    mediaFilePathBuilder.append('/');
    mediaFilePathBuilder.append(getFileName());
    String path = mediaFilePathBuilder.toString();
    path = path.replace("//", "/");
    return new File(mediaFilePathBuilder.toString());
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

  public String getMimeType() {
    return mimeType;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    Bundle b = new Bundle();
    b.putLong("id", id);
    b.putString(DbMedia.NAME_KEY, name);
    b.putString(DbMedia.TITLE_KEY, notificationTitle);
    b.putString(DbMedia.INET_URL_KEY, inetUrl);
    b.putString(DbMedia.DEVICE_URI_KEY, deviceUri);
    b.putLong(DbMedia.DL_ID_KEY, downloadId);
    b.putBoolean(DbMedia.IS_DL_KEY, isDownloaded);
    b.putString(DbMedia.MIME_KEY, mimeType);
    parcel.writeBundle(b);
  }

  private Media(Parcel in) {
    Bundle b = in.readBundle();
    id = b.getLong("id");
    name = b.getString(DbMedia.NAME_KEY);
    notificationTitle = b.getString(DbMedia.TITLE_KEY);
    inetUrl = b.getString(DbMedia.INET_URL_KEY);
    deviceUri = b.getString(DbMedia.DEVICE_URI_KEY);
    downloadId = b.getLong(DbMedia.DL_ID_KEY);
    isDownloaded = b.getBoolean(DbMedia.IS_DL_KEY);
    mimeType = b.getString(DbMedia.MIME_KEY);
  }

  public static final Parcelable.Creator<Media> CREATOR
      = new Parcelable.Creator<Media>() {
    public Media createFromParcel(Parcel in) {
      return new Media(in);
    }

    public Media[] newArray(int size) {
      return new Media[size];
    }
  };


  private class MediaBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d("BReceiver", "A download is complete");
      long fileId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
      //Assert.assertEquals(downloadId, fileId);
      if (downloadId != fileId){
        return;
      }

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
