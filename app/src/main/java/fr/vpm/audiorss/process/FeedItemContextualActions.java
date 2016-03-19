package fr.vpm.audiorss.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import fr.vpm.audiorss.presentation.FeedItemsInteraction;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 19/03/16.
 */
public class FeedItemContextualActions implements ContextualActions {

  RSSItemArrayAdapter itemsAdapter;

  FeedItemsInteraction interactor;

  public FeedItemContextualActions(RSSItemArrayAdapter itemsAdapter, FeedItemsInteraction interactor) {
    this.itemsAdapter = itemsAdapter;
    this.interactor = interactor;
  }

  List<RSSItem> getItems(Collection<Integer> selection) {
    List<RSSItem> items = new ArrayList<>();
    for (int position : selection) {
        items.add(itemsAdapter.getItem(position));
    }
    return items;
  }

  @Override
  public void deleteData(Collection<Integer> selection) {
    // do nothing
  }

  @Override
  public void markDataRead(Set<Integer> selection, boolean isRead) {
    interactor.markAsRead(getItems(selection), isRead);
  }

  @Override
  public void downloadMedia(Set<Integer> selection) {
    interactor.downloadMedia(getItems(selection));
  }

  @Override
  public void refreshData(Set<Integer> selection) {
    // do nothing
  }

  @Override
  public void createPlaylist(Set<Integer> selection) {
    // do nothing
  }
}
