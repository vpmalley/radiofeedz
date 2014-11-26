package fr.vpm.audiorss.db.filter;

/**
 * Created by vince on 23/11/14.
 */
public class EmptyFilter implements QueryFilter.SelectionFilter {

  @Override
  public int index() {
    return -1;
  }

  @Override
  public String getSelectionQuery() {
    return "";
  }

  @Override
  public String[] getSelectionValues() {
    return new String[0];
  }
}
