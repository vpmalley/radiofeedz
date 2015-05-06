package fr.vpm.audiorss.process;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import fr.vpm.audiorss.rss.CachableRSSChannel;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 05/05/15.
 */
public class CacheManager {

  private final CopyOnWriteArrayList<CachableRSSChannel> rssChannels;

  private CacheManager(List<RSSChannel> rssChannels) {
    this.rssChannels = new CopyOnWriteArrayList<>();
    for (RSSChannel rssChannel : rssChannels) {
      this.rssChannels.add(new CachableRSSChannel(rssChannel));
    }
  }

  private static CacheManager manager;

  public static CacheManager createManager(List<RSSChannel> rssChannels) {
    if (manager == null) {
      manager = new CacheManager(rssChannels);
    }
    return manager;
  }

  public static CacheManager getManager() {
    return manager;
  }

  public void updateCache(final Context context) {
    queueCacheQueries(context);
    queueCacheProcess(context);
    queueCachePersistence(context);
    TaskManager.getManager().startTasks();
  }

  private void queueCacheQueries(final Context context) {
    for (final CachableRSSChannel rssChannel : rssChannels) {
      if (rssChannel.shouldRefresh()) {
        TaskManager.getManager().queueTask(new TaskManager.Task() {
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

          @Override
          public TaskManager.Priority getPriority() {
            return TaskManager.Priority.HIGH;
          }
        });
      }
    }
  }

  private void queueCacheProcess(final Context context) {
    for (final CachableRSSChannel rssChannel : rssChannels) {
      if (rssChannel.shouldRefresh()) {
        TaskManager.getManager().queueTask(new TaskManager.Task() {
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

          @Override
          public TaskManager.Priority getPriority() {
            return TaskManager.Priority.MEDIUM;
          }
        });
      }
    }
  }

  private void queueCachePersistence(final Context context) {
    for (final CachableRSSChannel rssChannel : rssChannels) {
      if (rssChannel.shouldRefresh()) {
        TaskManager.getManager().queueTask(new TaskManager.Task() {
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

          @Override
          public TaskManager.Priority getPriority() {
            return TaskManager.Priority.LOW;
          }
        });
      }
    }
  }
}
