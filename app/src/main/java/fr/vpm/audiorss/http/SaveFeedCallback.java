package fr.vpm.audiorss.http;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.media.AsyncPictureLoader;
import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.media.PictureLoadedListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 06/11/14.
 */
public class SaveFeedCallback implements AsyncCallbackListener<RSSChannel> {

  private final ProgressListener progressListener;

  private final DataModel dataModel;

  private final AsyncCallbackListener<List<RSSChannel>> rssChannelCallback;

  public SaveFeedCallback(ProgressListener progressListener, DataModel dataModel, AsyncCallbackListener<List<RSSChannel>> rssChannelCallback) {
    this.progressListener = progressListener;
    this.dataModel = dataModel;
    this.rssChannelCallback = rssChannelCallback;
  }

  @Override
  public void onPreExecute() {
    progressListener.startRefreshProgress();
  }

  @Override
  public void onPostExecute(RSSChannel result) {
    progressListener.stopRefreshProgress();
    if (result != null) {
      try {
        result.asyncSaveToDb(progressListener, dataModel, rssChannelCallback);
      } catch (Exception e) {
        handleException(e);
        dataModel.onFeedFailureBeforeLoad();
      }
    } else {
      dataModel.onFeedFailureBeforeLoad();
    }
    //downloadImages(result);
  }

  private void downloadImages(RSSChannel result) {
    List<Media> pictures = new ArrayList<>();
    if (result != null) {
      for (RSSItem rssItem : result.getItems()) {
        if ((rssItem != null) && (rssItem.getMedia().isPicture())) {
          pictures.add(rssItem.getMedia());
        }
      }
    }
    Media[] images = new Media[pictures.size()];
    for (int i = 0; i < pictures.size(); i++) {
      images[i] = pictures.get(i);
    }
    Log.d("dlPics", "downloading " + images.length + " pictures");
    if (images.length > 0) {
      AsyncPictureLoader pictureLoader = new AsyncPictureLoader(new ArrayList<PictureLoadedListener>(), 300, 300,
              dataModel.getContext(), Media.Folder.INTERNAL_ITEMS_PICS);
      pictureLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, images);
    }
  }

  private void handleException(Exception e) {
    Toast.makeText(dataModel.getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    Log.e("Exception", e.toString());
  }

}
