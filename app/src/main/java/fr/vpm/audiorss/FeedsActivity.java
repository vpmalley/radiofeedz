package fr.vpm.audiorss;

import android.content.Context;

import java.util.List;

import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 03/11/14.
 *
 * Offer interaction with views and data for feeds manipulation.
 */
public interface FeedsActivity<T> {

  /**
   * Refreshes the view based on some data item
   */
  void refreshFeedItems(T data);

  /**
   * Refreshes the view based on feeds
   */
  void refreshNavigationDrawer(List<RSSChannel> allChannels);

  /**
   * Retrieves the Context bound with this Activity
   * @return the Android Context
   */
  Context getContext();
}
