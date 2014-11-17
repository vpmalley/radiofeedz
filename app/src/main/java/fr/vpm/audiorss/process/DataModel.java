package fr.vpm.audiorss.process;

import android.content.Context;
import android.widget.AdapterView;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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

  void deleteData(Collection<Integer> selection);

  void markDataRead(Set<Integer> selection, boolean isRead);

  /**
   * Whether the data model is built and ready for display
   * @return Whether the data model is built and ready for display
   */
  boolean isReady();

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
