package fr.vpm.audiorss.process;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 27/10/14.
 *
 * Compares multiple RSSItem instances based on the ordering set in the preferences.
 */
public class ItemComparator implements Comparator<RSSItem> {

  public enum ItemComparison {
    TIME("pubDate ASC"),
    REVERSE_TIME("pubDate DESC"),
    ALPHA("title ASC"),
    REVERSE_ALPHA("title DESC");

    private String key;
    private ItemComparison(String key){
      this.key = key;
    }

    public static ItemComparison fromKey(String key) {
      ItemComparison comparison = REVERSE_TIME;
      if (TIME.key.equals(key)){
        comparison = TIME;
      } else if (REVERSE_TIME.key.equals(key)){
        comparison = REVERSE_TIME;
      } else if (ALPHA.key.equals(key)){
        comparison = ALPHA;
      } else if (REVERSE_ALPHA.key.equals(key)){
        comparison = REVERSE_ALPHA;
      }
      return comparison;
    }
  }

  private final ItemComparison itemComparison;

  public ItemComparator(String itemComparison) {
    this.itemComparison = ItemComparison.fromKey(itemComparison);
  }

  @Override
  public int compare(RSSItem rssItem, RSSItem otherRssItem) {
    int comparison = 0;
    
    int comparisonByDate = rssItem.getDate().compareTo(otherRssItem.getDate());

    int comparisonByName = rssItem.getTitle().compareTo(otherRssItem.getTitle());

    if (ItemComparison.ALPHA.equals(itemComparison) || ItemComparison.REVERSE_ALPHA.equals(itemComparison)) {
      comparison = comparisonByName;
    } else {
      comparison = comparisonByDate;
    }

    int factor = 1;
    if (ItemComparison.REVERSE_TIME.equals(itemComparison) || ItemComparison.REVERSE_ALPHA.equals(itemComparison)) {
      factor = -1;
    }

    if (comparison == 0) {
      comparison = comparisonByName + comparisonByDate;
    }
    return factor * comparison;
  }

}
