package fr.vpm.audiorss.media;

import android.content.Context;

public interface Downloadable {

  void download(Context context, int visibility, MediaDownloadListener mediaDownloadListener, Media.Folder folder);

  long getDownloadId();

  boolean isDownloaded();

  String getDistantUrl();

}