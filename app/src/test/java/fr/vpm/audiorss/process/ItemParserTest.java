package fr.vpm.audiorss.process;

import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;

import fr.vpm.audiorss.rss.RSSChannel;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ItemParserTest {

  @Test
	public void testParseFull4DDate() throws XmlPullParserException, IOException, ParseException {
		ItemParser parser = new ItemParser();

    String rawDate = "Wed, 29 Jan 2014 15:05:00 +0100";
    String formattedDate = parser.parseDate(rawDate);
    // we expect pattern "yyyy-MM-dd-HH:mm:ss-ZZZZZ"
    // the result we expect has Locale US
    Assert.assertEquals("2014-01-29-09:05:00--0500", formattedDate);
	}

  @Test
  public void testParseMinimalDate() throws XmlPullParserException, IOException, ParseException {
    ItemParser parser = new ItemParser();

    String rawDate = "29 Jan 2014 15:05";
    String formattedDate = parser.parseDate(rawDate);
    // we expect pattern "yyyy-MM-dd-HH:mm:ss-ZZZZZ"
    // the result we expect has Locale US
    Assert.assertEquals("2014-01-29-15:05:00--0500", formattedDate);
  }

  @Test
  public void testParseWeirdDate() throws XmlPullParserException, IOException, ParseException {
    ItemParser parser = new ItemParser();

    String rawDate = "29 Jan 2014 15:05:00 Z";
    String formattedDate = parser.parseDate(rawDate);
    // we expect pattern "yyyy-MM-dd-HH:mm:ss-ZZZZZ"
    // the result we expect has Locale US
    Assert.assertEquals("2014-01-29-15:05:00--0500", formattedDate);
  }

  @Test
  public void testParseGMTDate() throws XmlPullParserException, IOException, ParseException {
    ItemParser parser = new ItemParser();

    String rawDate = "29 Jan 2014 15:05:00 GMT";
    String formattedDate = parser.parseDate(rawDate);
    // we expect pattern "yyyy-MM-dd-HH:mm:ss-ZZZZZ"
    // the result we expect has Locale US
    Assert.assertEquals("2014-01-29-10:05:00--0500", formattedDate);
  }

  @Test
  public void testParse2D2DDate() throws XmlPullParserException, IOException, ParseException {
    ItemParser parser = new ItemParser();

    String rawDate = "29 Jan 2014 15:05:00 +01:00";
    String formattedDate = parser.parseDate(rawDate);
    // we expect pattern "yyyy-MM-dd-HH:mm:ss-ZZZZZ"
    // the result we expect has Locale US
    Assert.assertEquals("2014-01-29-10:05:00--0500", formattedDate);
  }

  @Test
  public void testParseGMT2D2DDate() throws XmlPullParserException, IOException, ParseException {
    ItemParser parser = new ItemParser();

    String rawDate = "29 Jan 2014 15:05:00 GMT+01:00";
    String formattedDate = parser.parseDate(rawDate);
    // we expect pattern "yyyy-MM-dd-HH:mm:ss-ZZZZZ"
    // the result we expect has Locale US
    Assert.assertEquals("2014-01-29-10:05:00--0500", formattedDate);
  }
}
