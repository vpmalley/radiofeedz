package fr.vpm.audiorss.db.filter;

/**
 * Filters on SQL queries
 *
 * Created by vince on 23/11/14.
 */
public enum QueryFilter {
  LATEST(new LatestFilter()),
  TODAY(new TodayFilter()),
  UNREAD(new UnreadFilter()),
  DOWNLOADED(new LatestFilter()),
  ARCHIVED(new LatestFilter());

  private SelectionFilter selectionFilter;

  QueryFilter(SelectionFilter filter){
    this.selectionFilter = filter;
  }

  /**
   * Determines which filter to use based on the position in the drawer list.
   * /!\ The order must fit the order in the array R.array.item_filters
   *
   * @param position the position in the drawer list
   * @return the picked QueryFilter
   */
  public static QueryFilter fromPosition(int position){
    switch (position){
      case 0: return LATEST;
      case 1: return TODAY;
      case 2: return UNREAD;
      case 3: return DOWNLOADED;
      case 4: return ARCHIVED;
      default: return LATEST;
    }
  }

  /**
   * Retrieves the filter on the selection
   * @return a filter for the selection in DB
   */
  public SelectionFilter getFilter(){
    return selectionFilter;
  }

  public interface SelectionFilter {
    String getSelectionQuery();
  }
}
