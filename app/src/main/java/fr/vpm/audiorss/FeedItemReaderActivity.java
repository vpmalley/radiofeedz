package fr.vpm.audiorss;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

  private DataModel dataModel;

  private ViewPager viewPager;

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

    ProgressBarListener progressBarListener = new ProgressBarListener((ProgressBar) findViewById(R.id.refreshprogress));
    dataModel = new AllFeedItemsDataModel(this, progressBarListener, this, channelIds);
    dataModel.loadData();

    viewPager = (ViewPager) findViewById(R.id.pager);

  }

  @Override
  public void refreshView(RSSItemArrayAdapter data) {
    FragmentStatePagerAdapter rssItemAdapter = new FeedItemPagerAdapter(getSupportFragmentManager());
    viewPager.setAdapter(rssItemAdapter);
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
      return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pref_disp_max_items", "80"));
    }

    @Override
    public CharSequence getPageTitle(int position) {
      RSSItem rssItem = dataModel.getFeedItem(position).getParcelable(FeedItemReader.ITEM);
      return rssItem.getTitle();
    }
  }

}
