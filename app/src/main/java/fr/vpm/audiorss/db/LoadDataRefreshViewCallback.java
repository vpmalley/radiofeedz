package fr.vpm.audiorss.db;

import java.util.List;

import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 03/11/14.
 */
public class LoadDataRefreshViewCallback<T> implements AsyncCallbackListener<List<T>> {

  private final ProgressListener progressListener;

  private final DataModel dataModel;

  private final AsyncCallbackListener<List<RSSItem>> itemsLoadedCallback;

  private final AsyncCallbackListener<List<RSSChannel>> channelsLoadedCallback;

  private final AsyncCallbackListener<List<T>> loadCallback;

  public LoadDataRefreshViewCallback(ProgressListener progressListener, DataModel dataModel, AsyncCallbackListener<List<RSSItem>> itemsLoadedCallback, AsyncCallbackListener<List<RSSChannel>> channelsLoadedCallback, AsyncCallbackListener<List<T>> loadCallback) {
    this.progressListener = progressListener;
    this.dataModel = dataModel;
    this.itemsLoadedCallback = itemsLoadedCallback;
    this.channelsLoadedCallback = channelsLoadedCallback;
    this.loadCallback = loadCallback;
  }

  @Override
  public void onPreExecute() {
    progressListener.startRefreshProgress();
  }

  @Override
  public void onPostExecute(List<T> result) {
    progressListener.stopRefreshProgress();
    dataModel.loadData(itemsLoadedCallback, channelsLoadedCallback, loadCallback);
  }
}
