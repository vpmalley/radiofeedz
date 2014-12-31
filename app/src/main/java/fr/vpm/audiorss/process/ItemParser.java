package fr.vpm.audiorss.process;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class ItemParser {

  private static final String CHANNEL_TAG = "channel";
  private static final String RSS_TAG = "rss";
  private static final String ITEM_TAG = "item";

  public RSSChannel parseChannel(String rssUrl) throws XmlPullParserException,
      IOException, ParseException {
    InputStream in = null;
    HttpURLConnection urlConnection = null;
    try {
      Log.d("ItemParser", "started reading channel");
      URL formattedUrl = new URL(rssUrl);
      urlConnection = (HttpURLConnection) formattedUrl.openConnection();
      in = new BufferedInputStream(urlConnection.getInputStream());

      XmlPullParser parser = Xml.newPullParser();
      parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      parser.setInput(in, null);
      parser.nextTag();
      return readFeed(parser, rssUrl);
    } finally {
      in.close();
    }
  }

  private RSSChannel readFeed(XmlPullParser parser, String rssUrl) throws XmlPullParserException,
      IOException, ParseException {
    Map<String, RSSItem> items = new HashMap<String, RSSItem>();
    String title = "";
    String link = "";
    String description = "";
    // Date lastBuildDate = new Date();
    String lastBuildDate = "";
    String category = "";
    String imageUrl = "";

    parser.require(XmlPullParser.START_TAG, null, RSS_TAG);
    parser.nextTag();
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
        lastBuildDate = readTagContent(parser, RSSChannel.DATE_TAG);
      } else if (tagName.equals(RSSItem.CAT_TAG)) {
        category = readTagContent(parser, RSSItem.CAT_TAG);
      } else if (tagName.equals(RSSChannel.IMAGE_TAG)) {
        imageUrl = readImage(parser);
      } else if (tagName.equals(ITEM_TAG)) {
        RSSItem item = readEntry(parser, title);
        if (item.getId() != null) {
          Log.d("adding item with id", item.getId());
          items.put(item.getId(), item);
        } else {
          Log.d("adding item with link", item.getLink());
          items.put(item.getLink(), item);
        }
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
    channel.update(lastBuildDate, items);

    return channel;

  }

  private String readImage(XmlPullParser parser) throws XmlPullParserException, IOException {
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
        try {

          // time zone : Z/ZZ/ZZZ
          Pattern expectedNumPattern = Pattern.compile("[0-9]{2}\\s[A-Z][a-z]{2}\\s[0-9]{4}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}\\s[\\+\\-][0-9]{4}");
          Matcher numM = expectedNumPattern.matcher(pubDate);

          // time zone : ZZZZZ
          Pattern expectedTimePattern = Pattern.compile("[0-9]{2}\\s[A-Z][a-z]{2}\\s[0-9]{4}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}\\s[\\+\\-][0-9]{2}:[0-9]{2}");
          Matcher timeM = expectedTimePattern.matcher(pubDate);

          Pattern expectedMinimumPattern = Pattern.compile("[0-9]{2}\\s[A-Z][a-z]{2}\\s[0-9]{4}\\s[0-9]{2}:[0-9]{2}");
          Matcher m = expectedMinimumPattern.matcher(pubDate);

          Date date;
          if (numM.find()){
            Log.d("datePattern", "4D");
            date = new SimpleDateFormat(RSSChannel.RSS_DATE_PATTERN_TZ_4D, Locale.US).parse(numM.group());
          } else if (timeM.find()){
            Log.d("datePattern", "2D:2D");
            date = new SimpleDateFormat(RSSChannel.RSS_DATE_PATTERN_TZ_2D_2D, Locale.US).parse(timeM.group());
          } else if (m.find()){
            Log.d("datePattern", "default");
            date = new SimpleDateFormat(RSSChannel.RSS_DATE_MIN_PATTERN, Locale.US).parse(m.group());
          } else {
            Log.w("datePattern", "Could not parse the right date, defaulting to current date");
            date = Calendar.getInstance().getTime();
          }
          pubDate = new SimpleDateFormat(RSSChannel.DB_DATE_PATTERN, Locale.US).format(date);
        } catch (ParseException e) {
          Log.w("datePattern", "tried parsing date but failed: " + pubDate + ". " + e.getMessage());
          pubDate = new SimpleDateFormat(RSSChannel.DB_DATE_PATTERN, Locale.US).format(Calendar.getInstance().getTime());
        }
      } else {
        skip(parser);
      }
    }
    Media media = new Media(title, feedTitle, mediaUrl, mediaType);
    RSSItem item = new RSSItem(feedTitle, title, link, description, author, category, comments,
        media, guid, pubDate, false, -1, false);
    return item;
  }

  private Map<String, String> readTagAttribute(XmlPullParser parser, String tagName, String... attNames)
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

  private String readTagContent(XmlPullParser parser, String tagName) throws IOException,
      XmlPullParserException {
    parser.require(XmlPullParser.START_TAG, null, tagName);
    String content = "";
    if (parser.next() == XmlPullParser.TEXT) {
      content = parser.getText();
      parser.nextTag();
    }
    // Log.d(tagName, content);
    parser.require(XmlPullParser.END_TAG, null, tagName);
    return content;
  }

  private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
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
