package fr.vpm.audiorss.db.filter;

/**
 * Created by vince on 23/11/14.
 */
public class EmptyFilter implements SelectionFilter {

  @Override
  public String getSelectionQuery() {
    return "";
  }

  @Override
  public String[] getSelectionValues() {
    return new String[0];
  }
}
