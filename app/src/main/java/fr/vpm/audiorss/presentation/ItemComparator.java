package fr.vpm.audiorss.presentation;

import java.util.Comparator;

/**
 * Created by vince on 27/10/14.
 *
 * Compares multiple RSSItem instances based on the ordering set in the preferences.
 */
public class ItemComparator implements Comparator<DisplayedRSSItem> {

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
  public int compare(DisplayedRSSItem rssItem, DisplayedRSSItem otherRssItem) {
    int comparison = 0;

    if (ItemComparison.ALPHA.equals(itemComparison) || ItemComparison.REVERSE_ALPHA.equals(itemComparison)) {
      comparison = rssItem.getRssItem().getTitle().compareTo(otherRssItem.getRssItem().getTitle());
    } else {
      comparison = rssItem.getRssItem().getDate().compareTo(otherRssItem.getRssItem().getDate());
    }

    int factor = 1;
    if (ItemComparison.REVERSE_TIME.equals(itemComparison) || ItemComparison.REVERSE_ALPHA.equals(itemComparison)) {
      factor = -1;
    }

    return factor * comparison;
  }

}
