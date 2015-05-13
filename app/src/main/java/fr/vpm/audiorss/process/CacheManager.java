package fr.vpm.audiorss.process;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.rss.CachableRSSChannel;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 05/05/15.
 */
public class CacheManager {

  private final CopyOnWriteArrayList<CachableRSSChannel> rssChannels;

  private final DataModel dataModel;

  private final ProgressListener progressListener;

  public CacheManager(List<RSSChannel> rssChannels, DataModel dataModel, ProgressListener progressListener) {
    this.dataModel = dataModel;
    this.progressListener = progressListener;
    this.rssChannels = new CopyOnWriteArrayList<>();
    for (RSSChannel rssChannel : rssChannels) {
      this.rssChannels.add(new CachableRSSChannel(rssChannel));
    }
  }

  public CacheManager(String rssUrl, DataModel dataModel, ProgressListener progressListener) {
    this.dataModel = dataModel;
    this.progressListener = progressListener;
    this.rssChannels = new CopyOnWriteArrayList<>();
    this.rssChannels.add(new CachableRSSChannel(rssUrl));
  }

  public void updateCache(final Context context) {
    progressListener.startRefreshProgress();
    queueCacheQueries(context);
    queueCacheProcess(context);
    queueCachePersistence(context);
    queueCacheLoading(context);
    queueProgressListener();
    queueCachePostProcess(context);
    queueCachePersistence(context);
    queueCacheLoading(context);
    TaskManager.getManager().startTasks();
  }

  private void queueCacheQueries(final Context context) {
    for (final CachableRSSChannel rssChannel : rssChannels) {
      if (rssChannel.shouldRefresh()) {
        TaskManager.getManager().queueTask(new TaskManager.AsynchTask() {
          @Override
          public boolean shouldExecute() {
            return !rssChannel.failed();
          }

          @Override
          public boolean canExecute() {
            Log.d("cachablerss", "query: " + rssChannel.shouldRefresh());
            return rssChannel.shouldRefresh();
          }

          @Override
          public void execute() {
            rssChannel.query(context);
          }
        });
      }
    }
  }

  private void queueCacheProcess(final Context context) {
    for (final CachableRSSChannel rssChannel : rssChannels) {
      if (rssChannel.shouldRefresh()) {
        TaskManager.getManager().queueTask(new TaskManager.AsynchTask() {
          @Override
          public boolean shouldExecute() {
            return !rssChannel.failed();
          }

          @Override
          public boolean canExecute() {
            Log.d("cachablerss", "process: " + rssChannel.isQueried());
            return rssChannel.isQueried();
          }

          @Override
          public void execute() {
            rssChannel.process(context);
          }
        });
      }
    }
  }

  private void queueCachePersistence(final Context context) {
    for (final CachableRSSChannel rssChannel : rssChannels) {
      if (rssChannel.shouldRefresh()) {
        TaskManager.getManager().queueTask(new TaskManager.AsynchTask() {
          @Override
          public boolean shouldExecute() {
            return !rssChannel.failed();
          }

          @Override
          public boolean canExecute() {
            Log.d("cachablerss", "persist: " + rssChannel.isProcessed());
            return rssChannel.isProcessed();
          }

          @Override
          public void execute() {
            rssChannel.persist(context);
          }
        });
      }
    }
  }

  private void queueCacheLoading(Context context) {
    TaskManager.getManager().queueTask(new TaskManager.AsynchTask() {
      @Override
      public void execute() {
        dataModel.loadData(new AsyncCallbackListener.DummyCallback<List<RSSItem>>(),
            new AsyncCallbackListener.DummyCallback<List<RSSChannel>>(), new AsyncCallbackListener.DummyCallback());
      }
    });
  }

  private void queueProgressListener() {
    TaskManager.getManager().queueTask(new TaskManager.Task() {
      @Override
      public boolean shouldExecute() {
        return true;
      }

      @Override
      public boolean canExecute() {
        return true;
      }

      @Override
      public void execute() {
        progressListener.stopRefreshProgress();
      }

      @Override
      public boolean isAsynch() {
        return false;
      }
    });
  }

  private void queueCachePostProcess(final Context context) {
    for (final CachableRSSChannel rssChannel : rssChannels) {
      if (rssChannel.shouldRefresh()) {
        TaskManager.getManager().queueTask(new TaskManager.AsynchTask() {
          @Override
          public boolean shouldExecute() {
            return !rssChannel.failed();
          }

          @Override
          public boolean canExecute() {
            Log.d("cachablerss", "process: " + rssChannel.isQueried());
            return rssChannel.isQueried();
          }

          @Override
          public void execute() {
            rssChannel.postProcess(context);
          }
        });
      }
    }
  }
}
