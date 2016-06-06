package fr.vpm.audiorss.adapter;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.interaction.FeedItemsInteraction;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 03/11/14.
 */
public class FeedContextualActionListener implements AbsListView.MultiChoiceModeListener {

  private final FeedItemsInteraction interactor;

  private final ArrayAdapter<NavigationDrawerItem> navigationDrawerItemArrayAdapter;

  private final Set<Integer> selection = new HashSet<Integer>();

  private final int menuResource;

  public FeedContextualActionListener(FeedItemsInteraction interactor, ArrayAdapter<NavigationDrawerItem> navigationDrawerItemArrayAdapter, int menuResource) {
    this.interactor = interactor;
    this.navigationDrawerItemArrayAdapter = navigationDrawerItemArrayAdapter;
    this.menuResource = menuResource;
  }

  @Override
  public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean checked) {
    if (checked) {
      selection.add(position);
    } else {
      selection.remove(position);
    }
  }

  @Override
  public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
    actionMode.getMenuInflater().inflate(menuResource, menu);
    MenuItem playlistItem = menu.findItem(R.id.action_playlist);
    if (playlistItem != null) {
      playlistItem.setVisible(false);
    }
    return true;
  }

  @Override
  public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
    return false;
  }

  @Override
  public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
    if (R.id.action_delete == menuItem.getItemId()) {
      interactor.deleteFeeds(getChannels(selection));
      actionMode.finish();
      return true;
    } else if (R.id.action_refresh == menuItem.getItemId()) {
      interactor.retrieveLatestFeedItems(getChannels(selection));
      actionMode.finish();
      return true;
    }
    return false;
  }

  private List<RSSChannel> getChannels(Collection<Integer> selection) {
    List<RSSChannel> channels = new ArrayList<>();
    for (int position : selection) {
      if (navigationDrawerItemArrayAdapter.getItem(position).hasBoundChannel()) {
        channels.add(navigationDrawerItemArrayAdapter.getItem(position).getBoundChannel());
      }
    }
    return channels;
  }

  @Override
  public void onDestroyActionMode(ActionMode actionMode) {
    selection.clear();
  }
}
