package fr.vpm.audiorss.db.filter;

/**
 * Created by vince on 12/12/14.
 */
public interface SelectionFilter {

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
