package fr.vpm.audiorss;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.persistence.AsyncMaintenance;
import fr.vpm.audiorss.process.AllFeedItemsDataModel;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.process.NavigationDrawerProvider;
import fr.vpm.audiorss.process.RSSItemArrayAdapter;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 20/11/14.
 */
public class FeedItemReaderActivity extends FragmentActivity implements FeedsActivity<RSSItemArrayAdapter> {

  public static final String INITIAL_POSITION = "initial_position";
  public static final String ITEM_FILTER = "item_position";

  private DataModel dataModel;

  private ViewPager viewPager;

  private String initialGuid;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feed);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    viewPager = (ViewPager) findViewById(R.id.pager);

    Intent i = getIntent();
    if (i.hasExtra(INITIAL_POSITION)) {
      initialGuid = i.getStringExtra(INITIAL_POSITION);
    }

    ProgressBarListener progressBarListener = new ProgressBarListener((ProgressBar) findViewById(R.id.refreshprogress));
    dataModel = new AllFeedItemsDataModel(this, progressBarListener, this, R.layout.list_rss_item, false);
    List<SelectionFilter> selectionFilters = new ArrayList<SelectionFilter>();
    if (i.hasExtra(ITEM_FILTER)) {
      List<Parcelable> parcelledFilters = i.getParcelableArrayListExtra(ITEM_FILTER);
      for (Parcelable filter : parcelledFilters) {
        selectionFilters.add((SelectionFilter) filter);
      }
    }
    dataModel.filterData(selectionFilters, new AsyncCallbackListener.DummyCallback<List<RSSItem>>(),
        new AsyncCallbackListener.DummyCallback<List<RSSChannel>>());

    if (savedInstanceState != null) {
      initialGuid = savedInstanceState.getString("initialPosition");
    }
    new AsyncMaintenance(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @Override
  public void refreshView(RSSItemArrayAdapter data, NavigationDrawerProvider navigationDrawer) {
    int initialPosition = dataModel.getItemPositionByGuid(initialGuid);

    FragmentStatePagerAdapter rssItemAdapter = new FeedItemPagerAdapter(getSupportFragmentManager());
    viewPager.setAdapter(rssItemAdapter);
    viewPager.setCurrentItem(initialPosition);

    // trick to mark the picked item as read at the right time
    Set<Integer> read = new HashSet<Integer>();
    read.add(initialPosition);
    dataModel.markDataRead(read, true);
  }

  @Override
  public Context getContext() {
    return this;
  }

  public class FeedItemPagerAdapter extends FragmentStatePagerAdapter {

    public FeedItemPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      Fragment fragment = new FeedItemReader();
      fragment.setArguments(dataModel.getFeedItem(position));
      return fragment;
    }

    @Override
    public int getCount() {
      return dataModel.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      String title = "";
      Bundle feedItemArgs = dataModel.getFeedItem(position);
      if (feedItemArgs.containsKey(FeedItemReader.ITEM)) {
        title = ((RSSItem) feedItemArgs.getParcelable(FeedItemReader.ITEM)).getTitle();
      }
      return title;
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("initialPosition", dataModel.getItemGuidByPosition(viewPager.getCurrentItem()));
  }
}
