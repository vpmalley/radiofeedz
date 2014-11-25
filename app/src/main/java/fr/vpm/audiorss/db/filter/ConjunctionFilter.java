package fr.vpm.audiorss.db.filter;

/**
 * Created by vince on 23/11/14.
 */
public class ConjunctionFilter implements QueryFilter.SelectionFilter {

  QueryFilter[] filters;

  @Override
  public String getSelectionQuery() {
    StringBuilder queryBuilder = new StringBuilder();
    boolean first = true;
    for (QueryFilter filter : filters){
      String selectionQuery = filter.getFilter().getSelectionQuery();
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
    for (QueryFilter filter : filters){
      size += filter.getFilter().getSelectionValues().length;
    }
    String[] selectionValues = new String[size];
    if (size > 0){
      int j = 0;
      for (QueryFilter filter : filters){
        for (String selectionValue : filter.getFilter().getSelectionValues()){
          selectionValues[j++] = selectionValue;
        }
      }
    }
    return selectionValues;
  }
}
