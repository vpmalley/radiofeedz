package fr.vpm.audiorss.db;

import java.util.List;

import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;

/**
 * Created by vince on 03/11/14.
 */
public class LoadDataRefreshViewCallback<T> implements AsyncCallbackListener<List<T>> {

  private final ProgressListener progressListener;

  private final DataModel dataModel;

  public LoadDataRefreshViewCallback(ProgressListener progressListener, DataModel dataModel) {
    this.progressListener = progressListener;
    this.dataModel = dataModel;
  }

  @Override
  public void onPreExecute() {
    progressListener.startRefreshProgress();
  }

  @Override
  public void onPostExecute(List<T> result) {
    progressListener.stopRefreshProgress();
    dataModel.loadData();
  }
}
