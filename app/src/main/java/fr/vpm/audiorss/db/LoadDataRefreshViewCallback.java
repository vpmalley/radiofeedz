package fr.vpm.audiorss.db;

import android.util.Log;

import java.util.List;

import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 03/11/14.
 */
public class LoadDataRefreshViewCallback implements AsyncCallbackListener<List<RSSChannel>> {

  private final ProgressListener progressListener;

  private final FeedsActivity<List<RSSChannel>> activity;

  public LoadDataRefreshViewCallback(ProgressListener progressListener, FeedsActivity activity) {
    this.progressListener = progressListener;
    this.activity = activity;
  }

  @Override
  public void onPreExecute() {
    progressListener.startRefreshProgress();
  }

  @Override
  public void onPostExecute(List<RSSChannel> result) {
    progressListener.stopRefreshProgress();
    activity.loadDataAndRefreshView();
  }
}
