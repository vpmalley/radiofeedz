package fr.vpm.audiorss.process;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.R;
import fr.vpm.audiorss.db.AsyncDbDeleteRSSChannel;
import fr.vpm.audiorss.db.AsyncDbReadRSSChannel;
import fr.vpm.audiorss.db.LoadDataRefreshViewCallback;
import fr.vpm.audiorss.db.RefreshViewCallback;
import fr.vpm.audiorss.http.AsyncFeedRefresh;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.SaveFeedCallback;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 09/11/14.
 */
public class FeedsManagerDataModel implements DataModel<RSSChannel> {

  private final Activity activity;

  private final ProgressListener progressListener;

  private final FeedsActivity<RSSChannelArrayAdapter> feedsActivity;

  private List<RSSChannel> feeds;

  public FeedsManagerDataModel(Activity activity, ProgressListener progressListener, FeedsActivity<RSSChannelArrayAdapter> feedsActivity) {
    this.activity = activity;
    this.progressListener = progressListener;
    this.feedsActivity = feedsActivity;
  }

  @Override
  public void loadData() {
    RefreshViewCallback<RSSChannel> callback = new RefreshViewCallback<RSSChannel>(progressListener, this);
    AsyncDbReadRSSChannel asyncDbReader = new AsyncDbReadRSSChannel(callback, feedsActivity.getContext());
    // read all RSSChannel items from DB and refresh views
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

  }

  @Override
  public void setDataAndBuildModel(List<RSSChannel> feeds) {
    this.feeds = feeds;
  }

  @Override
  public void refreshView() {
    RSSChannelArrayAdapter feedAdapter = new RSSChannelArrayAdapter(activity, R.layout.list_item,
        feeds);
    feedsActivity.refreshView(feedAdapter);
  }

  @Override
  public Context getContext() {
    return activity;
  }

  @Override
  public void refreshData() {
    SaveFeedCallback.updateFeedsCounter(feeds.size());
    for (RSSChannel channel : feeds) {
      SaveFeedCallback callback = new SaveFeedCallback(progressListener, this);
      new AsyncFeedRefresh(getContext(), callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
          channel.getUrl());
    }
  }

  @Override
  public void addData() {
    FeedAdder feedAdder = new FeedAdder(this, new DefaultNetworkChecker(), progressListener);
    String feedUrl = feedAdder.retrieveFeedFromClipboard();
    if (feedUrl != null) {
      feedAdder.askForFeedValidation(feeds, feedUrl);
    } else {
      feedAdder.tellToCopy();
    }
  }

  @Override
  public void deleteData(Collection<Integer> selection) {
    LoadDataRefreshViewCallback callback = new LoadDataRefreshViewCallback(progressListener, this);
    AsyncDbDeleteRSSChannel feedDeletion = new AsyncDbDeleteRSSChannel(callback, getContext());
    RSSChannel[] feedsToDelete = new RSSChannel[selection.size()];
    int i = 0;
    for (int position : selection) {
      feedsToDelete[i++] = feeds.get(position);
    }
    feedDeletion.execute(feedsToDelete);
  }

  @Override
  public void markDataRead(Set<Integer> selection, boolean isRead) {
    // do nothing
  }

  @Override
  public AdapterView.OnItemClickListener getOnItemClickListener() {
    return new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // do nothing
      }
    };
  }
}
