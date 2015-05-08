package fr.vpm.audiorss.db;

import java.util.List;

import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 03/11/14.
 */
public class ItemRefreshViewCallback implements AsyncCallbackListener<List<RSSItem>> {

  private final AsyncCallbackListener<List<RSSItem>> itemsLoadedCallback;

  private final DataModel.RSSItemDataModel dataModel;

  public ItemRefreshViewCallback(AsyncCallbackListener<List<RSSItem>> itemsLoadedCallback, DataModel.RSSItemDataModel dataModel) {
    this.itemsLoadedCallback = itemsLoadedCallback;
    this.dataModel = dataModel;
  }

  @Override
  public void onPreExecute() {
    itemsLoadedCallback.onPreExecute();
  }

  @Override
  public void onPostExecute(List<RSSItem> result) {
    dataModel.setItemsAndBuildModel(result);
    itemsLoadedCallback.onPostExecute(result);
  }
}
