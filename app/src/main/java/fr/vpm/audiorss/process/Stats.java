package fr.vpm.audiorss.process;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by vince on 16/02/16.
 */
public class Stats {

  private static final String STAT_PATH = "stats";
  private static Stats instance;

  private Properties properties;

  public static Stats get() {
    if (instance == null) {
      instance = new Stats();
      instance.load();
    }
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

  public void load() {
    try {
      properties.load(new FileInputStream(STAT_PATH));
    } catch (IOException e) {
      Log.e("stats", "overriding stats");
    }
  }

  public void save() {
    try {
      properties.store(new FileOutputStream(STAT_PATH), "stored updated props");
    } catch (IOException e) {
      Log.e("stats", "failed storing stats");
    }
  }

  public void increment(String tag) {
    properties.setProperty(tag, String.valueOf(Integer.valueOf(properties.getProperty(tag, "0")) + 1));
    save();
  }
  
}
