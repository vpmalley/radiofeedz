package fr.vpm.audiorss.media;

/**
 * Created by vince on 18/12/14.
 */
public interface MediaDownloadListener {

  /**
   * Triggered when a media download is over
   */
  void onMediaDownloaded();

  class DummyMediaDownloadListener implements MediaDownloadListener {

    @Override
    public void onMediaDownloaded() {
      // do nothing
    }
  }
}
