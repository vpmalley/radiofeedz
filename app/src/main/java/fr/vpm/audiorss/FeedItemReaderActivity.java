package fr.vpm.audiorss;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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
public class FeedItemReaderActivity extends AppCompatActivity implements FeedsActivity<RSSItemArrayAdapter> {

  public static final String INITIAL_POSITION = "initial_position";
  public static final String ITEM_FILTER = "item_position";

  private DataModel dataModel;

  private ViewPager viewPager;

  private String initialGuid;

  private boolean firstRefresh = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feed);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    viewPager = (ViewPager) findViewById(R.id.pager);

    Intent i = getIntent();
    if (i.hasExtra(INITIAL_POSITION)) {
      initialGuid = i.getStringExtra(INITIAL_POSITION);
    }

    ProgressBarListener progressBarListener = new ProgressBarListener((ProgressBar) findViewById(R.id.refreshprogress));
    dataModel = new AllFeedItemsDataModel(this, progressBarListener, this, R.layout.list_rss_item);
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

    int randomTask = new Random().nextInt(20);
    if (randomTask < 10) {
      new AsyncMaintenance(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      dataModel.preRefreshData();
    }
  }

  @Override
  public void refreshFeedItems(RSSItemArrayAdapter data) {
    if (!firstRefresh) {
      final int initialPosition = dataModel.getItemPositionByGuid(initialGuid);

      FragmentStatePagerAdapter rssItemAdapter = new FeedItemPagerAdapter(getSupportFragmentManager());
      viewPager.setAdapter(rssItemAdapter);
      viewPager.setCurrentItem(initialPosition);
      viewPager.getAdapter().notifyDataSetChanged();

      firstRefresh = true;

      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          // trick to mark the picked item as read at the right time
          final Set<Integer> read = new HashSet<Integer>();
          read.add(initialPosition);

          dataModel.markDataRead(read, true);
        }
      }, 2000);
    } else {
      viewPager.getAdapter().notifyDataSetChanged();
    }
  }

  @Override
  public void refreshNavigationDrawer(NavigationDrawerProvider navigationDrawer) {

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
