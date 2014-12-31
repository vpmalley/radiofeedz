package fr.vpm.audiorss.process;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.R;
import fr.vpm.audiorss.db.AsyncDbDeleteRSSChannel;
import fr.vpm.audiorss.db.LoadDataRefreshViewCallback;
import fr.vpm.audiorss.db.filter.ArchivedFilter;
import fr.vpm.audiorss.db.filter.AudioFilter;
import fr.vpm.audiorss.db.filter.ChannelFilter;
import fr.vpm.audiorss.db.filter.DownloadedFilter;
import fr.vpm.audiorss.db.filter.EmptyFilter;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.db.filter.TodayFilter;
import fr.vpm.audiorss.db.filter.UnreadFilter;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 *
 * The data model for the navigation drawer
 *
 * This is where a new filter should be added to appear in the navigation drawer.
 * For that,
 * 1. create a new subclass SelectionFilter in package fr.vpm.audiorss.db.filter
 * 2. add an instance of this filter at the end of the STATIC_ITEM_FILTERS array, below
 * 3. add a resource for the name of this filter at the end of the STATIC_ITEM_TITLES array, below
 * (and do not forget to add a French translation for that)
 *
 * Created by vince on 12/12/14.
 */
public class NavigationDrawerList implements NavigationDrawerProvider {

  /**
   * The filters for the first items of the list.
   * Must have the same length as STATIC_ITEM_TITLES
   */
  private static final SelectionFilter[] STATIC_ITEM_FILTERS = new SelectionFilter[] {
          new EmptyFilter(), new TodayFilter(), new UnreadFilter(), new DownloadedFilter(), new ArchivedFilter(), new AudioFilter()
  };

  /**
   * The titles for the first items of the list.
   * Must have the same length as STATIC_ITEM_FILTERS
   */
  private static final int[] STATIC_ITEM_TITLES = new int[] {
          R.string.drawer_latest, R.string.drawer_today, R.string.drawer_unread, R.string.drawer_downloaded, R.string.drawer_archived, R.string.drawer_audio
  };

  private final Context context;

  private final LoadDataRefreshViewCallback<RSSChannel> loadCallback;

  private final List<NavigationDrawerItem> items = new ArrayList<NavigationDrawerItem>();

  public NavigationDrawerList(Context context, DataModel dataModel, ProgressListener progressListener) {
    this.context = context;
    this.loadCallback = new LoadDataRefreshViewCallback<RSSChannel>(progressListener, dataModel);
  }

  public void clear() {
    this.items.clear();
  }

  public void addStaticItems() {
    for (int i = 0; i < STATIC_ITEM_FILTERS.length; i++) {
      NavigationDrawerItem drawerItem = new NavigationDrawerItem(STATIC_ITEM_FILTERS[i], context.getString(STATIC_ITEM_TITLES[i]), null);
      items.add(i, drawerItem);
    }
  }

  public void addChannels(List<RSSChannel> channels) {
    for (RSSChannel channel : channels) {
      items.add(new NavigationDrawerItem(new ChannelFilter(channel.getId()), channel.getTitle(), channel));
    }
  }

  public ArrayAdapter<NavigationDrawerItem> getAdapter(int layout) {
    return new ArrayAdapter<NavigationDrawerItem>(context, layout, items);
  }

  @Override
  public void deleteData(Collection<Integer> selection) {
    AsyncDbDeleteRSSChannel feedDeletion = new AsyncDbDeleteRSSChannel(loadCallback, context);
    int i = 0;
    // first figure how many selected feeds are actually feeds
    for (int position : selection) {
      if (items.get(position).hasBoundChannel()) {
        i++;
      }
    }

    // fill an array with the selected feeds
    RSSChannel[] feedsToDelete = new RSSChannel[i];
    i = 0;
    for (int position : selection) {
      if (items.get(position).hasBoundChannel()) {
        feedsToDelete[i++] = items.get(position).getBoundChannel();
      }
    }

    // delete these feeds
    Log.d("deletefeed", String.valueOf(feedsToDelete.length));
    if (feedsToDelete.length > 0) {
      feedDeletion.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, feedsToDelete);
    }
  }

  @Override
  public void markDataRead(Set<Integer> selection, boolean isRead) {
    // do nothing
  }

  @Override
  public void downloadMedia(Set<Integer> selection) {
    // do nothing
  }

  /**
   * Simple data structure for any item to show up in the navigation drawer
   */
  public class NavigationDrawerItem {

    private final SelectionFilter filter;

    private final String title;

    private final RSSChannel boundChannel;

    public NavigationDrawerItem(SelectionFilter filter, String title, RSSChannel boundChannel) {
      this.filter = filter;
      this.title = title;
      this.boundChannel = boundChannel;
    }

    public SelectionFilter getFilter(){
      return filter;
    }

    public boolean hasBoundChannel(){
      return boundChannel != null;
    }

    public RSSChannel getBoundChannel() {
      return boundChannel;
    }

    @Override
    public String toString() {
      return title;
    }
  }

}
