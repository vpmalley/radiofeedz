package fr.vpm.audiorss.media;

import android.content.Context;

public interface Downloadable {

  public void download(final Context context, int visibility);

  public long getDownloadId();

  public boolean isDownloaded();

  public String getDistantUrl();

}