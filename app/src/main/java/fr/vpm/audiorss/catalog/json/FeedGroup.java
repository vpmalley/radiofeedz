package fr.vpm.audiorss.catalog.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.vpm.audiorss.catalog.Catalog;

/**
 * Created by vince on 05/01/15.
 */
public class FeedGroup {

  String name;

  List<Feed> feeds;

  public String getName() {
    return name;
  }

  public List<Feed> getFeeds() {
    return feeds;
  }

  public Map<String, String> getAsMap(){
    Map<String, String> group = new HashMap<>();
    group.put(Catalog.NAME_KEY, name);
    return group;
  }
}
