package fr.vpm.audiorss.process;

import java.io.IOException;
import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import fr.vpm.audiorss.rss.RSSChannel;

public class ItemParserTest {

	@Test
	public void testParse() throws XmlPullParserException, IOException, ParseException {
		
		ItemParser parser = new ItemParser();
		
		RSSChannel channel = parser.parseChannel(null, "someUrl");
		/*
		Assert.assertEquals(SAMPLE_TITLE, channel.title);
		Assert.assertEquals(SAMPLE_LINK, channel.link);
		Assert.assertEquals(SAMPLE_DESCRIPTION, channel.description);
		Assert.assertEquals(SAMPLE_CATEGORY, channel.category);
		Assert.assertEquals(SAMPLE_TAG, channel.tags.get(0));
		*/
	}
}
