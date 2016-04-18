package fr.vpm.audiorss.adapter;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.Collections;

import fr.vpm.audiorss.interaction.FeedItemsInteraction;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 18/04/16.
 */
public class FeedItemsListRecyclerListener implements AbsListView.RecyclerListener {

  private FeedItemsInteraction feedItemsInteraction;

  private RSSItemArrayAdapter rssItemArrayAdapter;

  public FeedItemsListRecyclerListener(FeedItemsInteraction feedItemsInteraction, RSSItemArrayAdapter rssItemArrayAdapter) {
    this.feedItemsInteraction = feedItemsInteraction;
    this.rssItemArrayAdapter = rssItemArrayAdapter;
  }

  @Override
  public void onMovedToScrapHeap(View view) {
    int position = getListView(view).getPositionForView(view);
    RSSItem rssItem = rssItemArrayAdapter.getItem(position).getRssItem();
    feedItemsInteraction.markAsRead(Collections.singletonList(rssItem), true);
    rssItem.setRead(true);
  }

  private ListView getListView(View view) {
    return (ListView) view.getParent();
  }
}
