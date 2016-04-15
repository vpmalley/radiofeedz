package fr.vpm.audiorss.adapter;

import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.interaction.FeedItemsInteraction;
import fr.vpm.audiorss.process.Stats;

/**
 * Created by vince on 15/04/16.
 */
public class NavigationDrawerClickListener implements AdapterView.OnItemClickListener {

  private FeedsActivity feedsActivity;
  private DrawerLayout drawerLayout;
  private ListView drawerList;
  private FeedItemsInteraction interaction;

  public NavigationDrawerClickListener(FeedsActivity feedsActivity, DrawerLayout drawerLayout, ListView drawerView, FeedItemsInteraction interaction) {
    this.feedsActivity = feedsActivity;
    this.drawerLayout = drawerLayout;
    this.drawerList = drawerView;
    this.interaction = interaction;
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    NavigationDrawerItem navigationDrawerItem = (NavigationDrawerItem) drawerList.getAdapter().getItem(position);
    Stats.get(feedsActivity.getContext()).increment(navigationDrawerItem.getStatTag());
    feedsActivity.resetFeedItemsListSelection();
    feedsActivity.refreshTitle(navigationDrawerItem.getTitle());
    feedsActivity.startRefreshProgress();
    interaction.loadFeedItems(navigationDrawerItem.getFilter());
    drawerLayout.closeDrawer(drawerList);
  }
}
