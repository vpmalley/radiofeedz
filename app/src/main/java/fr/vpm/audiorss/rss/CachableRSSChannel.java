package fr.vpm.audiorss.rss;

import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;

import fr.vpm.audiorss.exception.RetrieveException;
import fr.vpm.audiorss.process.Cachable;
import fr.vpm.audiorss.process.ItemParser;

/**
 * Created by vince on 05/05/15.
 */
public class CachableRSSChannel implements Cachable {

  private static final int PROCESS_MAX_ITEMS = 3;
  private ItemParser itemParser;

  private RSSChannel initialRSSChannel;

  private String initialRSSUrl;

  public CachableRSSChannel(RSSChannel rssChannel) {
    this.initialRSSChannel = rssChannel;
  }

  public CachableRSSChannel(String rssUrl) {
    this.initialRSSUrl = rssUrl;
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
  public void query(Context context) throws RetrieveException, ParseException, XmlPullParserException, IOException {
    if (initialRSSChannel != null) {
      itemParser = ItemParser.retrieveFeedContent(initialRSSChannel);
    } else if (initialRSSUrl != null) {
      itemParser = ItemParser.retrieveFeedContent(initialRSSUrl);
    }
    if (itemParser == null) {
      throw new RetrieveException(initialRSSChannel);
    }
  }

  @Override
  public boolean isQueried() {
    return itemParser != null;
  }

  @Override
  public void process(Context context) throws IOException, XmlPullParserException, RetrieveException {
    if (isQueried()) {
      if (initialRSSUrl != null) {
        itemParser.extractRSSItems(itemParser.getThresholdDate(context, true), 0);
      } else {
        itemParser.extractRSSItems(itemParser.getThresholdDate(context, false), PROCESS_MAX_ITEMS);
      }
    }
    if (!itemParser.extractedItems()) {
      if (initialRSSChannel != null) {
        throw new RetrieveException(initialRSSChannel);
      } else {
        throw new RetrieveException(itemParser.getRssChannel());
      }
    }
  }

  @Override
  public boolean isProcessed() {
    return (itemParser != null) && itemParser.extractedItems();
  }

  @Override
  public void persist(Context context) {
    if (isProcessed()) {
      itemParser.persistRSSChannel(context);
    }
  }

  @Override
  public void postProcess(Context context) throws IOException, XmlPullParserException {
    if (isQueried()) {
      String thresholdDate = "";
      if (initialRSSUrl != null) {
        thresholdDate = itemParser.getThresholdDate(context, true);
      } else {
        thresholdDate = itemParser.getThresholdDate(context, false);
      }
      itemParser.extractRSSItems(thresholdDate);
    }
  }

  public RSSChannel getRSSChannel() {
    return itemParser.getRssChannel();
  }
}
