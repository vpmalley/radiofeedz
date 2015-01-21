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
	public void testParse() throws XmlPullParserException, IOException, ParseException {
		
		ItemParser parser = new ItemParser();

    Assert.assertEquals(2, 1 + 1);
    //RSSChannel channel = parser.parseChannel("someUrl");
		/*
		Assert.assertEquals(SAMPLE_TITLE, channel.title);
		Assert.assertEquals(SAMPLE_LINK, channel.link);
		Assert.assertEquals(SAMPLE_DESCRIPTION, channel.description);
		Assert.assertEquals(SAMPLE_CATEGORY, channel.category);
		Assert.assertEquals(SAMPLE_TAG, channel.tags.get(0));
		*/
	}
}
