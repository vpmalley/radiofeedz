package fr.vpm.audiorss.db;

import android.os.AsyncTask;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 20/10/14.
 * <p/>
 * Reads the DB for RSSChannel items. The expected String is a url. If none is passed, all RSSChannel items are searched.
 */
public class AsyncDbReadRSSChannel extends AsyncTask<String, Integer, List<RSSChannel>> {

  private final FeedsActivity activity;

  private final DbRSSChannel dbReader;

  public AsyncDbReadRSSChannel(FeedsActivity feedsActivity) {
    this.activity = feedsActivity;
    this.dbReader = new DbRSSChannel(feedsActivity);
    activity.startRefreshProgress();
  }

  @Override
  protected List<RSSChannel> doInBackground(String... url) {
    List<RSSChannel> foundChannels = new ArrayList<RSSChannel>();
    Log.d("measures", "load start " + String.valueOf(System.currentTimeMillis()));
    try {
      if (0 == url.length) { // if no argument, read all RSSChannel in DB
        foundChannels.addAll(dbReader.readAll());
      } else {
        foundChannels.add(dbReader.readByUrl(url[0]));
      }
    } catch (ParseException e) {
      Log.e("dbIssue", e.getMessage());
    } finally {
      dbReader.closeDb();
    }
    Log.d("measures", "load -end- " + String.valueOf(System.currentTimeMillis()));
    return foundChannels;
  }

  @Override
  protected void onPostExecute(List<RSSChannel> rssChannels) {
    activity.setChannels(rssChannels);
    activity.refreshView();
    activity.stopRefreshProgress();
  }
}
