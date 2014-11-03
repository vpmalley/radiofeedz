package fr.vpm.audiorss;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import fr.vpm.audiorss.db.AsyncDbReadRSSChannel;
import fr.vpm.audiorss.db.LoadDataRefreshViewCallback;
import fr.vpm.audiorss.db.RefreshViewCallback;
import fr.vpm.audiorss.process.FeedChoiceModeListener;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 03/11/14.
 */
public class FeedsManager extends Activity implements FeedsActivity<List<RSSChannel>>, ProgressListener {

  private ListView mFeeds;

  private ProgressBar mRefreshProgress;

  private List<RSSChannel> feeds;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feeds);
    mRefreshProgress = (ProgressBar) findViewById(R.id.refreshprogress);
    mFeeds = (ListView) findViewById(R.id.list);
    mFeeds.setTextFilterEnabled(true);

    loadDataAndRefreshView();
  }

  @Override
  public void loadDataAndRefreshView() {
    RefreshViewCallback callback = new RefreshViewCallback(this, this);
    AsyncDbReadRSSChannel asyncDbReader = new AsyncDbReadRSSChannel(callback, this);
    // read all RSSChannel items from DB and refresh views
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[0]);
  }

  @Override
  public void setData(List<RSSChannel> feeds) {
    this.feeds = feeds;
  }

  public void refreshView() {
    ArrayAdapter<RSSChannel> itemAdapter = new ArrayAdapter<RSSChannel>(this, R.layout.activity_item,
        feeds);
    // fill ListView with all the feeds
    mFeeds.setAdapter(itemAdapter);
  }

  /**
   * Defines the listener when long clicking on one or multiple items of the list
   */
  private void setContextualListeners() {
    mFeeds.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    LoadDataRefreshViewCallback callback = new LoadDataRefreshViewCallback(this, this);
    final AbsListView.MultiChoiceModeListener actionModeCallback = new FeedChoiceModeListener(feeds, callback, this);
    mFeeds.setMultiChoiceModeListener(actionModeCallback);
  }

  public void startRefreshProgress() {
    mRefreshProgress.setVisibility(View.VISIBLE);
  }

  public void updateProgress(int progress) {
    mRefreshProgress.setProgress(progress);
  }

  public void stopRefreshProgress() {
    mRefreshProgress.setVisibility(View.GONE);
  }
}
