package fr.vpm.audiorss.process;

import android.widget.ArrayAdapter;

/**
 * Created by vince on 15/12/14.
 */
public interface NavigationDrawerProvider extends ContextualActions {

  /**
   * Provides the adapter containing the data for the navigation drawer list
   * @param layout the layout used for every item of the navigation drawer
   * @return the adapter containing the list of items for the navigation drawer
   */
  public ArrayAdapter<NavigationDrawer.NavigationDrawerItem> getAdapter(int layout);
}
