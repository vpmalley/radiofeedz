package fr.vpm.audiorss.db.filter;

import android.os.Parcelable;

/**
 * Created by vince on 12/12/14.
 */
public interface SelectionFilter extends Parcelable {

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
