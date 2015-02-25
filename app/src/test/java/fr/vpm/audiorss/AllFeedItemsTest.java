package fr.vpm.audiorss;

import android.widget.AbsListView;
import android.app.Activity;

import junit.framework.Assert;


import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.util.ActivityController;

import fr.vpm.audiorss.AllFeedItems;
import fr.vpm.audiorss.rss.RSSChannel;

@Config(manifest = "./src/main/AndroidManifest.xml")
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class AllFeedItemsTest {

  @Test
  public void testOpeningCatalog() throws Exception {

    ActivityController<AllFeedItems> activityController = Robolectric.buildActivity(AllFeedItems.class);
    activityController.create();
    AllFeedItems activity = activityController.get();

    AbsListView feedItems = (AbsListView) activity.findViewById(R.id.allitems);
    Assert.assertTrue(feedItems.getAdapter().isEmpty());
  }
}