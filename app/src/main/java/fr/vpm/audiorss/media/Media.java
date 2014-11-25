package fr.vpm.audiorss.media;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import junit.framework.Assert;

import java.io.File;
import java.util.List;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.db.DbMedia;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.persistence.FilePictureSaver;

public class Media implements Downloadable, Parcelable {

  public static final String INTERNAL_ITEMS_PIC_DIR = "items-icons";
  public static final String INTERNAL_FEEDS_PIC_DIR = "feeds-icons";
  public static final String INTERNAL_APP_DIR = "RadioFeedz";
  public static final String MIME_IMAGE = "image";

  public enum Folder {
    INTERNAL_FEEDS_PICS,
    INTERNAL_ITEMS_PICS,
    EXTERNAL_DOWNLOADS_PODCASTS,
    EXTERNAL_DOWNLOADS_PICTURES
  }

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

  private Boolean isPodcastDownloaded = null;

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

    DownloadManager.Request r = new DownloadManager.Request(Uri.parse(inetUrl));

    Media.Folder externalDownloadsFolder = Media.Folder.EXTERNAL_DOWNLOADS_PODCASTS;
    if (getMimeType().startsWith("image")){
      externalDownloadsFolder = Media.Folder.EXTERNAL_DOWNLOADS_PICTURES;
    }
    r.setDestinationUri(Uri.fromFile(getMediaFile(context, externalDownloadsFolder)));

    // When downloading music and videos they will be listed in the player
    // (Seems to be available since Honeycomb only)
    r.allowScanningByMediaScanner();

    r.setNotificationVisibility(visibility);

    int networkFlags = retrieveNetworkFlags(context);
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

  public File getDownloadFolder(Context context, Folder folder) {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    String storageMediaRoot = sharedPref.getString("pref_storage_root",
        Environment.getExternalStorageDirectory().getPath());
    if (storageMediaRoot.isEmpty()){
      storageMediaRoot = Environment.getExternalStorageDirectory().getPath();
    }
    String storageMediaDir = sharedPref.getString("pref_download_podcasts_folder", "music/Podcasts");
    if (folder.equals(Folder.EXTERNAL_DOWNLOADS_PICTURES)){
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
    return dirFile;
  }

  public File getInternalFeedsPicsFolder(){
    File dir = new File(Environment.getExternalStoragePublicDirectory(INTERNAL_APP_DIR), INTERNAL_FEEDS_PIC_DIR);
    dir.mkdirs();
    return dir;
  }

  public File getInternalItemsPicsFolder(){
    File dir = new File(Environment.getExternalStoragePublicDirectory(INTERNAL_APP_DIR), INTERNAL_ITEMS_PIC_DIR);
    dir.mkdirs();
    return dir;
  }

  public String getFileName() {
    String typeExtension = "";
    int extensionStart = inetUrl.lastIndexOf('.');
    if (extensionStart > -1) {
      typeExtension = inetUrl.substring(extensionStart);
    }
    return name.replace(' ', '_') + typeExtension;
  }

  public File getMediaFile(Context context, Folder folder) {
    File dirFile = null;
    switch(folder) {
      case INTERNAL_FEEDS_PICS:
        dirFile = getInternalFeedsPicsFolder();
        break;
      case INTERNAL_ITEMS_PICS:
        dirFile = getInternalItemsPicsFolder();
        break;
      case EXTERNAL_DOWNLOADS_PODCASTS:
      case EXTERNAL_DOWNLOADS_PICTURES:
      default:
        dirFile = getDownloadFolder(context, folder);
        break;
    }
    return new File(dirFile, getFileName());
  }

  public boolean isPodcastDownloaded(Context context, boolean forceCheck){
    if (isPodcastDownloaded == null){

      Media.Folder externalDownloadsFolder = Media.Folder.EXTERNAL_DOWNLOADS_PODCASTS;
      if (getMimeType().startsWith("image")){
        externalDownloadsFolder = Media.Folder.EXTERNAL_DOWNLOADS_PICTURES;
      }
      isPodcastDownloaded = getMediaFile(context, externalDownloadsFolder).exists();
    }
    return isPodcastDownloaded;
  }

  private boolean checkNetwork(int networkFlags, Context context) {
    // Start download
    if (0 == networkFlags) {
      Toast
          .makeText(
              context,
              context.getResources().getString(R.string.disabled_download),
              Toast.LENGTH_LONG).show();
    }
    return (!(0 == networkFlags));
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

  public Bitmap getAsBitmap(Context context, List<PictureLoadedListener> pictureLoadedListeners, Folder folder){
    if (!isPicture()){
      return null;
    }
    Bitmap b = null;
    FilePictureSaver pictureRetriever = new FilePictureSaver(context);
    File pictureFile = getMediaFile(context, folder);
    if (pictureFile.exists()){
      b = pictureRetriever.retrieve(pictureFile);
    } else if (new DefaultNetworkChecker().checkNetworkForDownload(context, false)) {
      AsyncPictureLoader pictureLoader = new AsyncPictureLoader(pictureLoadedListeners, 300, 300, context, folder);
      pictureLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
    }
    return b;
  }

  public boolean isPicture() {
    return getMimeType().startsWith(MIME_IMAGE);
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
