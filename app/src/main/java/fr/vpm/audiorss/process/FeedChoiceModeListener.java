package fr.vpm.audiorss.process;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.db.AsyncDbDeleteRSSChannel;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 03/11/14.
 */
public class FeedChoiceModeListener implements AbsListView.MultiChoiceModeListener {

  private final List<RSSChannel> feeds;

  private final List<RSSChannel> selectedFeeds;

  private final AsyncDbDeleteRSSChannel feedSaver;

  public FeedChoiceModeListener(List<RSSChannel> feeds, AsyncCallbackListener<List<RSSChannel>> asyncCallbackListener, Context context) {
    this.feeds = feeds;
    this.feedSaver = new AsyncDbDeleteRSSChannel(asyncCallbackListener, context);
    this.selectedFeeds = new ArrayList<RSSChannel>();
  }

  @Override
  public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean checked) {
    if (checked) {
      selectedFeeds.add(feeds.get(position));
    } else {
      selectedFeeds.remove(feeds.get(position));
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
      feedSaver.execute((RSSChannel[]) selectedFeeds.toArray());
      actionMode.finish();
      return true;
    }
    return false;
  }

  @Override
  public void onDestroyActionMode(ActionMode actionMode) {
    selectedFeeds.clear();
  }
}
