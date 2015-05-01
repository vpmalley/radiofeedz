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
   * @param itemsLoadedCallback
   * @param channelsLoadedCallback
   * @param loadCallback
   */
  void loadData(AsyncCallbackListener<List<RSSItem>> itemsLoadedCallback, AsyncCallbackListener<List<RSSChannel>> channelsLoadedCallback, AsyncCallbackListener loadCallback);

  /**
   * Refreshes the view based on activityś data
   */
  void refreshView();

  /**
   * Retrieves the Context bound with this Activity
   * @return the Android Context
   */
  Context getContext();

  void preRefreshData();

  void refreshData();

  void refreshData(List<RSSChannel> feedsToUpdate);

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
   *  @param filters the filter to apply on the DB query
   * @param itemsLoadedCallback
   * @param channelsLoadedCallback
   */
  void filterData(List<SelectionFilter> filters, AsyncCallbackListener<List<RSSItem>> itemsLoadedCallback, AsyncCallbackListener<List<RSSChannel>> channelsLoadedCallback);

  /**
   * The number of elements in this structure
   * @return the number of elements in this structure
   */
  int size();

  void onFeedFailureBeforeLoad();

  /**
   * Determines the latest date any of the feeds was refreshed
   * @return
   */
  String getLastBuildDate();

  /**
   * Gives the position of an item based on its unique GUID
   * @param guid the unique identifier of the new item
   * @return the current position in the list
   */
  int getItemPositionByGuid(String guid);

  /**
   * Gives the GUID of an item based on its position in the list
   * @param position the current position in the list
   * @return the unique identifier of the new item
   */
  String getItemGuidByPosition(int position);

  void dataToPostProcess(ItemParser itemParser);

  void postProcessData();

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
