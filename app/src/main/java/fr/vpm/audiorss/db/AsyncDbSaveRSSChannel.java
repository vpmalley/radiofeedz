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
 */
public class AsyncDbSaveRSSChannel extends AsyncTask<RSSChannel, Integer, RSSChannel> {

  private final Context context;

  private final AsyncCallbackListener<List<RSSChannel>> asyncCallbackListener;

  private final DbRSSChannel dbUpdater;

  public AsyncDbSaveRSSChannel(AsyncCallbackListener<List<RSSChannel>> asyncCallbackListener, Context context) {
    this.context = context;
    this.dbUpdater = new DbRSSChannel(context, true);
    this.asyncCallbackListener = asyncCallbackListener;
    asyncCallbackListener.onPreExecute();
  }

  @Override
  protected RSSChannel doInBackground(RSSChannel... rssChannels) {
    if (0 == rssChannels.length) {
      return null;
    }
    RSSChannel newChannel = rssChannels[0];
    Log.d("measures", "save start ch");
    long initSave = System.currentTimeMillis();
    RSSChannel persistedChannel = newChannel.saveToDb(context);
    Log.d("measures", "save ch -end- " + String.valueOf(System.currentTimeMillis() - initSave));
    return persistedChannel;
  }

  @Override
  protected void onPostExecute(RSSChannel rssChannel) {
    List<RSSChannel> channels = new ArrayList<RSSChannel>();
    channels.add(rssChannel);
    asyncCallbackListener.onPostExecute(channels);
  }
}
