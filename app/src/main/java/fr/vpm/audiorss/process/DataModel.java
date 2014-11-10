package fr.vpm.audiorss.process;

import android.content.Context;
import android.widget.AdapterView;

import java.util.Collection;
import java.util.List;

/**
 * Created by vince on 09/11/14.
 */
public interface DataModel<T> {

  /**
   * Loads the data and refreshes the view once data is retrieved.
   */
  void loadData();

  /**
   * Sets the loaded data
   * @param data data loaded and used by the activity
   */
  void setDataAndBuildModel(List<T> data);

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

  void addData();

  AdapterView.OnItemClickListener getOnItemClickListener();

  void deleteData(Collection<Integer> selection);
}
