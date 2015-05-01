package fr.vpm.audiorss.process;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

import org.apache.commons.io.IOUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class ItemParser {

  private static final String CHANNEL_TAG = "channel";
  private static final String RSS_TAG = "rss";
  private static final String ITEM_TAG = "item";

  private RSSChannel rssChannel;

  private List<String> rssItems = new ArrayList<>();

  private AsyncCallbackListener<RSSChannel> asyncCallbackListener;

  public ItemParser(RSSChannel rssChannel, List<String> rssItems) {
    this.rssChannel = rssChannel;
    this.rssItems = rssItems;
  }

  public RSSChannel getRssChannel() {
    return rssChannel;
  }

  public void setCallback(AsyncCallbackListener<RSSChannel> asyncCallbackListener) {
    this.asyncCallbackListener = asyncCallbackListener;
  }

  public void callback() {
    if (asyncCallbackListener != null) {
      asyncCallbackListener.onPostExecute(getRssChannel());
    }
  }

  public static ItemParser retrieveFeedContent(String rssUrl) throws XmlPullParserException,
      IOException, ParseException {
    String channel = "";
    List<String> items = new ArrayList<>();
    String feedContent = getFeedContent(rssUrl);

    for (String token : feedContent.split("<item>")) {
      if (token.contains("</item>")) {
        items.add("<item>" + token);
        int lastIndex = token.indexOf("</item>") + "</item>".length();
        if ((token.length() > lastIndex) && (token.substring(lastIndex).contains("</channel>"))) {
          channel += token.substring(lastIndex, token.indexOf("</channel>"));
        }
      } else {

        int start = token.indexOf("<channel>");
        if (start < 0) {
          Log.d("parsing", rssUrl + " - " + token);
          start = 0;
        }
        channel += token.substring(start);
      }
    }
    channel += "</channel>";
    RSSChannel rssChannel = extractRSSChannel(rssUrl, channel);
    return new ItemParser(rssChannel, items);
  }

  public static ItemParser retrieveFeedContent(RSSChannel rssChannel) throws XmlPullParserException,
      IOException, ParseException {
    List<String> items = new ArrayList<>();
    String feedContent = getFeedContent(rssChannel.getUrl());

    for (String token : feedContent.split("<item>")) {
      if (token.contains("</item>")) {
        items.add("<item>" + token);
      }
    }
    return new ItemParser(rssChannel, items);
  }

  private static String getFeedContent(String rssUrl) throws IOException {
    InputStream in = null;
    HttpURLConnection urlConnection = null;
    String content;

    try {
      URL formattedUrl = new URL(rssUrl);
      urlConnection = (HttpURLConnection) formattedUrl.openConnection();
      urlConnection.setConnectTimeout(10000);
      in = new BufferedInputStream(urlConnection.getInputStream());

      StringWriter sw = new StringWriter();
      IOUtils.copy(in, sw, Charset.defaultCharset());
      content = sw.toString();
    } finally {
      if (in != null) {
        in.close();
      }
    }
    return content;
  }

  /**
   * Determines when to refresh for this feed the next time
   * @param itemDates the list of dates of items,
   *                  formatted as {@link fr.vpm.audiorss.process.DateUtils#DB_DATE_PATTERN} and with the US locale
   * @return the date the feed should be refreshed the next time,
   *         formatted as {@link fr.vpm.audiorss.process.DateUtils#DB_DATE_PATTERN} and with the US locale
   */
  private String getWhenToRefreshNext(List<String> itemDates) {
    // if no less than 3 items in the feed, the average is probably not trustable
    if (itemDates.size() < 3) {
      return DateUtils.formatDBDate(Calendar.getInstance().getTime());
    }

    SortedSet<String> sortedDates = new TreeSet<>(itemDates);

    String earliest = sortedDates.first();
    String latest = sortedDates.last();

    long totalTime = 0;
    long latestPublished = 0;
    try {
      Date latestItem = DateUtils.parseDBDate(latest);
      Date earliestItem = DateUtils.parseDBDate(earliest);
      totalTime = latestItem.getTime() - earliestItem.getTime();
      latestPublished = latestItem.getTime();
    } catch (ParseException e) {
      Log.w("dateParsing", e.toString());
    }

    long average = totalTime / itemDates.size();
    Log.d("average", ((float) average / 3600000 / 24 ) + " days");

    // starting another strategy
    String previousDate = sortedDates.first();
    totalTime = 0;
    for (String date : sortedDates) {
      try {
        Date previousItem = DateUtils.parseDBDate(previousDate);
        Date currentItem = DateUtils.parseDBDate(date);
        long betweenTwo = currentItem.getTime() - previousItem.getTime();
        if (betweenTwo < (average * 1.2)) {
          totalTime += betweenTwo;
        }
      } catch (ParseException e) {
        Log.w("dateParsing", e.toString());
      }
      previousDate = date;
    }
    long newAverage = totalTime / itemDates.size();
    Log.d("average", ((float) newAverage / 3600000 / 24 ) + " days (new computation)");
    // ending the other strategy

    long nextRefresh = latestPublished + (long)( newAverage / 1.2) ;
    String formattedNextRefresh = DateUtils.formatDBDate(new Date(nextRefresh));

    return formattedNextRefresh;
  }

  /**
   * Retrieves from preferences the threshold date before which no item should be kept.
   * @param context the current Android context
   * @return the threshold date
   */
  public String getThresholdDate(Context context) {
    String itemsExpiryTime = PreferenceManager.getDefaultSharedPreferences(context).
        getString("pref_items_deletion", "30");
    if (!Pattern.compile("\\d+").matcher(itemsExpiryTime).matches()){
      itemsExpiryTime = "30";
    }
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DAY_OF_YEAR, -1 * Integer.valueOf(itemsExpiryTime));
    return DateUtils.formatDBDate(yesterday.getTime());
  }

  /**
   * Extracts a RSSChannel from a String containing its XML representation
   * @param rssUrl the url to the file that contained the RSS content
   * @param channel the XML content containing the channel description (root tag is "channel")
   * @return a {@link RSSChannel}
   * @throws XmlPullParserException
   * @throws IOException
   * @throws ParseException
   */
  static RSSChannel extractRSSChannel(String rssUrl, String channel) throws XmlPullParserException, IOException, ParseException {
    RSSChannel rssChannel;
    InputStream channelStream = null;
    try {
      channelStream = new ByteArrayInputStream(channel.getBytes("UTF-8"));
      XmlPullParser channelParser = Xml.newPullParser();
      channelParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      channelParser.setInput(channelStream, null);
      channelParser.nextTag();
      rssChannel = readFeed(channelParser, rssUrl);
    } finally {
      if (channelStream != null) {
        channelStream.close();
      }
    }
    return rssChannel;
  }

  /**
   * Extracts RSSItems from Strings containing their XML representation
   * @param thresholdDate the date before which no item should be kept
   * @throws XmlPullParserException
   * @throws IOException
   */
  public void extractRSSItems(String thresholdDate) throws XmlPullParserException, IOException {
    extractRSSItems(thresholdDate, -1);
  }

  /**
   * Extracts RSSItems from Strings containing their XML representation
   * @param thresholdDate the date before which no item should be kept
   * @param maxItems the number of items to extract, at most. If set to 0 or less, all items will be extracted
   * @throws XmlPullParserException
   * @throws IOException
   */
  public void extractRSSItems(String thresholdDate, int maxItems) throws XmlPullParserException, IOException {
    Map<String, RSSItem> allItems = new HashMap<>();
    List<String> itemDates = new ArrayList<>();


    List<String> itemsToParse = new ArrayList<>();
    if (maxItems > 0) {
      itemsToParse.addAll(rssItems.subList(0, Math.min(rssItems.size(), maxItems)));
    } else {
      itemsToParse.addAll(rssItems);
    }

    while (!rssItems.isEmpty()) {
    //for (String item : itemsToParse) {
      String item = rssItems.get(0);
      InputStream itemStream = null;
      try {
        itemStream = new ByteArrayInputStream(item.getBytes("UTF-8"));
        XmlPullParser itemParser = Xml.newPullParser();
        itemParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        itemParser.setInput(itemStream, null);
        itemParser.nextTag();

        RSSItem rssItem = readEntry(itemParser, rssChannel.getTitle());
        itemDates.add(rssItem.getDate());
        // if item date is after the threshold date
        if (thresholdDate.compareTo(rssItem.getDate()) < 0) {
          if (rssItem.getId() != null) {
            allItems.put(rssItem.getId(), rssItem);
          } else {
            allItems.put(rssItem.getLink(), rssItem);
          }
          rssItems.remove(item);
        } else {
          // once one item is before threshold date, stop parsing them
          rssItems.clear();
        }
      } finally {
        if (itemStream != null) {
          itemStream.close();
        }
      }
    }

    rssChannel.update(DateUtils.formatDBDate(Calendar.getInstance().getTime()), allItems);
    String whenToRefreshNext = getWhenToRefreshNext(itemDates);
    rssChannel.setNextRefresh(whenToRefreshNext);

  }

  private static RSSChannel readFeed(XmlPullParser parser, String rssUrl) throws XmlPullParserException,
      IOException, ParseException {
    String title = "";
    String link = "";
    String description = "";
    String category = "";
    String imageUrl = "";

    parser.require(XmlPullParser.START_TAG, null, CHANNEL_TAG);
    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String tagName = parser.getName();
      if (tagName.equals(RSSChannel.TITLE_TAG)) {
        title = readTagContent(parser, RSSChannel.TITLE_TAG);
      } else if (tagName.equals(RSSChannel.LINK_TAG)) {
        link = readTagContent(parser, RSSChannel.LINK_TAG);
      } else if (tagName.equals(RSSChannel.DESC_TAG)) {
        description = readTagContent(parser, RSSChannel.DESC_TAG);
      } else if (tagName.equals(RSSChannel.DATE_TAG)) {
        readTagContent(parser, RSSChannel.DATE_TAG);
      } else if (tagName.equals(RSSItem.CAT_TAG)) {
        category = readTagContent(parser, RSSItem.CAT_TAG);
      } else if (tagName.equals(RSSChannel.IMAGE_TAG)) {
        imageUrl = readImage(parser);
      } else {
        skip(parser);
      }
    }
    String imageType = "image/";
    if (!"".equals(imageUrl)){
      imageType += imageUrl.substring(imageUrl.lastIndexOf('.')+1);
    } else {
      imageType += "*";
    }
    Media image = new Media(title, "media-miniature", imageUrl, imageType);
    RSSChannel channel = new RSSChannel(rssUrl, title, link, description, category, image);
    return channel;

  }

  private static String readImage(XmlPullParser parser) throws XmlPullParserException, IOException {
    String imageUrl = "";
    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String tagName = parser.getName();
      if (tagName.equals(RSSChannel.URL_KEY)) {
        imageUrl = readTagContent(parser, RSSChannel.URL_KEY);
      } else {
        skip(parser);
      }
    }
    return imageUrl;
  }

  private RSSItem readEntry(XmlPullParser parser, String feedTitle) throws IOException,
      XmlPullParserException {
    String title = "";
    String link = "";
    String description = "";
    String author = "";
    String category = "";
    String comments = "";
    String mediaUrl = "";
    String mediaType = "";
    String guid = "";
    // Date pubDate = new Date();
    String pubDate = "";

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String tagName = parser.getName();
      if (tagName.equals(RSSItem.TITLE_TAG)) {
        title = readTagContent(parser, RSSItem.TITLE_TAG);
      } else if (tagName.equals(RSSItem.LINK_TAG)) {
        link = readTagContent(parser, RSSItem.LINK_TAG);
      } else if (tagName.equals(RSSItem.DESC_TAG)) {
        description = readTagContent(parser, RSSItem.DESC_TAG);
      } else if (tagName.equals(RSSItem.AUTHOR_TAG)) {
        author = readTagContent(parser, RSSItem.AUTHOR_TAG);
      } else if (tagName.equals(RSSItem.CAT_TAG)) {
        category = readTagContent(parser, RSSItem.CAT_TAG);
      } else if (tagName.equals(RSSItem.COMMENTS_TAG)) {
        comments = readTagContent(parser, RSSItem.COMMENTS_TAG);
      } else if (tagName.equals(RSSItem.ENC_TAG)) {
        Map<String, String> enclosureAtts = readTagAttribute(parser, RSSItem.ENC_TAG, "url", "type");
        mediaUrl = enclosureAtts.get("url");
        mediaType = enclosureAtts.get("type");
      } else if (tagName.equals(RSSItem.GUID_TAG)) {
        guid = readTagContent(parser, RSSItem.GUID_TAG);
      } else if (tagName.equals(RSSItem.DATE_TAG)) {
        // Wed, 29 Jan 2014 15:05:00 +0100
        // Wed, 29 Jan 2014 15:05:00 Z
        pubDate = readTagContent(parser, RSSItem.DATE_TAG);
        Log.d("date before", pubDate);
        Date date = DateUtils.parseDate(pubDate);
        pubDate = DateUtils.formatDBDate(date);
        Log.d("date  after", pubDate);
      } else {
        skip(parser);
      }
    }
    Media media = new Media(title, feedTitle, mediaUrl, mediaType);
    RSSItem item = new RSSItem(feedTitle, title, link, description, author, category, comments,
        media, guid, pubDate, false, -1, false);
    return item;
  }

  private static Map<String, String> readTagAttribute(XmlPullParser parser, String tagName, String... attNames)
      throws XmlPullParserException, IOException {
    parser.require(XmlPullParser.START_TAG, null, tagName);
    Map<String, String> attributes = new HashMap<String, String>();
    for (String attName : attNames) {
      String attValue = parser.getAttributeValue(null, attName);
      attributes.put(attName, attValue);
    }
    if (parser.next() == XmlPullParser.TEXT) {
      String content = parser.getText();
      parser.nextTag();
    }
    parser.require(XmlPullParser.END_TAG, null, tagName);
    return attributes;
  }

  private static String readTagContent(XmlPullParser parser, String tagName) throws IOException,
      XmlPullParserException {
    parser.require(XmlPullParser.START_TAG, null, tagName);
    String content = "";
    if (parser.next() == XmlPullParser.TEXT) {
      content = parser.getText();
      parser.nextTag();
    }
    parser.require(XmlPullParser.END_TAG, null, tagName);
    return content;
  }

  private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
    if (parser.getEventType() != XmlPullParser.START_TAG) {
      throw new IllegalStateException();
    }
    int depth = 1;
    while (depth != 0) {
      switch (parser.next()) {
        case XmlPullParser.END_TAG:
          depth--;
          break;
        case XmlPullParser.START_TAG:
          depth++;
          break;
      }
    }
  }
}
