package fr.vpm.audiorss.db.filter;

/**
 * Filters on SQL queries
 *
 * Created by vince on 23/11/14.
 */
public enum QueryFilter {
  LATEST(new EmptyFilter(), 0),
  TODAY(new TodayFilter(), 1),
  UNREAD(new UnreadFilter(), 2),
  DOWNLOADED(new EmptyFilter(), 3),
  ARCHIVED(new EmptyFilter(), 4);

  private final int index;

  private final SelectionFilter selectionFilter;

  QueryFilter(SelectionFilter filter, int index){
    this.selectionFilter = filter;
    this.index = index;
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

  /**
   * Retrieves the index identifying the filter
   * @return index identifying the filter
   */
  int index(){
    return index;
  }

  public interface SelectionFilter {

    /**
     * Retrieves the index identifying the filter
     * @return index identifying the filter
     */
    int index();

    /**
     * Gives the selection query to put in an Android query call
     * @return
     */
    String getSelectionQuery();

    /**
     * Gives one of the selection values to put in an Android query call
     * @return
     */
    String[] getSelectionValues();
  }
}
