package fr.vpm.audiorss.rss;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;

import fr.vpm.audiorss.process.Cachable;
import fr.vpm.audiorss.process.ItemParser;

/**
 * Created by vince on 05/05/15.
 */
public class CachableRSSChannel implements Cachable {

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
    } catch (XmlPullParserException | IOException | ParseException e) {
      Log.w("query", e.toString());
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
        itemParser.extractRSSItems(itemParser.getThresholdDate(context));
      }
    } catch (XmlPullParserException | IOException e) {
      Log.w("query", e.toString());
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
    if (isProcessed()) {
      itemParser.getRssChannel().saveToDb(context);
    }
  }

  @Override
  public void staleStore() {

  }
}
