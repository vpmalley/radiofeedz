package fr.vpm.audiorss.process;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import java.util.HashSet;
import java.util.Set;

import fr.vpm.audiorss.R;

/**
 * Created by vince on 03/11/14.
 */
public class FeedChoiceModeListener implements AbsListView.MultiChoiceModeListener {

  private final ContextualActions contextualActions;

  private final Set<Integer> selection = new HashSet<Integer>();

  private final int menuResource;

  public FeedChoiceModeListener(ContextualActions contextualActions, int menuResource) {
    this.contextualActions = contextualActions;
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
      contextualActions.deleteFeeds(selection);
      actionMode.finish();
      return true;
    } else if (R.id.action_refresh == menuItem.getItemId()) {
      contextualActions.refreshFeeds(selection);
      actionMode.finish();
      return true;
    }
    return false;
  }

  @Override
  public void onDestroyActionMode(ActionMode actionMode) {
    selection.clear();
  }
}
