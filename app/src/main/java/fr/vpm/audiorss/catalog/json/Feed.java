package fr.vpm.audiorss.catalog.json;

import java.util.HashMap;
import java.util.Map;

import fr.vpm.audiorss.catalog.Catalog;

/**
 * Created by vince on 05/01/15.
 */
public class Feed {

  String name;

  String url;

  String iconUrl;

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public Map<String, String> getAsMap(){
    Map<String, String> feed = new HashMap<>();
    feed.put(Catalog.NAME_KEY, name);
    feed.put(Catalog.URL_KEY, url);
    feed.put(Catalog.ICON_URL_KEY, iconUrl);
    return feed;
  }
}
