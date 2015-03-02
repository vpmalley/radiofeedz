package fr.vpm.audiorss;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by vince on 03/11/14.
 */
public class ProgressBarListener implements ProgressListener {

  private final ProgressBar mRefreshProgress;

  private long initialTime = -1;

  private int ctrStart = 0;

  private int ctrStop = 0;

  private boolean isVisible = false;

  private ProgressListener delegate;

  public ProgressBarListener(ProgressBar mRefreshProgress) {
    this.mRefreshProgress = mRefreshProgress;
  }

  public void setDelegate(ProgressListener delegate) {
    this.delegate = delegate;
  }

  public void startRefreshProgress() {
    ctrStart++;
    if (!isVisible) {
      initialTime = System.currentTimeMillis();
      mRefreshProgress.setVisibility(View.VISIBLE);
      isVisible = true;
    }
    if (delegate != null) {
      delegate.startRefreshProgress();
    }
  }

  public void updateProgress(int progress) {
    mRefreshProgress.setProgress(progress);
    if (delegate != null) {
      delegate.updateProgress(progress);
    }
  }

  public void stopRefreshProgress() {
    ctrStop++;
    if (isVisible && (ctrStop == ctrStart)) {
      mRefreshProgress.setVisibility(View.GONE);
      isVisible = false;
      Log.d("measures", "progress -end- " + (System.currentTimeMillis() - initialTime));
    }
    if (delegate != null) {
      delegate.stopRefreshProgress();
    }
  }
}
