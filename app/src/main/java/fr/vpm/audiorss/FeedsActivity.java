package fr.vpm.audiorss;

import android.content.Context;

/**
 * Created by vince on 03/11/14.
 *
 * Offer interaction with views and data for feeds manipulation.
 */
public interface FeedsActivity<T> {

  /**
   * Loads the data and refreshes the view once data is retrieved.
   */
  void loadDataAndRefreshView();

  /**
   * Sets the loaded data
   * @param data data loaded and used by the activity
   */
  void setData(T data);

  /**
   * Refreshes the view based on activityś data
   */
  void refreshView();
}
