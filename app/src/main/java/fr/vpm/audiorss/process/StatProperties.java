package fr.vpm.audiorss.process;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by vince on 16/02/16.
 */
public class StatProperties {

  private static final String STAT_PATH = "stats";
  private static StatProperties instance;

  private Properties properties;

  public StatProperties getInstance() {
    if (instance == null) {
      instance = new StatProperties();
      instance.load();
    }
    return instance;
  }

  public StatProperties() {
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
    properties.put(tag, properties.get(tag) + 1);
    save();
  }
  
}
