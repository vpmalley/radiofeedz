package fr.vpm.audiorss;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import fr.vpm.audiorss.adapter.NavigationDrawerItem;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 03/11/14.
 *
 * Offer interaction with views and data for feeds manipulation.
 */
public interface FeedsActivity<T> {

  void refreshTitle(String title);

  void startRefreshProgress();

  void stopRefreshProgress();

  /**
   * Refreshes the view based on some data item
   */
  void refreshFeedItems(T data);

  /**
   * Refreshes the view based on the adapter
   */
  void refreshNavigationDrawer(ArrayAdapter<NavigationDrawerItem> feedAdapter);

  /**
   * Refreshes the time when the feeds were last refreshed
   */
  void displayLastRefreshTime(String lastRefreshTime);

  /**
   * Retrieves the Context bound with this Activity
   * @return the Android Context
   */
  Context getContext();
}
