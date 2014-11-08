package fr.vpm.audiorss.db;

import android.graphics.Bitmap;

import java.util.List;

import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.media.PictureLoadedListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 03/11/14.
 */
public class RefreshViewCallback implements AsyncCallbackListener<List<RSSChannel>>,PictureLoadedListener {

  private final ProgressListener progressListener;

  private final FeedsActivity<List<RSSChannel>> activity;

  private final NetworkChecker networkChecker;

  public RefreshViewCallback(ProgressListener progressListener, FeedsActivity activity, NetworkChecker networkChecker) {
    this.progressListener = progressListener;
    this.activity = activity;
    this.networkChecker = networkChecker;
  }

  @Override
  public void onPreExecute() {
    progressListener.startRefreshProgress();
  }

  @Override
  public void onPostExecute(List<RSSChannel> result) {
    progressListener.stopRefreshProgress();
    activity.setData(result);
    activity.refreshView();
  }

  @Override
  public void onPictureLoaded(Bitmap pictureBitmap) {
    activity.refreshView();
  }

}
