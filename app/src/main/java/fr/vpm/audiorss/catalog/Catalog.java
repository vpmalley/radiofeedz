package fr.vpm.audiorss.catalog;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.catalog.json.Feed;
import fr.vpm.audiorss.catalog.json.FeedGroup;

/**
 * Created by vince on 04/01/15.
 */
public class Catalog {

  public static final int GROUP_LAYOUT = R.layout.list_item_indented;
  public static final int[] GROUP_VIEWS = new int[]{R.id.feed_item};
  public static final int CHILD_LAYOUT = R.layout.list_item;
  public static final int[] CHILD_VIEWS = new int[]{R.id.feed_item};

  public static final String NAME_KEY = "name";
  public static final String[] GROUP_KEYS = new String[]{NAME_KEY};
  public static final String[] CHILD_KEYS = new String[]{NAME_KEY};
  public static final String URL_KEY = "url";
  public static final String ICON_URL_KEY = "iconUrl";

  private final List<FeedGroup> feeds;

  public Catalog(List<FeedGroup> feeds) {
    this.feeds = feeds;
  }

  /**
   * Extracts from the catalog data the feed groups information, to pass to the expandable list adapter.
   * @return the data for the feed groups of the catalog
   */
  public List<? extends Map<String, ?>> getGroups() {
    List<Map<String, String>> allGroups = new ArrayList<>();
    for (FeedGroup feedGroup : feeds) {
      allGroups.add(feedGroup.getAsMap());
    }
    return allGroups;
  }

  /**
   * Extracts from the catalog data the feeds information, to pass to the expandable list adapter.
   * @return the data for the feeds of the catalog
   */
  public List<? extends List<? extends Map<String, ?>>> getChildren() {
    List<List<Map<String, String>>> allGroups = new ArrayList<>();
    for (FeedGroup feedGroup : feeds) {
      List<Map<String, String>> feedGroupAsList = new ArrayList<>();
      for (Feed feed : feedGroup.getFeeds()){
        feedGroupAsList.add(feed.getAsMap());
      }
      allGroups.add(feedGroupAsList);
    }
    return allGroups;
  }

  /**
   * Retrieves the feed url based on the picked item, identified by a group and child index
   * @param groupPosition the group index for the picked item
   * @param childPosition the child index for the picked item
   * @return the feed url for the picked item
   */
  public String getUrl(int groupPosition, int childPosition) {
    String url = null;
    if (feeds.isEmpty()){
      Log.w("catalog", "The catalog is empty. Did you load it with Catalog.loadData(Context) ?");
    } else {
      url = feeds.get(groupPosition).getFeeds().get(childPosition).getUrl();
    }
    return url;
  }
}
