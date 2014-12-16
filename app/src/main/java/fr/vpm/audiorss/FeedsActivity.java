package fr.vpm.audiorss;

import android.content.Context;

import fr.vpm.audiorss.process.NavigationDrawerProvider;

/**
 * Created by vince on 03/11/14.
 *
 * Offer interaction with views and data for feeds manipulation.
 */
public interface FeedsActivity<T> {

  /**
   * Refreshes the view based on some data item
   */
  void refreshView(T data, NavigationDrawerProvider navigationDrawer);

  /**
   * Retrieves the Context bound with this Activity
   * @return the Android Context
   */
  Context getContext();
}
