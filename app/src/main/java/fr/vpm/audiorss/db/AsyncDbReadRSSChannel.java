package fr.vpm.audiorss.db;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 20/10/14.
 * <p/>
 * Reads the DB for RSSChannel items. The expected String is a url. If none is passed, all RSSChannel items are searched.
 */
public class AsyncDbReadRSSChannel extends AsyncTask<Long, Integer, List<RSSChannel>> {

  private final AsyncCallbackListener<List<RSSChannel>> asyncCallbackListener;

  private final DbRSSChannel dbReader;

  private final boolean readItems;

  public AsyncDbReadRSSChannel(AsyncCallbackListener<List<RSSChannel>> callbackListener, Context context, boolean readItems) {
    this.asyncCallbackListener = callbackListener;
    this.dbReader = new DbRSSChannel(context, false);
    this.readItems = readItems;
    this.asyncCallbackListener.onPreExecute();
  }

  @Override
  protected List<RSSChannel> doInBackground(Long... ids) {
    List<RSSChannel> foundChannels = new ArrayList<RSSChannel>();
    Log.d("measures", "load ch start");
    long initLoad = System.currentTimeMillis();
    try {
      if (0 == ids.length) { // if no argument, read all RSSChannel in DB
        foundChannels.addAll(dbReader.readAll(readItems));
      } else { // unused branch
        for (long id : ids){
          foundChannels.add(dbReader.readById(id, readItems, false));
        }
      }
    } catch (ParseException e) {
      Log.e("dbIssue", e.getMessage());
    } finally {
      dbReader.closeDb();
    }
    Log.d("measures", "load ch -end- " + String.valueOf(System.currentTimeMillis() - initLoad));
    return foundChannels;
  }

  @Override
  protected void onPostExecute(List<RSSChannel> rssChannels) {
    this.asyncCallbackListener.onPostExecute(rssChannels);
  }
}
