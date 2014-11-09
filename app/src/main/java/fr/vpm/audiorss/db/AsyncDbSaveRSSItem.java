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
public class AsyncDbSaveRSSItem extends AsyncTask<RSSItem, Integer, List<RSSItem>> {

  private final Context context;

  private final AsyncCallbackListener<List<RSSItem>> asyncCallbackListener;

  private final DbRSSChannel dbUpdater;

  public AsyncDbSaveRSSItem(AsyncCallbackListener<List<RSSItem>> asyncCallbackListener, Context context) {
    this.context = context;
    this.dbUpdater = new DbRSSChannel(context);
    this.asyncCallbackListener = asyncCallbackListener;
    asyncCallbackListener.onPreExecute();
  }

  @Override
  protected List<RSSItem> doInBackground(RSSItem... rssItems) {
    List<RSSItem> persistedRssItems = new ArrayList<RSSItem>();

    try {
      for (RSSItem rssItem : rssItems){
        RSSItem persistedRSSItem = dbUpdater.addOrUpdate(rssItem, -1);
        persistedRssItems.add(persistedRSSItem);
      }
    } finally {
      dbUpdater.closeDb();
    }
    return persistedRssItems;
  }

  @Override
  protected void onPostExecute(List<RSSItem> rssItems) {
    asyncCallbackListener.onPostExecute(rssItems);
  }
}
