package fr.vpm.audiorss.rss;

import junit.framework.Assert;

import org.junit.Test;

import fr.vpm.audiorss.db.DbRSSChannel;
import android.content.ContentValues;
import android.test.InstrumentationTestCase;

public class RSSChannelTest extends InstrumentationTestCase {

	private static final String SAMPLE_URL = "someUrl";
	private static final String SAMPLE_TAG = "someTag";
	private static final String SAMPLE_DESCRIPTION = "someDescription";
	private static final String SAMPLE_CATEGORY = "someCategory";
	private static final String SAMPLE_LINK = "someLink";
	private static final String SAMPLE_TITLE = "someTitle";

	@Test
	public void testBuildChannel() {
		RSSChannel channel = new RSSChannel(SAMPLE_URL, SAMPLE_TITLE,
				SAMPLE_LINK, SAMPLE_DESCRIPTION, SAMPLE_CATEGORY, "");
		channel.addTag(SAMPLE_TAG);
		Assert.assertEquals(SAMPLE_TITLE, channel.title);
		Assert.assertEquals(SAMPLE_LINK, channel.link);
		Assert.assertEquals(SAMPLE_DESCRIPTION, channel.description);
		Assert.assertEquals(SAMPLE_CATEGORY, channel.category);
		Assert.assertEquals(SAMPLE_TAG, channel.tags.get(0));
	}

	@Test
	public void testSaveChannel() {
		RSSChannel channel = new RSSChannel(SAMPLE_URL, SAMPLE_TITLE,
				SAMPLE_LINK, SAMPLE_DESCRIPTION, SAMPLE_CATEGORY, "");
		channel.addTag(SAMPLE_TAG);

		RSSChannel retrievedChannel = RSSChannel.allChannels.get(0);
		Assert.assertEquals(SAMPLE_TITLE, retrievedChannel.title);
		Assert.assertEquals(SAMPLE_LINK, retrievedChannel.link);
		Assert.assertEquals(SAMPLE_DESCRIPTION, retrievedChannel.description);
		Assert.assertEquals(SAMPLE_CATEGORY, retrievedChannel.category);
		Assert.assertEquals(SAMPLE_TAG, retrievedChannel.tags.get(0));
	}

	@Test
	public void testBuildContentValuesFromChannel() {

		RSSChannel channel = new RSSChannel(SAMPLE_URL, SAMPLE_TITLE,
				SAMPLE_LINK, SAMPLE_DESCRIPTION, SAMPLE_CATEGORY, "");
		channel.addTag(SAMPLE_TAG);

		ContentValues channelValues = new DbRSSChannel(null).createContentValues(channel);
		Assert.assertEquals(SAMPLE_URL, channelValues.get(RSSChannel.URL_KEY));
		
	}
}
