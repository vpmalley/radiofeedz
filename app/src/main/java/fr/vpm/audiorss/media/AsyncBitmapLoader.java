package fr.vpm.audiorss.media;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 17/03/15.
 */
public class AsyncBitmapLoader extends AsyncTask<RSSItem, Integer, List<Media>> {

  private final Context context;

  public AsyncBitmapLoader(Context context) {
    this.context = context;
  }

  @Override
  protected List<Media> doInBackground(RSSItem... rssItems) {
    Log.d("bitmaploader", String.valueOf(rssItems.length));
    List<Media> loadedMedias = new ArrayList<>();
    for (RSSItem rssItem : rssItems) {
      Media media = rssItem.getMedia();
      if ((media != null) && (media.isPicture())) {
        media.getAsBitmap(context, new ArrayList<PictureLoadedListener>(), Media.Folder.INTERNAL_ITEMS_PICS);
        loadedMedias.add(media);
      }
    }
    return loadedMedias;
  }
}
