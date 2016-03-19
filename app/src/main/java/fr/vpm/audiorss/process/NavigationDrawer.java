package fr.vpm.audiorss.process;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.db.filter.ArchivedFilter;
import fr.vpm.audiorss.db.filter.AudioFilter;
import fr.vpm.audiorss.db.filter.ChannelFilter;
import fr.vpm.audiorss.db.filter.DownloadedFilter;
import fr.vpm.audiorss.db.filter.EmptyFilter;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.db.filter.TodayFilter;
import fr.vpm.audiorss.db.filter.UnreadFilter;
import fr.vpm.audiorss.presentation.FeedItemsInteraction;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 19/03/16.
 */
public class NavigationDrawer implements NavigationDrawerProvider {

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

  private final List<NavigationDrawerList.NavigationDrawerItem> items = new ArrayList<NavigationDrawerList.NavigationDrawerItem>();

  private FeedItemsInteraction interactor;

  public NavigationDrawer(Context context, FeedItemsInteraction interactor) {
    this.context = context;
    this.interactor = interactor;
  }

  public void clear() {
    this.items.clear();
  }

  public void addStaticItems() {
    for (int i = 0; i < STATIC_ITEM_FILTERS.length; i++) {
      NavigationDrawerList.NavigationDrawerItem drawerItem = new NavigationDrawerList.NavigationDrawerItem(STATIC_ITEM_FILTERS[i], context.getString(STATIC_ITEM_TITLES[i]), null, context.getString(STATIC_ITEM_TITLES[i]));
      items.add(i, drawerItem);
    }
  }

  public void addChannels(List<RSSChannel> channels) {
    for (RSSChannel channel : channels) {
      items.add(new NavigationDrawerList.NavigationDrawerItem(new ChannelFilter(channel.getId()), channel.getTitle(), channel, Stats.ACTION_FEED_FILTER));
    }
  }

  private List<RSSChannel> getChannels(Collection<Integer> selection) {
    List<RSSChannel> channels = new ArrayList<>();
    for (int position : selection) {
      if (items.get(position).hasBoundChannel()) {
        channels.add(items.get(position).getBoundChannel());
      }
    }
    return channels;
  }


  @Override
  public ArrayAdapter<NavigationDrawerList.NavigationDrawerItem> getAdapter(int layout) {
    return new ArrayAdapter<NavigationDrawerList.NavigationDrawerItem>(context, layout, items);
  }


  @Override
  public void deleteData(Collection<Integer> selection) {
    interactor.deleteFeeds(getChannels(selection));
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
  public void refreshData(Set<Integer> selection) {
    interactor.retrieveLatestFeedItems(getChannels(selection));
  }

  @Override
  public void createPlaylist(Set<Integer> selection) {
    // do nothing
  }
}
