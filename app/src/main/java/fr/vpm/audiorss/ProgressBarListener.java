package fr.vpm.audiorss;

import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by vince on 03/11/14.
 */
public class ProgressBarListener implements ProgressListener {

  private final ProgressBar mRefreshProgress;

  public ProgressBarListener(ProgressBar mRefreshProgress) {
    this.mRefreshProgress = mRefreshProgress;
  }

  public void startRefreshProgress() {
    mRefreshProgress.setVisibility(View.VISIBLE);
  }

  public void updateProgress(int progress) {
    mRefreshProgress.setProgress(progress);
  }

  public void stopRefreshProgress() {
    mRefreshProgress.setVisibility(View.GONE);
  }
}
