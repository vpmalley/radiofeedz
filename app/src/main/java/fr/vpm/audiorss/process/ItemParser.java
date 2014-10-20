package fr.vpm.audiorss.process;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class ItemParser {

  private static final String CHANNEL_TAG = "channel";
  private static final String RSS_TAG = "rss";
  private static final String ITEM_TAG = "item";

  public RSSChannel parseChannel(HttpEntity entity, String rssUrl) throws XmlPullParserException,
      IOException, ParseException {
    InputStream in = null;
    try {
      Log.d("ItemParser", "started reading channel");
      in = entity.getContent();
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
    RSSChannel channel = new RSSChannel(rssUrl, title, link, description, category, imageUrl);
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
        mediaUrl = readTagAttribute(parser, RSSItem.ENC_TAG, "url");
      } else if (tagName.equals(RSSItem.GUID_TAG)) {
        guid = readTagContent(parser, RSSItem.GUID_TAG);
      } else if (tagName.equals(RSSItem.DATE_TAG)) {
        /*
         * try { // pubDate = new
         * SimpleDateFormat(RSSChannel.DATE_PATTERN).parse( } catch
         * (ParseException e) { Log.e("Exception", e.toString()); }
         */
        // Wed, 29 Jan 2014 15:05:00 +0100
        pubDate = readTagContent(parser, RSSItem.DATE_TAG);
      } else {
        skip(parser);
      }
    }
    RSSItem item = new RSSItem(feedTitle, title, link, description, author, category, comments,
        mediaUrl, guid, pubDate);
    return item;
  }

  private String readTagAttribute(XmlPullParser parser, String tagName, String attName)
      throws XmlPullParserException, IOException {
    parser.require(XmlPullParser.START_TAG, null, tagName);
    String content = null;
    String att = parser.getAttributeValue(null, attName);
    if (parser.next() == XmlPullParser.TEXT) {
      content = parser.getText();
      parser.nextTag();
    }
    // Log.d(attName, att);
    parser.require(XmlPullParser.END_TAG, null, tagName);
    return att;
  }

  private String readTagContent(XmlPullParser parser, String tagName) throws IOException,
      XmlPullParserException {
    parser.require(XmlPullParser.START_TAG, null, tagName);
    String content = null;
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

  /**
   * Returns the Uri of a file if it is downloaded. Returns null if not
   * downloaded yet.
   * 
   * @param fileId
   * @param context
   * @return
   */
  /*
   * public static Uri retrieveUri(long fileId, Context context){
   * DownloadManager dm = (DownloadManager)
   * context.getSystemService(Context.DOWNLOAD_SERVICE); return
   * dm.getUriForDownloadedFile(fileId); }
   */
}