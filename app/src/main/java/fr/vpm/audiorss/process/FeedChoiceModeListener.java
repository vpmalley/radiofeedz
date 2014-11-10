package fr.vpm.audiorss.process;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import java.util.HashSet;
import java.util.Set;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 03/11/14.
 */
public class FeedChoiceModeListener implements AbsListView.MultiChoiceModeListener {

  private final DataModel<RSSChannel> dataModel;

  private final Set<Integer> selection = new HashSet<Integer>();

  public FeedChoiceModeListener(DataModel<RSSChannel> dataModel) {
    this.dataModel = dataModel;
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
    actionMode.getMenuInflater().inflate(R.menu.feeds_context, menu);
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
    }
    return false;
  }

  @Override
  public void onDestroyActionMode(ActionMode actionMode) {
    selection.clear();
  }
}
