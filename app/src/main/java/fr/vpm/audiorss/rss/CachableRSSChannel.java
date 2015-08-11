package fr.vpm.audiorss.rss;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

import fr.vpm.audiorss.process.Cachable;
import fr.vpm.audiorss.process.DateUtils;
import fr.vpm.audiorss.process.ItemParser;

/**
 * Created by vince on 05/05/15.
 */
public class CachableRSSChannel implements Cachable {

  private static final int PROCESS_MAX_ITEMS = 3;
  private ItemParser itemParser;

  private RSSChannel initialRSSChannel;

  private String initialRSSUrl;

  private boolean failed;

  public CachableRSSChannel(RSSChannel rssChannel) {
    this.initialRSSChannel = rssChannel;
  }

  public CachableRSSChannel(String rssUrl) {
    this.initialRSSUrl = rssUrl;
  }

  public boolean failed() {
    return failed;
  }

  @Override
  public boolean shouldRefresh() {
    boolean shouldRefresh = true;
    if (initialRSSChannel != null) {
      shouldRefresh = initialRSSChannel.shouldRefresh();
    }
    return shouldRefresh;
  }

  @Override
  public void query(Context context) {
    try {
      if (initialRSSChannel != null) {
        itemParser = ItemParser.retrieveFeedContent(initialRSSChannel);
      } else if (initialRSSUrl != null) {
        itemParser = ItemParser.retrieveFeedContent(initialRSSUrl);
      }
    } catch (Exception e) {
      Log.w("cache-query", e.toString());
      failed = true;
    }
    if (itemParser == null) {
      failed = true;
    }
  }

  @Override
  public boolean isQueried() {
    return itemParser != null;
  }

  @Override
  public void process(Context context) {
    try {
      if (isQueried()) {
        itemParser.extractRSSItems(itemParser.getThresholdDate(context), PROCESS_MAX_ITEMS);
      }
    } catch (Exception e) {
      Log.w("cache-process", e.toString());
      failed = true;
    }
    if (!itemParser.extractedItems()) {
      failed = true;
    }
  }

  @Override
  public boolean isProcessed() {
    return (itemParser != null) && itemParser.extractedItems();
  }

  @Override
  public void persist(Context context) {
    try {
    if (isProcessed()) {
      itemParser.persistRSSChannel(context);
    }
    } catch (Exception e) {
      Log.w("cache-persist", e.toString());
      failed = true;
    }
  }

  @Override
  public void postProcess(Context context) {
    try {
      if (isQueried()) {
        String thresholdDate = "";
        if (initialRSSUrl != null) { // if this is a new feed
          Calendar lastYear = Calendar.getInstance();
          lastYear.add(Calendar.YEAR, -1);
          thresholdDate = DateUtils.formatDBDate(lastYear.getTime());
        } else {
          thresholdDate = itemParser.getThresholdDate(context);
        }
        itemParser.extractRSSItems(thresholdDate);
      }
    } catch (Exception e) {
      Log.w("cache-post-process", e.toString());
      failed = true;
    }
    if (!itemParser.extractedItems()) {
      failed = true;
    }
  }

  @Override
  public void staleStore() {

  }
}
