package fr.vpm.audiorss.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.ItemComparator;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 20/10/14.
 */
public class AsyncDbReadRSSItems extends AsyncTask<Long, Integer, List<RSSItem>> {

  private final Context context;

  private final AsyncCallbackListener<List<RSSItem>> asyncCallbackListener;

  private final DbRSSChannel dbUpdater;

  public AsyncDbReadRSSItems(AsyncCallbackListener<List<RSSItem>> asyncCallbackListener, Context context) {
    this.context = context;
    this.dbUpdater = new DbRSSChannel(context);
    this.asyncCallbackListener = asyncCallbackListener;
    asyncCallbackListener.onPreExecute();
  }

  @Override
  protected List<RSSItem> doInBackground(Long... channelIds) {
    Map<String, RSSItem> rssItems = new HashMap<String, RSSItem>();
    try {
      if (0 == channelIds.length) {
        rssItems.putAll(dbUpdater.readItems(RSSItem.DELETED_KEY + "=0", null));
      } else {
        for (long channelId : channelIds){
          rssItems.putAll(dbUpdater.readItemsByChannelId(channelId));
        }
      }
    } catch (ParseException e) {
      Log.e("dbIssue", e.getMessage());
    } finally {
      dbUpdater.closeDb();
    }
    SharedPreferences sharedPref = PreferenceManager
        .getDefaultSharedPreferences(context);
    String ordering = sharedPref.getString("pref_feed_ordering", "REVERSE_TIME");
    SortedSet<RSSItem> allItems = new TreeSet<RSSItem>(new ItemComparator(ordering));
    allItems.addAll(rssItems.values());
    return new ArrayList<RSSItem>(allItems);
  }

  @Override
  protected void onPostExecute(List<RSSItem> rssItems) {
    asyncCallbackListener.onPostExecute(rssItems);
  }
}
