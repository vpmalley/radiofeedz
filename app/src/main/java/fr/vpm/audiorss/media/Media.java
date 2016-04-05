package fr.vpm.audiorss.media;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import fr.vpm.audiorss.db.DbMedia;

public class Media implements Parcelable {

  public static final String MIME_IMAGE = "image";

  public enum Folder {
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

  private boolean isDownloaded;

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

  public String getFileName() {
    String typeExtension = "";
    int extensionStart = inetUrl.lastIndexOf('.');
    if (extensionStart > -1) {
      typeExtension = inetUrl.substring(extensionStart);
    }
    return name.replace(' ', '_') + typeExtension;
  }

  public Folder getDownloadFolder() {
    Media.Folder folder = Media.Folder.EXTERNAL_DOWNLOADS_PODCASTS;
    if (getMimeType().startsWith(MIME_IMAGE)){
      folder = Media.Folder.EXTERNAL_DOWNLOADS_PICTURES;
    }
    return folder;
  }

  private boolean exists() {
    return !getDistantUrl().isEmpty();
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
  public boolean isDownloaded() {
    return isDownloaded;
  }

  public void setIsDownloaded(boolean isDownloaded) {
    this.isDownloaded = isDownloaded;
  }

  public String getName() {
    return name;
  }

  public String getNotificationTitle() {
    return notificationTitle;
  }

  public String getDistantUrl() {
    return inetUrl;
  }

  public String getDeviceUri() {
    return deviceUri;
  }

  public void setDeviceUri(String deviceUri) {
    this.deviceUri = deviceUri;
  }

  public long getDownloadId() {
    return downloadId;
  }

  public void setDownloadId(long downloadId) {
    this.downloadId = downloadId;
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


}
