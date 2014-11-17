package fr.vpm.audiorss;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;

import fr.vpm.audiorss.process.FeedChoiceModeListener;
import fr.vpm.audiorss.process.FeedsManagerDataModel;
import fr.vpm.audiorss.process.RSSChannelArrayAdapter;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 03/11/14.
 */
public class FeedsManager extends Activity implements FeedsActivity<RSSChannelArrayAdapter> {

  private ListView mFeeds;

  private FeedsManagerDataModel dataModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feeds);
    ProgressBarListener progressBarListener = new ProgressBarListener((ProgressBar) findViewById(R.id.refreshprogress));

    dataModel = new FeedsManagerDataModel(this, progressBarListener, this);

    mFeeds = (ListView) findViewById(R.id.list);
    mFeeds.setTextFilterEnabled(true);
    mFeeds.setOnItemClickListener(dataModel.getOnItemClickListener());

    setContextualListeners();
    dataModel.loadData();
  }

  @Override
  public void refreshView(RSSChannelArrayAdapter feedAdapter) {
    mFeeds.setAdapter(feedAdapter);
  }

  @Override
  public Context getContext() {
    return this;
  }

  /**
   * Defines the listener when long clicking on one or multiple items of the list
   */
  private void setContextualListeners() {
    mFeeds.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    FeedChoiceModeListener<RSSChannel> actionModeCallback = new FeedChoiceModeListener<RSSChannel>(dataModel, R.menu.feeds_context);
    mFeeds.setMultiChoiceModeListener(actionModeCallback);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.feedsmanager, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent i;
    boolean result = false;
    switch (item.getItemId()) {
      case R.id.action_add:
        dataModel.addData();
        result = true;
        break;
      case R.id.action_settings:
        i = new Intent(this, PreferencesActivity.class);
        startActivity(i);
        result = true;
        break;
      default:
        result = super.onOptionsItemSelected(item);
    }
    return result;
  }

}
