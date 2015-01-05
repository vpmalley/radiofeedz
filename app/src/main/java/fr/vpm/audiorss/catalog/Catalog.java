package fr.vpm.audiorss.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.vpm.audiorss.R;

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

  private List<List<Map<String, String>>> allGroups = new ArrayList<>();

  public void loadData() {

  }

  public List<? extends Map<String, ?>> getGroups() {
    Map<String, String> bbcGroup = new HashMap<>();
    bbcGroup.put(NAME_KEY, "BBC");
    List<Map<String, String>> allGroups = new ArrayList<>();
    allGroups.add(bbcGroup);
    return allGroups;
  }

  public List<? extends List<? extends Map<String, ?>>> getChildren() {
    Map<String, String> twtwItem = new HashMap<>();
    twtwItem.put(NAME_KEY, "BBC The world this week");
    twtwItem.put(URL_KEY, "http://downloads.bbc.co.uk/podcasts/worldservice/twtw/rss.xml");
    Map<String, String> foocItem = new HashMap<>();
    foocItem.put(NAME_KEY, "BBC From our own Correspondent");
    foocItem.put(URL_KEY, "http://downloads.bbc.co.uk/podcasts/radio4/fooc/rss.xml");
    List<Map<String, String>> bbcGroup = new ArrayList<>();
    bbcGroup.add(twtwItem);
    bbcGroup.add(foocItem);
    allGroups.add(bbcGroup);
    return allGroups;
  }

  public String getUrl(int groupPosition, int childPosition) {
    return allGroups.get(groupPosition).get(childPosition).get(URL_KEY);
  }
}
