package fr.vpm.audiorss.db;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.media.AsyncPictureLoader;
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
    for (RSSChannel channel: result) {
      retrieveFeedPicture(channel);
    }
    progressListener.stopRefreshProgress();
    activity.setData(result);
    activity.refreshView();
  }

  private void retrieveFeedPicture(RSSChannel channel) {
    if ((channel.getBitmap() == null) && (channel.getImage() != null) && (channel.getImage().getInetUrl() != null) &&
        (networkChecker.checkNetwork(activity.getContext()))) {
      int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
          activity.getContext().getResources().getDisplayMetrics()));
      // shortcut: we expect 50 dp for the picture for the feed
      List<PictureLoadedListener> listeners = new ArrayList<PictureLoadedListener>();
      listeners.add(channel);
      new AsyncPictureLoader(listeners, px, px).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
          channel.getImage().getInetUrl());
    }
  }

  @Override
  public void onPictureLoaded(Bitmap pictureBitmap) {
    activity.refreshView();
  }

}
