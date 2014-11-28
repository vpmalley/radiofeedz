package fr.vpm.audiorss.process;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import fr.vpm.audiorss.AllFeedItems;
import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.R;
import fr.vpm.audiorss.db.AsyncDbDeleteRSSChannel;
import fr.vpm.audiorss.db.AsyncDbReadRSSChannel;
import fr.vpm.audiorss.db.ChannelRefreshViewCallback;
import fr.vpm.audiorss.db.LoadDataRefreshViewCallback;
import fr.vpm.audiorss.db.filter.QueryFilter;
import fr.vpm.audiorss.http.AsyncFeedRefresh;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.SaveFeedCallback;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 09/11/14.
 */
public class FeedsManagerDataModel implements DataModel.RSSChannelDataModel {

  private final Activity activity;

  private final ProgressListener progressListener;

  private final FeedsActivity<RSSChannelArrayAdapter> feedsActivity;

  private List<RSSChannel> feeds = new ArrayList<RSSChannel>();

  public FeedsManagerDataModel(Activity activity, ProgressListener progressListener, FeedsActivity<RSSChannelArrayAdapter> feedsActivity) {
    this.activity = activity;
    this.progressListener = progressListener;
    this.feedsActivity = feedsActivity;
  }

  @Override
  public void loadData() {
    ChannelRefreshViewCallback callback = new ChannelRefreshViewCallback(progressListener, this);
    AsyncDbReadRSSChannel asyncDbReader = new AsyncDbReadRSSChannel(callback, feedsActivity.getContext(), false);
    // read all RSSChannel items from DB and refresh views
    asyncDbReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

  }

  @Override
  public void setChannelsAndBuildModel(List<RSSChannel> feeds) {
    this.feeds = feeds;
  }

  @Override
  public void refreshView() {
    RSSChannelArrayAdapter feedAdapter = new RSSChannelArrayAdapter(activity, R.layout.list_rss_feeds,
        feeds);
    feedsActivity.refreshView(feedAdapter);
  }

  @Override
  public Context getContext() {
    return activity;
  }

  @Override
  public void refreshData() {
    if (new DefaultNetworkChecker().checkNetworkForRefresh(getContext(), false)) {
      for (RSSChannel channel : feeds) {
        SaveFeedCallback callback = new SaveFeedCallback(progressListener, this);
        new AsyncFeedRefresh(getContext(), callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                channel.getUrl());
      }
    }
  }

  @Override
  public void addData(String feedUrl) {
    FeedAdder feedAdder = new FeedAdder(this, new DefaultNetworkChecker(), progressListener);
    if (feedUrl == null){
      feedUrl = feedAdder.retrieveFeedFromClipboard();
    }
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
  public void downloadMedia(Set<Integer> selection) {
    // do nothing
  }

  @Override
  public boolean isReady() {
    return !feeds.isEmpty();
  }

  @Override
  public Bundle getFeedItem(int position) {
    return new Bundle();
  }

  @Override
  public void filterData(List<QueryFilter> filters) {
    // do nothing
  }

  @Override
  public int size() {
    return feeds.size();
  }

  @Override
  public AdapterView.OnItemClickListener getOnItemClickListener() {
    return new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (!feeds.isEmpty()) {
          Intent i = new Intent(getContext(), AllFeedItems.class);
          i.putExtra(AllFeedItems.CHANNEL_ID, new long[]{feeds.get(position).getId()});
          getContext().startActivity(i);
        }
      }
    };
  }
}
