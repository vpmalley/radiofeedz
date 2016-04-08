package fr.vpm.audiorss.process;

import java.util.Collection;
import java.util.Set;

/**
 * Created by vince on 15/12/14.
 */
public interface ContextualActions {

  /**
   * Deletes the selected items
   * @param selection the list of indices of the items
   */
  void deleteFeeds(Collection<Integer> selection);

  /**
   * Refreshes specifically the selected feeds
   * @param selection the list of indices of the feeds
   */
  void refreshFeeds(Set<Integer> selection);
}
