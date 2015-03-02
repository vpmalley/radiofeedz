package fr.vpm.audiorss;

import android.content.Context;
import android.widget.TextView;

import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.process.DateUtils;

/**
 * Created by vince on 01/03/15.
 */
public class LastRefreshListener implements ProgressListener {

  private final TextView mLatestUpdate;

  private final DataModel dataModel;

  private final Context context;

  public LastRefreshListener(TextView mLatestUpdate, DataModel dataModel, Context context) {
    this.mLatestUpdate = mLatestUpdate;
    this.dataModel = dataModel;
    this.context = context;
    mLatestUpdate.setText("");
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
    String currentDate = DateUtils.getDisplayDate(dataModel.getLastBuildDate());
    mLatestUpdate.setText(context.getString(R.string.last_refresh) + " : " + currentDate);
  }
}
