package fr.vpm.audiorss.process;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.db.filter.ArchivedFilter;
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
 * Created by vince on 12/12/14.
 */
public class NavigationDrawerList {

  /**
   * The filters for the first items of the list.
   * Must have the same length as STATIC_ITEM_TITLES
   */
  private static final SelectionFilter[] STATIC_ITEM_FILTERS = new SelectionFilter[] {
          new EmptyFilter(), new TodayFilter(), new UnreadFilter(), new DownloadedFilter(), new ArchivedFilter()
  };

  /**
   * The titles for the first items of the list.
   * Must have the same length as STATIC_ITEM_FILTERS
   */
  private static final int[] STATIC_ITEM_TITLES = new int[] {
          R.string.drawer_latest, R.string.drawer_today, R.string.drawer_unread, R.string.drawer_downloaded, R.string.drawer_archived
  };

  private final Context context;

  private final List<NavigationDrawerItem> items = new ArrayList<NavigationDrawerItem>();

  public NavigationDrawerList(Context context) {
    this.context = context;
    this.items.clear();
  }

  public void addStaticItems() {
    for (int i = 0; i < STATIC_ITEM_FILTERS.length; i++) {
      NavigationDrawerItem drawerItem = new NavigationDrawerItem(STATIC_ITEM_FILTERS[i], context.getString(STATIC_ITEM_TITLES[i]));
      items.add(i, drawerItem);
    }
  }

  public void addChannels(List<RSSChannel> channels) {
    for (RSSChannel channel : channels) {
      items.add(new NavigationDrawerItem(new ChannelFilter(channel.getId()), channel.getTitle()));
    }
  }

  public ArrayAdapter<NavigationDrawerItem> getAdapter(int layout) {
    return new ArrayAdapter<NavigationDrawerItem>(context, layout, items);
  }

  /**
   * Simple data structure for any item to show up in the navigation drawer
   */
  public class NavigationDrawerItem {

    private final SelectionFilter filter;

    private final String title;

    public NavigationDrawerItem(SelectionFilter filter, String title) {
      this.filter = filter;
      this.title = title;
    }

    public SelectionFilter getFilter(){
      return filter;
    }

    @Override
    public String toString() {
      return title;
    }
  }

}
