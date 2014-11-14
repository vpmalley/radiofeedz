package fr.vpm.audiorss.db;

import android.content.Context;
import android.os.AsyncTask;

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
    List<RSSItem> deletedChannels  = new ArrayList<RSSItem>();
    if (rssItems.length > 0) {
      for (RSSItem item : rssItems){
        if (item.getDbId() > -1) {
          dbUpdater.deleteById(item.getDbId());
          deletedChannels.add(item);
        }
      }
    }
    return deletedChannels;
  }

  @Override
  protected void onPostExecute(List<RSSItem> rssChannels) {
    asyncCallbackListener.onPostExecute(rssChannels);
  }
}
