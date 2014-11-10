package fr.vpm.audiorss.db;

import java.util.List;

import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 03/11/14.
 */
public class LoadDataRefreshViewCallback implements AsyncCallbackListener<List<RSSChannel>> {

  private final ProgressListener progressListener;

  private final DataModel<RSSChannel> dataModel;

  public LoadDataRefreshViewCallback(ProgressListener progressListener, DataModel<RSSChannel> dataModel) {
    this.progressListener = progressListener;
    this.dataModel = dataModel;
  }

  @Override
  public void onPreExecute() {
    progressListener.startRefreshProgress();
  }

  @Override
  public void onPostExecute(List<RSSChannel> result) {
    progressListener.stopRefreshProgress();
    dataModel.loadData();
  }
}
