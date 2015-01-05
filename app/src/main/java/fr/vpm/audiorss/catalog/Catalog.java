package fr.vpm.audiorss.catalog;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

  public static final int GROUP_LAYOUT = R.layout.drawer_item;
  public static final int[] GROUP_VIEWS = new int[]{R.id.feed_item};
  public static final int CHILD_LAYOUT = R.layout.drawer_item;
  public static final int[] CHILD_VIEWS = new int[]{R.id.feed_item};

  public static final String NAME_KEY = "name";
  public static final String[] GROUP_KEYS = new String[]{NAME_KEY};
  public static final String[] CHILD_KEYS = new String[]{NAME_KEY};
  public static final String URL_KEY = "url";
  public static final String ICON_URL_KEY = "iconUrl";

  private List<FeedGroup> feeds = new ArrayList<>();

  public void loadData() {
    // use R.raw.catalog;
  }

  public List<? extends Map<String, ?>> getGroups() {
    List<Map<String, String>> allGroups = new ArrayList<>();
    for (FeedGroup feedGroup : feeds) {
      allGroups.add(feedGroup.getAsMap());
    }
    return allGroups;
  }

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

  public String getUrl(int groupPosition, int childPosition) {
    return feeds.get(groupPosition).getFeeds().get(childPosition).getUrl();
  }


  public List<FeedGroup> retrieve(File catalogFile) throws FileNotFoundException {
    if (!catalogFile.exists()){
      return null;
    }
    FileInputStream pictureStream = null;
    try {
      pictureStream = new FileInputStream(catalogFile);
      // read to a string?
      // use Gson to parse the json catalog

    } finally {
      if (pictureStream != null){
        try {
          pictureStream.close();
        } catch (IOException e) {
          Log.e("file", e.getMessage());
        }
      }
    }
    return feeds;
  }

}
