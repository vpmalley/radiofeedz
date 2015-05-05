package fr.vpm.audiorss.db;

import java.util.List;

import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.process.TaskManager;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 03/11/14.
 */
public class ItemRefreshViewCallback implements AsyncCallbackListener<List<RSSItem>> {

  private final AsyncCallbackListener<List<RSSItem>> itemsLoadedCallback;

  private final ProgressListener progressListener;

  private final DataModel.RSSItemDataModel dataModel;

  public ItemRefreshViewCallback(AsyncCallbackListener<List<RSSItem>> itemsLoadedCallback, ProgressListener progressListener, DataModel.RSSItemDataModel dataModel) {
    this.itemsLoadedCallback = itemsLoadedCallback;
    this.progressListener = progressListener;
    this.dataModel = dataModel;
  }

  @Override
  public void onPreExecute() {
    progressListener.startRefreshProgress();
    itemsLoadedCallback.onPreExecute();
  }

  @Override
  public void onPostExecute(List<RSSItem> result) {
    progressListener.stopRefreshProgress();
    dataModel.setItemsAndBuildModel(result);
    itemsLoadedCallback.onPostExecute(result);
    TaskManager manager = TaskManager.getManager();
    manager.queueTask(new TaskManager.Task() {
      @Override
      public void execute() {
        dataModel.postProcessData();
      }
    });
    manager.onPostExecute(null);
  }
}
