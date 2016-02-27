package fr.vpm.audiorss.process;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 * Created by vince on 16/02/16.
 */
public class Stats {

  private static final String STAT_PATH = "stats";
  private static Stats instance;

  private Properties properties;
  private Context context;

  public static Stats get(Context context) {
    if (instance == null) {
      instance = new Stats();
      instance.load(context);
    }
    instance.context = context;
    return instance;
  }

  public static final String ACTION_FEED_FILTER = "ACTION_FEED_FILTER";
  public static final String ACTION_FEED_ADD = "ACTION_FEED_ADD";
  public static final String ACTION_FEED_DELETE = "ACTION_FEED_DELETE";
  public static final String ACTION_SEARCH = "ACTION_FEED_SEARCH";

  public static final String ACTION_REFRESH = "ACTION_REFRESH";
  public static final String ACTION_CATALOG = "ACTION_CATALOG";
  public static final String ACTION_SETTINGS = "ACTION_SETTINGS";
  public static final String ACTION_PLAYLIST = "ACTION_PLAYLIST";

  public static final String ACTION_READ = "ACTION_ITEM_READ";
  public static final String ACTION_MARK_READ = "ACTION_ITEM_MARK_READ";
  public static final String ACTION_DOWNLOAD = "ACTION_ITEM_DOWNLOAD";
  public static final String ACTION_WEB = "ACTION_ITEM_WEB";
  public static final String ACTION_PLAY = "ACTION_ITEM_PLAY";
  public static final String ACTION_DELETE = "ACTION_ITEM_DELETE";
  public static final String ACTION_ARCHIVE = "ACTION_ITEM_ARCHIVE";

  public Stats() {
    properties = new Properties();
  }

  public void load(Context context) {
    try {
      properties.load(context.openFileInput(STAT_PATH));
    } catch (IOException e) {
      Log.e("stats", "overriding stats");
    }
  }

  public void save() {
    try {
      properties.store(context.openFileOutput(STAT_PATH, Context.MODE_PRIVATE), "stored updated props");
    } catch (IOException e) {
      Log.e("stats", "failed storing stats");
    }
    context = null;
  }

  public void increment(String tag) {
    properties.setProperty(tag, String.valueOf(Integer.valueOf(properties.getProperty(tag, "0")) + 1));
    save();
  }

  public void empty() {
    properties.clear();
    save();
  }

  public void pushStats(final Context context) {
    new AsyncTask<String, Integer, String>() {

      @Override
      protected String doInBackground(String... strings) {
        CloudantClient client = ClientBuilder.account("radiofeedz")
            .username("therippeceinguagaingives")
            .password("c5b5215a47fe452dff234db9ffd755e59899a349")
            .build();

        Database analyticsDB = client.database("rf_analytics", false);
        Stats stats = Stats.get(null);
        analyticsDB.save(stats);
        Log.i("analytics", "pushed a doc");
        return null;
      }

      @Override
      protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Stats.get(context).empty();
      }
    }.execute("");
  }

  @Override
  public String toString() {
    Date today = new Date();
    StringBuilder jsonStats =  new StringBuilder("{ \"version\" : \"1.1.7\",\n  \"date\" : \"");
    jsonStats.append(today.toString());
    jsonStats.append("\",\n  \"tags\" : {\n");
    for (String propKey : properties.stringPropertyNames()) {
      String propValue = properties.getProperty(propKey, "");
      jsonStats.append("\"");
      jsonStats.append(propKey);
      jsonStats.append("\" : \"");
      jsonStats.append(propValue);
      jsonStats.append("\",\n");
    }
    jsonStats.append("\"lala\" : \"lolo\"\n");
    jsonStats.append("}\n}");

    return jsonStats.toString();
  }
}
