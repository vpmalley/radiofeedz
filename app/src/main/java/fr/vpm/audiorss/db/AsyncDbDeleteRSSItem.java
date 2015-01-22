package fr.vpm.audiorss.db;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 20/10/14.
 */
public class AsyncDbDeleteRSSItem extends AsyncTask<RSSItem, Integer, List<RSSItem>> {

  private final AsyncCallbackListener<List<RSSItem>> asyncCallbackListener;

  private final DbRSSChannel dbUpdater;

  public AsyncDbDeleteRSSItem(AsyncCallbackListener<List<RSSItem>> asyncCallbackListener, Context context) {
    this.asyncCallbackListener = asyncCallbackListener;
    this.dbUpdater = new DbRSSChannel(context, true);
    this.asyncCallbackListener.onPreExecute();
  }

  @Override
  protected List<RSSItem> doInBackground(RSSItem... rssItems) {
    List<RSSItem> deletedItems  = new ArrayList<RSSItem>();
    if (rssItems.length > 0) {
      for (RSSItem item : rssItems){
        if (item.getDbId() > -1) {
          dbUpdater.deleteItemAndItsMedia(item);
          deletedItems.add(item);
        }
      }
    }
    Log.d("items", "deleted " + deletedItems.size());
    return deletedItems;
  }

  @Override
  protected void onPostExecute(List<RSSItem> rssChannels) {
    asyncCallbackListener.onPostExecute(rssChannels);
  }
}
