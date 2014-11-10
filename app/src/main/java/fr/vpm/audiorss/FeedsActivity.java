package fr.vpm.audiorss;

import android.content.Context;

/**
 * Created by vince on 03/11/14.
 *
 * Offer interaction with views and data for feeds manipulation.
 */
public interface FeedsActivity<T> {

  /**
   * Refreshes the view based on some data item
   */
  void refreshView(T data);

  /**
   * Retrieves the Context bound with this Activity
   * @return the Android Context
   */
  Context getContext();
}
