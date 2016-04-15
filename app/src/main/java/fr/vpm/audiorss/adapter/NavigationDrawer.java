package fr.vpm.audiorss.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.db.filter.ArchivedFilter;
import fr.vpm.audiorss.db.filter.AudioFilter;
import fr.vpm.audiorss.db.filter.ChannelFilter;
import fr.vpm.audiorss.db.filter.DownloadedFilter;
import fr.vpm.audiorss.db.filter.EmptyFilter;
import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.db.filter.TodayFilter;
import fr.vpm.audiorss.db.filter.UnreadFilter;
import fr.vpm.audiorss.interaction.FeedItemsInteraction;
import fr.vpm.audiorss.presentation.ChannelComparator;
import fr.vpm.audiorss.process.Stats;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 19/03/16.
 */
public class NavigationDrawer {

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

  private final List<NavigationDrawerItem> items = new ArrayList<NavigationDrawerItem>();

  private FeedItemsInteraction interactor;

  private ArrayAdapter<NavigationDrawerItem> rssChannelAdapter;

  public NavigationDrawer(Context context, FeedItemsInteraction interactor) {
    this.context = context;
    this.interactor = interactor;
  }

  public void clear() {
    this.items.clear();
  }

  public void addStaticItems() {
    for (int i = 0; i < STATIC_ITEM_FILTERS.length; i++) {
      NavigationDrawerItem drawerItem = new NavigationDrawerItem(STATIC_ITEM_FILTERS[i], context.getString(STATIC_ITEM_TITLES[i]), null, context.getString(STATIC_ITEM_TITLES[i]));
      items.add(i, drawerItem);
    }
  }

  public void addChannels(List<RSSChannel> channels) {
    for (RSSChannel channel : channels) {
      items.add(new NavigationDrawerItem(new ChannelFilter(channel.getId()), channel.getTitle(), channel, Stats.ACTION_FEED_FILTER));
    }
  }

  public ArrayAdapter<NavigationDrawerItem> setChannelsAndGetAdapter(List<RSSChannel> allChannels) {
    Collections.sort(allChannels, new ChannelComparator());
    clear();
    addStaticItems();
    addChannels(allChannels);
    if (rssChannelAdapter == null) {
      rssChannelAdapter = new ArrayAdapter<>(context, R.layout.list_item, items);
    } else {
      rssChannelAdapter.notifyDataSetChanged();
    }
    return rssChannelAdapter;
  }

}
