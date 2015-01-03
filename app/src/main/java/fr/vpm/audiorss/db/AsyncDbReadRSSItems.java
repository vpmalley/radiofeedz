package fr.vpm.audiorss.db;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 20/10/14.
 */
public class AsyncDbReadRSSItems extends AsyncTask<Long, Integer, List<RSSItem>> {

  private final Context context;

  private final AsyncCallbackListener<List<RSSItem>> asyncCallbackListener;

  private final DbRSSChannel dbUpdater;

  private final List<SelectionFilter> filters;

  private boolean forceReadAll = false;

  public AsyncDbReadRSSItems(AsyncCallbackListener<List<RSSItem>> asyncCallbackListener, Context context, List<SelectionFilter> filters) {
    this.context = context;
    this.dbUpdater = new DbRSSChannel(context, false);
    this.asyncCallbackListener = asyncCallbackListener;
    this.filters = filters;
    asyncCallbackListener.onPreExecute();
  }

  public void forceReadAll(){
    forceReadAll = true;
  }

  @Override
  protected List<RSSItem> doInBackground(Long... channelIds) {
    List<RSSItem> rssItems = new ArrayList<>();
    Log.d("measures", "load it start");
    long initLoad = System.currentTimeMillis();
    try {
        rssItems = dbUpdater.readItemsAsList(forceReadAll, filters);
    } catch (ParseException e) {
      Log.e("dbIssue", e.getMessage());
    } finally {
      dbUpdater.closeDb();
    }
    Log.d("measures", "load it -end- " + String.valueOf(System.currentTimeMillis() - initLoad));
    return rssItems;
  }

  @Override
  protected void onPostExecute(List<RSSItem> rssItems) {
    asyncCallbackListener.onPostExecute(rssItems);
  }
}
