package fr.vpm.audiorss;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ProgressBar;

import fr.vpm.audiorss.process.AllFeedItemsDataModel;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.process.RSSItemArrayAdapter;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 20/11/14.
 */
public class FeedItemReaderActivity extends FragmentActivity implements FeedsActivity<RSSItemArrayAdapter> {

  public static final String INITIAL_POSITION = "initial_position";

  private DataModel dataModel;

  private ViewPager viewPager;

  private int initialPosition = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feed);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    Intent i = getIntent();
    Long[] channelIds = new Long[0];
    if (i.hasExtra(AllFeedItems.CHANNEL_ID)) {
      long[] chIds = i.getExtras().getLongArray(AllFeedItems.CHANNEL_ID);
      int j = 0;
      channelIds = new Long[chIds.length];
      for (long chId : chIds){
        channelIds[j++] = chId;
      }
    }
    if (i.hasExtra(INITIAL_POSITION)) {
      initialPosition = i.getIntExtra(INITIAL_POSITION, 0);
    }

    ProgressBarListener progressBarListener = new ProgressBarListener((ProgressBar) findViewById(R.id.refreshprogress));
    dataModel = new AllFeedItemsDataModel(this, progressBarListener, this, channelIds, R.layout.list_rss_item);
    dataModel.loadData();

    viewPager = (ViewPager) findViewById(R.id.pager);

  }

  @Override
  public void refreshView(RSSItemArrayAdapter data) {
    FragmentStatePagerAdapter rssItemAdapter = new FeedItemPagerAdapter(getSupportFragmentManager());
    viewPager.setAdapter(rssItemAdapter);
    viewPager.setCurrentItem(initialPosition);
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

}
