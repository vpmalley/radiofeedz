package fr.vpm.audiorss.rss;

import java.util.Comparator;

/**
 * Created by vince on 01/05/15.
 */
public class RSSItemChronoComparator implements Comparator<RSSItem> {

  @Override
  public int compare(RSSItem rssItem, RSSItem otherRSSItem) {
    return rssItem.getDate().compareTo(otherRSSItem.getDate());
  }
}
