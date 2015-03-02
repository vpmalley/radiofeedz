package fr.vpm.audiorss;

import android.util.Log;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 01/03/15.
 */
public class LastRefreshListener implements ProgressListener {

  private final TextView mLatestUpdate;

  private final DataModel dataModel;

  public LastRefreshListener(TextView mLatestUpdate, DataModel dataModel) {
    this.mLatestUpdate = mLatestUpdate;
    this.dataModel = dataModel;
    refreshView();
  }

  @Override
  public void startRefreshProgress() {

  }

  @Override
  public void updateProgress(int progress) {

  }

  @Override
  public void stopRefreshProgress() {
    refreshView();
  }

  private void refreshView() {
    if (mLatestUpdate != null) { // should occur only after a refresh
      Date lastUpdateDate = Calendar.getInstance().getTime();
      try {
        lastUpdateDate = new SimpleDateFormat(RSSChannel.DB_DATE_PATTERN).parse(dataModel.getLastBuildDate());
      } catch (ParseException e) {
        Log.w("date", "wrong parsing of last update date");
      }
      String currentDate = new SimpleDateFormat(RSSChannel.DISPLAY_PATTERN).format(lastUpdateDate);
      mLatestUpdate.setText("Last update : " + currentDate);
    }
  }
}
