package fr.vpm.audiorss;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 03/11/14.
 */
public class ProgressBarListener implements ProgressListener {

  private final ProgressBar mRefreshProgress;

  private long initialTime = -1;

  private int ctrStart = 0;

  private int ctrStop = 0;

  private boolean isVisible = false;

  private final TextView mLatestUpdate;

  public ProgressBarListener(ProgressBar mRefreshProgress) {
    this.mRefreshProgress = mRefreshProgress;
    mLatestUpdate = null;
  }

  public ProgressBarListener(ProgressBar mRefreshProgress, TextView latestUpdate) {
    this.mRefreshProgress = mRefreshProgress;
    mLatestUpdate = latestUpdate;
  }

  public void startRefreshProgress() {
    ctrStart++;
    if (!isVisible) {
      initialTime = System.currentTimeMillis();
      mRefreshProgress.setVisibility(View.VISIBLE);
      isVisible = true;
    }
  }

  public void updateProgress(int progress) {
    mRefreshProgress.setProgress(progress);
  }

  public void stopRefreshProgress() {
    ctrStop++;
    if (isVisible && (ctrStop == ctrStart)) {
      mRefreshProgress.setVisibility(View.GONE);
      isVisible = false;
      Log.d("measures", "progress -end- " + (System.currentTimeMillis() - initialTime));

      if (mLatestUpdate != null) { // should occur only after a refresh
        String currentDate = new SimpleDateFormat(RSSChannel.DISPLAY_PATTERN).format(Calendar.getInstance().getTime());
        mLatestUpdate.setText("Last update : " + currentDate);
      }
    }
  }
}
