package fr.vpm.audiorss.db;

import java.util.List;

import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 03/11/14.
 */
public class ItemRefreshViewCallback implements AsyncCallbackListener<List<RSSItem>> {

  private final ProgressListener progressListener;

  private final DataModel.RSSItemDataModel dataModel;

  public ItemRefreshViewCallback(ProgressListener progressListener, DataModel.RSSItemDataModel dataModel) {
    this.progressListener = progressListener;
    this.dataModel = dataModel;
  }

  @Override
  public void onPreExecute() {
    progressListener.startRefreshProgress();
  }

  @Override
  public void onPostExecute(List<RSSItem> result) {
    progressListener.stopRefreshProgress();
    dataModel.setItemsAndBuildModel(result);
    if (dataModel.isReady()) {
      dataModel.refreshView();
    }
  }
}
