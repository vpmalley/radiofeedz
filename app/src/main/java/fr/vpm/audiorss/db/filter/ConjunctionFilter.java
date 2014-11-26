package fr.vpm.audiorss.db.filter;

import java.util.List;

/**
 * Created by vince on 23/11/14.
 */
public class ConjunctionFilter implements QueryFilter.SelectionFilter {

  private List<QueryFilter.SelectionFilter> filters;

  public ConjunctionFilter(List<QueryFilter.SelectionFilter> filters) {
    this.filters = filters;
  }

  @Override
  public int index() {
    return -1;
  }

  @Override
  public String getSelectionQuery() {
    StringBuilder queryBuilder = new StringBuilder();
    boolean first = true;
    for (QueryFilter.SelectionFilter filter : filters){
      String selectionQuery = filter.getSelectionQuery();
      if ((selectionQuery != null) && (!selectionQuery.isEmpty())) {
        if (!first){
          queryBuilder.append(" AND ");
        } else {
          first = false;
        }
        queryBuilder.append(selectionQuery);
      }
    }

    return queryBuilder.toString();
  }

  @Override
  public String[] getSelectionValues() {
    int size = 0;
    for (QueryFilter.SelectionFilter filter : filters){
      size += filter.getSelectionValues().length;
    }
    String[] selectionValues = new String[size];
    if (size > 0){
      int j = 0;
      for (QueryFilter.SelectionFilter filter : filters){
        for (String selectionValue : filter.getSelectionValues()){
          selectionValues[j++] = selectionValue;
        }
      }
    }
    return selectionValues;
  }
}
