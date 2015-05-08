package fr.vpm.audiorss.db;

import android.graphics.Bitmap;

import java.util.List;

import fr.vpm.audiorss.media.PictureLoadedListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 03/11/14.
 */
public class ChannelRefreshViewCallback implements AsyncCallbackListener<List<RSSChannel>>,PictureLoadedListener {

  private final AsyncCallbackListener<List<RSSChannel>> channelsLoadedCallback;
  private final DataModel.RSSChannelDataModel dataModel;

  public ChannelRefreshViewCallback(AsyncCallbackListener<List<RSSChannel>> channelsLoadedCallback, DataModel.RSSChannelDataModel dataModel) {
    this.channelsLoadedCallback = channelsLoadedCallback;
    this.dataModel = dataModel;
  }

  @Override
  public void onPreExecute() {
    channelsLoadedCallback.onPreExecute();
  }

  @Override
  public void onPostExecute(List<RSSChannel> result) {
    dataModel.setChannelsAndBuildModel(result);
    channelsLoadedCallback.onPostExecute(result);
  }

  @Override
  public void onPictureLoaded(Bitmap pictureBitmap) {
    dataModel.refreshView();
  }

}
