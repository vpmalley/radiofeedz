package fr.vpm.audiorss.adapter;

import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.vpm.audiorss.interaction.FeedItemsInteraction;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 18/04/16.
 */
public class FeedItemsListRecyclerListener implements AbsListView.RecyclerListener {

  private FeedItemsInteraction feedItemsInteraction;

  private List<RSSItem> visibleItems = new ArrayList<>();

  public FeedItemsListRecyclerListener(FeedItemsInteraction feedItemsInteraction) {
    this.feedItemsInteraction = feedItemsInteraction;
  }

  public void onAppear(int position, RSSItem rssItem) {
    Log.d("recycling", "APP: item " + position + ", " + rssItem.getTitle() + " appeared.");
    visibleItems.add(position, rssItem);
  }

  public void onDisappear(int position, RSSItem rssItem) {
    Log.d("recycling", "DIS: item " + position + ", " + rssItem.getTitle() + " disappeared.");
    feedItemsInteraction.markAsRead(Collections.singletonList(rssItem), true);
  }

  @Override
  public void onMovedToScrapHeap(View view) {
    ListView listView = getListView(view);

    if (listView != null) {
      int position = listView.getPositionForView(view);
      RSSItem rssItem = visibleItems.get(position);
      if (rssItem != null) {
        onDisappear(position, rssItem);
      }
    }
  }

  private ListView getListView(View view) {
    return (ListView) view.getParent();
  }
}
