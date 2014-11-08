package fr.vpm.audiorss.db;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 20/10/14.
 */
public class AsyncDbDeleteRSSChannel extends AsyncTask<RSSChannel, Integer, List<RSSChannel>> {

  private final AsyncCallbackListener<List<RSSChannel>> asyncCallbackListener;

  private final DbRSSChannel dbUpdater;

  public AsyncDbDeleteRSSChannel(AsyncCallbackListener<List<RSSChannel>> asyncCallbackListener, Context context) {
    this.asyncCallbackListener = asyncCallbackListener;
    this.dbUpdater = new DbRSSChannel(context);
    this.asyncCallbackListener.onPreExecute();
  }

  @Override
  protected List<RSSChannel> doInBackground(RSSChannel... rssChannels) {
    List<RSSChannel> deletedChannels  = new ArrayList<RSSChannel>();
    if (rssChannels.length > 0) {
      for (RSSChannel channel : rssChannels){
        if (channel.getId() > -1) {
          dbUpdater.deleteById(channel.getId());
          deletedChannels.add(channel);
        }
      }
    }
    return deletedChannels;
  }

  @Override
  protected void onPostExecute(List<RSSChannel> rssChannels) {
    asyncCallbackListener.onPostExecute(rssChannels);
  }
}
