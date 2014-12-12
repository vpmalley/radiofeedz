package fr.vpm.audiorss.process;

import android.content.Context;
import android.os.Bundle;
import android.widget.AdapterView;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 09/11/14.
 */
public interface DataModel {

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
   * Deletes the selected items
   * @param selection the list of indices of the items
   */
  void deleteData(Collection<Integer> selection);

  /**
   * Marks the selected items as read/unread
   * @param selection the list of indices of the items
   */
  void markDataRead(Set<Integer> selection, boolean isRead);

  /**
   * Whether the data model is built and ready for display
   * @return Whether the data model is built and ready for display
   */
  boolean isReady();

  /**
   * Downloads the media associated with the selected items
   * @param selection the list of indices of the items
   */
  void downloadMedia(Set<Integer> selection);

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

  public interface RSSChannelDataModel extends DataModel, RSSChannelInput {}

  public interface RSSItemDataModel extends DataModel, RSSItemInput {}
}
