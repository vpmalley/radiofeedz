package fr.vpm.audiorss.db.filter;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vince on 23/11/14.
 */
public class ConjunctionFilter implements SelectionFilter {

  private static final String FILTERS_KEY = "filters";

  private ArrayList<SelectionFilter> filters;

  public ConjunctionFilter(List<SelectionFilter> filters) {
    this.filters = new ArrayList<SelectionFilter>(filters);
  }

  @Override
  public String getSelectionQuery() {
    StringBuilder queryBuilder = new StringBuilder();
    boolean first = true;
    for (SelectionFilter filter : filters){
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
    for (SelectionFilter filter : filters){
      size += filter.getSelectionValues().length;
    }
    String[] selectionValues = new String[size];
    if (size > 0){
      int j = 0;
      for (SelectionFilter filter : filters){
        for (String selectionValue : filter.getSelectionValues()){
          selectionValues[j++] = selectionValue;
        }
      }
    }
    return selectionValues;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    Bundle b = new Bundle();
    b.putParcelableArrayList(FILTERS_KEY, filters);
    dest.writeBundle(b);
  }

  public static final Parcelable.Creator<ConjunctionFilter> CREATOR
          = new Parcelable.Creator<ConjunctionFilter>() {
    public ConjunctionFilter createFromParcel(Parcel in) {
      List<Parcelable> parcelledFilters = in.readBundle().getParcelableArrayList(FILTERS_KEY);
      List<SelectionFilter> selectionFilters = new ArrayList<SelectionFilter>();
      for (Parcelable filter : parcelledFilters) {
        selectionFilters.add((SelectionFilter) filter);
      }
      return new ConjunctionFilter(selectionFilters);
    }

    public ConjunctionFilter[] newArray(int size) {
      return new ConjunctionFilter[size];
    }
  };
}
