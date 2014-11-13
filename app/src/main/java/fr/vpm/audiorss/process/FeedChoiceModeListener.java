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
public class FeedChoiceModeListener<T> implements AbsListView.MultiChoiceModeListener {

  private final DataModel dataModel;

  private final Set<Integer> selection = new HashSet<Integer>();

  private final int menuResource;

  public FeedChoiceModeListener(DataModel dataModel, int menuResource) {
    this.dataModel = dataModel;
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
    return true;
  }

  @Override
  public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
    return false;
  }

  @Override
  public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
    if (R.id.action_delete == menuItem.getItemId()) {
      dataModel.deleteData(selection);
      actionMode.finish();
      return true;
    } else if (R.id.action_read == menuItem.getItemId()) {
      dataModel.markDataRead(selection, true);
      actionMode.finish();
      return true;
    } else if (R.id.action_unread == menuItem.getItemId()) {
      dataModel.markDataRead(selection, false);
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
