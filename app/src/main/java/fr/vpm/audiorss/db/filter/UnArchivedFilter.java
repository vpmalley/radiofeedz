package fr.vpm.audiorss.db.filter;

import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 23/11/14.
 */
public class UnArchivedFilter implements QueryFilter.SelectionFilter {

  @Override
  public int index() {
    return -1;
  }

  @Override
  public String getSelectionQuery() {
    return RSSItem.ARCHIVED_KEY + "=0";
  }

  @Override
  public String[] getSelectionValues() {
    return new String[0];
  }
}
