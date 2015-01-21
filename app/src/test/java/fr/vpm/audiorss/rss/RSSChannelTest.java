package fr.vpm.audiorss.rss;

import junit.framework.Assert;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;

import org.junit.Test;

import fr.vpm.audiorss.db.DbRSSChannel;
import fr.vpm.audiorss.rss.RSSChannel;

import android.content.ContentValues;
import android.test.InstrumentationTestCase;


@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class RSSChannelTest {

	private static final String SAMPLE_URL = "someUrl";
	private static final String SAMPLE_TAG = "someTag";
	private static final String SAMPLE_DESCRIPTION = "someDescription";
	private static final String SAMPLE_CATEGORY = "someCategory";
	private static final String SAMPLE_LINK = "someLink";
	private static final String SAMPLE_TITLE = "someTitle";

	@Test
	public void testBuildChannel() {
		RSSChannel channel = new RSSChannel(SAMPLE_URL, SAMPLE_TITLE,
				SAMPLE_LINK, SAMPLE_DESCRIPTION, SAMPLE_CATEGORY, null);
		channel.addTag(SAMPLE_TAG);
		Assert.assertEquals(SAMPLE_TITLE, channel.getTitle());
		Assert.assertEquals(SAMPLE_LINK, channel.getLink());
		Assert.assertEquals(SAMPLE_DESCRIPTION, channel.getDescription());
		Assert.assertEquals(SAMPLE_CATEGORY, channel.getCategory());
		Assert.assertEquals(SAMPLE_TAG, channel.getTags().get(0));
	}

	@Test
	public void testSaveChannel() {
		RSSChannel channel = new RSSChannel(SAMPLE_URL, SAMPLE_TITLE,
				SAMPLE_LINK, SAMPLE_DESCRIPTION, SAMPLE_CATEGORY, null);
		channel.addTag(SAMPLE_TAG);
    RSSChannel.allChannels.add(channel);

		RSSChannel retrievedChannel = RSSChannel.allChannels.get(0);
		Assert.assertEquals(SAMPLE_TITLE, retrievedChannel.getTitle());
		Assert.assertEquals(SAMPLE_LINK, retrievedChannel.getLink());
		Assert.assertEquals(SAMPLE_DESCRIPTION, retrievedChannel.getDescription());
		Assert.assertEquals(SAMPLE_CATEGORY, retrievedChannel.getCategory());
		Assert.assertEquals(SAMPLE_TAG, retrievedChannel.getTags().get(0));
	}
}
