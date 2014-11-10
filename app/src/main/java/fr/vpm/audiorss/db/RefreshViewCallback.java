package fr.vpm.audiorss.db;

import android.graphics.Bitmap;

import java.util.List;

import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.media.PictureLoadedListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;

/**
 * Created by vince on 03/11/14.
 */
public class RefreshViewCallback<T> implements AsyncCallbackListener<List<T>>,PictureLoadedListener {

  private final ProgressListener progressListener;

  private final DataModel<T> dataModel;

  public RefreshViewCallback(ProgressListener progressListener, DataModel<T> dataModel) {
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
    dataModel.setDataAndBuildModel(result);
    dataModel.refreshView();
  }

  @Override
  public void onPictureLoaded(Bitmap pictureBitmap) {
    dataModel.refreshView();
  }

}
