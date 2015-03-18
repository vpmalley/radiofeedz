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
  void deleteData(Collection<Integer> selection);

  /**
   * Marks the selected items as read/unread
   * @param selection the list of indices of the items
   */
  void markDataRead(Set<Integer> selection, boolean isRead);

  /**
   * Downloads the media associated with the selected items
   * @param selection the list of indices of the items
   */
  void downloadMedia(Set<Integer> selection);

  /**
   * Refreshes specifically the selected feeds
   * @param selection the list of indices of the feeds
   */
  void refreshData(Set<Integer> selection);

  /**
   * Creates a playlist from the selected items
   * @param selection the list of indices of the items
   */
  void createPlaylist(Set<Integer> selection);
}
