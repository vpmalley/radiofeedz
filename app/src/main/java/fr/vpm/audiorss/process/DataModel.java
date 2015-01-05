package fr.vpm.audiorss.process;

import android.content.Context;
import android.os.Bundle;
import android.widget.AdapterView;

import java.util.List;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 09/11/14.
 */
public interface DataModel extends ContextualActions {

  /**
   * Loads the data and refreshes the view once data is retrieved.
   */
  void loadData();

  /**
   * Refreshes the view based on activityś data
   */
  void refreshView();

  /**
   * Retrieves the Context bound with this Activity
   * @return the Android Context
   */
  Context getContext();

  void refreshData();

  void addData(String feedUrl);

  AdapterView.OnItemClickListener getOnItemClickListener();

  /**
   * Whether the data model is built and ready for display
   * @return Whether the data model is built and ready for display
   */
  boolean isReady();

  Bundle getFeedItem(int position);

  /**
   * Filters the elements based on passed filter and reloads the data from the DB
   *
   * @param filters the filter to apply on the DB query
   */
  void filterData(List<SelectionFilter> filters);

  /**
   * The number of elements in this structure
   * @return the number of elements in this structure
   */
  int size();

  void onFeedFailureBeforeLoad();

  interface RSSChannelInput {

    /**
     * Sets the data
     * @param data data used to for the activity display
     */
    void setChannelsAndBuildModel(List<RSSChannel> data);
  }

  interface RSSItemInput {

    /**
     * Sets the data
     * @param data data used to for the activity display
     */
    void setItemsAndBuildModel(List<RSSItem> data);
  }

  NavigationDrawerProvider getNavigationDrawer();

  public interface RSSChannelDataModel extends DataModel, RSSChannelInput {}

  public interface RSSItemDataModel extends DataModel, RSSItemInput {}
}
