package fr.vpm.audiorss.db;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 20/10/14.
 */
public class AsyncDbSaveMedia extends AsyncTask<Media, Integer, List<Media>> {

  private final AsyncCallbackListener<List<Media>> asyncCallbackListener;

  private final DbMedia dbUpdater;

  public AsyncDbSaveMedia(AsyncCallbackListener<List<Media>> asyncCallbackListener, Context context) {
    this.dbUpdater = new DbMedia(context, true);
    this.asyncCallbackListener = asyncCallbackListener;
    asyncCallbackListener.onPreExecute();
  }

  @Override
  protected List<Media> doInBackground(Media... medias) {
    List<Media> persistedMedias = new ArrayList<Media>();

    try {
      for (Media media : medias){
        Media persistedMedia = dbUpdater.update(media);
        persistedMedias.add(persistedMedia);
      }
    } finally {
      dbUpdater.closeDb();
    }
    return persistedMedias;
  }

  @Override
  protected void onPostExecute(List<Media> medias) {
    asyncCallbackListener.onPostExecute(medias);
  }
}
