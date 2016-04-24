package fr.vpm.audiorss.adapter;

import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.vpm.audiorss.interaction.FeedItemsInteraction;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 18/04/16.
 */
public class FeedItemsListRecyclerListener implements AbsListView.RecyclerListener {

  private FeedItemsInteraction feedItemsInteraction;

  private Map<Integer, RSSItem> visibleItems = new HashMap<>();

  public FeedItemsListRecyclerListener(FeedItemsInteraction feedItemsInteraction) {
    this.feedItemsInteraction = feedItemsInteraction;
  }

  public void onAppear(int position, RSSItem rssItem) {
    Log.d("recycling", "APP: item " + position + ", " + rssItem.getTitle() + " appeared.");
    visibleItems.put(position, rssItem);
  }

  public void onDisappear(int position, RSSItem rssItem) {
    Log.d("recycling", "DIS: item " + position + ", " + rssItem.getTitle() + " disappeared.");
    rssItem.setRead(true);
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
