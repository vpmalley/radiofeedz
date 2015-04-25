package fr.vpm.audiorss.process;

import junit.framework.TestCase;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class ItemParserTest extends TestCase {

  public void testParseItem() {

    String itemContent = "<item>\n" +
        "<title>Inter soir 19h</title>\n" +
        "<link>http://www.franceinter.fr/emission-le-journal-de-19h-inter-soir-19h-506</link>\n" +
        "<description>durée : 00:17:59 - Journal de 19h - </description>\n" +
        "<author>podcast@radiofrance.com</author>\n" +
        "<category >News &amp; Politics </category>\n" +
        "<enclosure url=\"http://rf.proxycast.org/m/media/273073201426.mp3?c=information&amp;p=Journal+de+19h_11736&amp;l3=20140808&amp;l4=&amp;media_url=http%3A%2F%2Fmedia.radiofrance-podcast.net%2Fpodcast09%2F11736-08.08.2014-ITEMA_20658050-0.mp3\" length=\"17978336\" type=\"audio/mpeg\"  />\n" +
        "<guid >http://media.radiofrance-podcast.net/podcast09/11736-08.08.2014-ITEMA_20658050-0.mp3</guid>\n" +
        "<pubDate>Fri, 08 Aug 2014 19:00:00 +0200</pubDate>\n" +
        "<podcastRF:businessReference>2958</podcastRF:businessReference>\n" +
        "<podcastRF:magnetothequeID>2014F2958S0220</podcastRF:magnetothequeID>\n" +
        "<podcastRF:stepID>10159448</podcastRF:stepID>\n" +
        "<itunes:author>Radio France</itunes:author>\n" +
        "<itunes:explicit>no</itunes:explicit>\n" +
        "<itunes:keywords>Inter,soir,19h</itunes:keywords>\n" +
        "<itunes:subtitle>Émission du 08.08.2014</itunes:subtitle>\n" +
        "<itunes:summary>durée : 00:17:59 - </itunes:summary>\n" +
        "<itunes:duration>00:17:59</itunes:duration>\n" +
        "</item>\n";

    List<String> items = new ArrayList<>();
    items.add(itemContent);
    RSSChannel channel = new RSSChannel("", "Hello", "", "World", "", null);
    assertEquals(0, channel.getItems().size());

    ItemParser parser = new ItemParser(channel, items);
    try {
      parser.extractRSSItems("2000-01-01-01:00:00--05:00");
    } catch (XmlPullParserException e) {
      fail(e.toString());
    } catch (IOException e) {
      fail(e.toString());
    }
    assertEquals(1, channel.getItems().size());
    RSSItem rssItem = channel.getItems().iterator().next();
    assertEquals("http://media.radiofrance-podcast.net/podcast09/11736-08.08.2014-ITEMA_20658050-0.mp3", rssItem.getGuid());
    assertEquals("Inter soir 19h", rssItem.getTitle());
  }

  public void testParseChannel() {

    String channelContent = "<channel>\n" +
        "<title>Journal de 19h</title>\n" +
        "<link>http://www.radiofrance.fr/chaines/france-inter01/information/journaux/</link>\n" +
        "<description>Journal de 19h</description>\n" +
        "<language>fr</language>\n" +
        "<copyright>Radio France</copyright>\n" +
        "<lastBuildDate>Wed, 05 Nov 2014 19:34:22 +0100</lastBuildDate>\n" +
        "<generator>Radio France</generator>\n" +
        "<image>\n" +
        "<url>http://media.radiofrance-podcast.net/podcast09/RF_OMM_0000006174_ITE.jpg</url>\n" +
        "<title>Journal de 19h</title>\n" +
        "<link>http://www.radiofrance.fr/chaines/france-inter01/information/journaux/</link>\n" +
        "</image>\n" +
        "<itunes:author>Radio France</itunes:author>\n" +
        "<itunes:category text=\"News &amp; Politics\"></itunes:category>\n" +
        "<itunes:explicit>no</itunes:explicit>\n" +
        "<itunes:image href=\"http://media.radiofrance-podcast.net/podcast09/RF_OMM_0000006174_ITE.jpg\" />\n" +
        "<itunes:owner>\n" +
        "<itunes:email>podcast@radiofrance.com</itunes:email>\n" +
        "<itunes:name>Radio France</itunes:name>\n" +
        "</itunes:owner>\n" +
        "<itunes:subtitle>Journal de 19h</itunes:subtitle>\n" +
        "<itunes:summary>Journal de 19h</itunes:summary>\n" +
        "<podcastRF:originStation>1000</podcastRF:originStation>\n" +
        "</channel>";

    RSSChannel channel = null;
    try {
      channel = ItemParser.extractRSSChannel("http://myrssfeed", channelContent);
    } catch (XmlPullParserException e) {
      fail(e.toString());
    } catch (IOException e) {
      fail(e.toString());
    } catch (ParseException e) {
      fail(e.toString());
    }
    assertNotNull(channel);
    assertEquals(0, channel.getItems().size());
    assertEquals("http://myrssfeed", channel.getUrl());
    assertEquals("Journal de 19h", channel.getTitle());
    assertEquals("Journal de 19h", channel.getDescription());
  }
}
