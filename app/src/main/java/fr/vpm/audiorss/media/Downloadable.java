package fr.vpm.audiorss.media;

import android.content.Context;

public interface Downloadable {

  void download(final Context context, int visibility, MediaDownloadListener mediaDownloadListener);

  long getDownloadId();

  boolean isDownloaded();

  String getDistantUrl();

}