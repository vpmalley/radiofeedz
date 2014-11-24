package fr.vpm.audiorss.db.filter;

import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 23/11/14.
 */
public class UnreadFilter implements QueryFilter.SelectionFilter {
  @Override
  public String getSelectionQuery() {
    return RSSItem.READ_KEY + "=0";
  }
}
