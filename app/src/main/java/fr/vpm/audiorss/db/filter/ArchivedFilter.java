package fr.vpm.audiorss.db.filter;

import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 23/11/14.
 */
public class ArchivedFilter implements QueryFilter.SelectionFilter {

  @Override
  public int index() {
    return QueryFilter.ARCHIVED.index();
  }

  @Override
  public String getSelectionQuery() {
    return RSSItem.ARCHIVED_KEY + "!=0";
  }

  @Override
  public String[] getSelectionValues() {
    return new String[0];
  }
}
