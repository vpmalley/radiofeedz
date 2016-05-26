package fr.vpm.audiorss;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import fr.vpm.audiorss.adapter.FeedContextualActionListener;
import fr.vpm.audiorss.adapter.FeedItemsListRecyclerListener;
import fr.vpm.audiorss.adapter.NavigationDrawerClickListener;
import fr.vpm.audiorss.adapter.NavigationDrawerItem;
import fr.vpm.audiorss.adapter.RSSItemArrayAdapter;
import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.interaction.FeedItemsInteraction;
import fr.vpm.audiorss.interaction.FeedItemsInteractor;
import fr.vpm.audiorss.maintenance.RecurrentTaskManager;
import fr.vpm.audiorss.process.Stats;

public class AllFeedItems extends AppCompatActivity implements FeedsActivity, SwipeRefreshLayout.OnRefreshListener {

  private static final int REQ_PREFS = 2;
  private static final int REQ_CATALOG = 3;

  private AbsListView mFeedItems;

  private NetworkChecker networkChecker;

  private ActionBarDrawerToggle drawerToggle;

  private String title = "Radiofeedz";
  private FeedItemsInteraction interactor;
  private ProgressBarListener progressBarListener;
  private SwipeRefreshLayout itemsRefresher;
  private ShowcaseView showcaseView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // list layouts
    int activity_layout = R.layout.activity_feeds_list;
    int rss_item_layout = R.layout.list_rss_item;

    setContentView(activity_layout);

    progressBarListener = new ProgressBarListener((ProgressBar) findViewById(R.id.refreshprogress));

    // services
    networkChecker = new DefaultNetworkChecker();
    interactor = new FeedItemsInteractor(this, rss_item_layout);

    mFeedItems = (AbsListView) findViewById(R.id.allitems);
    mFeedItems.setTextFilterEnabled(true);
    setEmptyView();

    FloatingActionButton catalogFab = (FloatingActionButton) findViewById(R.id.fab);
    catalogFab.setOnClickListener(new OnCatalogFabClickListener());

    itemsRefresher = (SwipeRefreshLayout) findViewById(R.id.itemsrefresher);
    itemsRefresher.setOnRefreshListener(this);
    itemsRefresher.setColorSchemeResources(R.color.holo_blue_light_comp);

    setNavigationDrawer();

    interactor.loadFeedItems();

    Intent i = getIntent();
    if (i.hasExtra(FeedAddingActivity.CHANNEL_NEW_URL)) {
      progressBarListener.startRefreshProgress();
      interactor.addFeed(i.getStringExtra(FeedAddingActivity.CHANNEL_NEW_URL));
    }

    setupSnackBarWithClipboardContent();

    new RecurrentTaskManager().performRecurrentTasks(this);
  }

  private void setupSnackBarWithClipboardContent() {
    final String feedUrl = retrieveFeedUrlFromClipboard();
    if (feedUrl != null) {
      Snackbar
          .make(mFeedItems, R.string.ask_add_feed + feedUrl, Snackbar.LENGTH_LONG)
          .setAction(R.string.action_add, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              interactor.addFeed(feedUrl);
            }
          }).show();
    }
  }

  public String retrieveFeedUrlFromClipboard() {
    String resultUrl = null;
    ClipData data = ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).getPrimaryClip();
    if (data != null) {
      ClipData.Item cbItem = data.getItemAt(0);
      if (cbItem != null) {
        String url = cbItem.getText().toString();
        if ((url != null) && (url.startsWith("http"))) {
          resultUrl = url;
        }
      }
    }
    return resultUrl;
  }

  private void setEmptyView() {
    LinearLayout emptyView = (LinearLayout) findViewById(R.id.emptyView);
    mFeedItems.setEmptyView(emptyView);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    drawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    drawerToggle.onConfigurationChanged(newConfig);
  }

  /**
   * Sets the navigation drawer and related elements
   */
  private void setNavigationDrawer() {
    final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    ListView feedsList = (ListView) findViewById(R.id.left_drawer);
    feedsList.setOnItemClickListener(new NavigationDrawerClickListener(this, drawerLayout, feedsList, interactor));

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    drawerToggle = new ActionBarDrawerToggle(
        this,
        drawerLayout,
        R.string.drawer_open,
        R.string.drawer_close
    ) {
      public void onDrawerClosed(View view) {
        getSupportActionBar().setTitle(title);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }

      public void onDrawerOpened(View drawerView) {
        hideShowcase();
        getSupportActionBar().setTitle(R.string.drawer_opened_title);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }
    };
    drawerLayout.setDrawerListener(drawerToggle);
    setFeedsContextualListener(feedsList);
  }

  @Override
  public void onRefresh() {
    Stats.get(this).increment(Stats.ACTION_REFRESH);
    if (networkChecker.checkNetworkForRefresh(this, true)) {
      interactor.retrieveLatestFeedItems();
    } else {
      itemsRefresher.setRefreshing(false);
    }
  }

  @Override
  public void refreshTitle(String title) {
    this.title = title;
  }

  @Override
  public void startRefreshProgress() {
    progressBarListener.startRefreshProgress();
  }

  @Override
  public void stopRefreshProgress() {
    progressBarListener.stopRefreshProgress();
    if (itemsRefresher.isRefreshing()) {
      itemsRefresher.setRefreshing(false);
    }
  }

  @Override
  public void resetFeedItemsListSelection() {
    mFeedItems.setSelection(0);
  }

  @Override
  public void refreshNavigationDrawer(ArrayAdapter<NavigationDrawerItem> feedAdapter) {
    ListView feedsList = (ListView) findViewById(R.id.left_drawer);
    feedsList.setAdapter(feedAdapter);
    setFeedsContextualListener(feedsList);
  }

  public void setFeedsContextualListener(ListView feedsList) {
    feedsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
    FeedContextualActionListener drawerModeCallback = new FeedContextualActionListener(interactor, (ArrayAdapter<NavigationDrawerItem>) feedsList.getAdapter(), R.menu.feeds_context);
    feedsList.setMultiChoiceModeListener(drawerModeCallback);
  }

  @Override
  public void refreshFeedItems(RSSItemArrayAdapter rssItemAdapter) {
    FeedItemsListRecyclerListener recyclerListener = new FeedItemsListRecyclerListener(interactor);
    mFeedItems.setRecyclerListener(recyclerListener);
    rssItemAdapter.setRecyclerListener(recyclerListener);
    mFeedItems.setAdapter(rssItemAdapter);
  }

  public void displayShowcase() {
    if (showcaseView == null) {
      showcaseView = new ShowcaseView.Builder(this)
          .setStyle(R.style.CustomShowcaseTheme)
          .setTarget(new ViewTarget(R.id.fab, this))
          .setContentText(getResources().getText(R.string.no_feed))
          .hideOnTouchOutside()
          .build();
      showcaseView.hideButton();
      showcaseView.show();
    }
  }

  @Override
  public void hideShowcase() {
    if (showcaseView != null) {
      showcaseView.hide();
      showcaseView = null;
    }
  }

  @Override
  public void displayLastRefreshTime(String lastRefreshTime) {
    if (getSupportActionBar() != null) {
      getSupportActionBar().setSubtitle(getString(R.string.last_refresh) + " : " + lastRefreshTime);
    }
  }

  @Override
  public Context getContext() {
    return this;
  }

  @Override
  public void displayFeedRetrieveError() {
    Toast.makeText(this, R.string.cannot_retrieve_feed, Toast.LENGTH_LONG).show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.allfeedsitem, menu);
    MenuItem searchItem = menu.findItem(R.id.action_search);
    searchItem.setVisible(false);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent i;
    boolean result = false;
    if (drawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    switch (item.getItemId()) {
      case R.id.action_refresh:
        Stats.get(this).increment(Stats.ACTION_REFRESH);
        if (networkChecker.checkNetworkForRefresh(this, true)) {
          progressBarListener.startRefreshProgress();
          interactor.retrieveLatestFeedItems();
        }
        result = true;
        break;
      case R.id.action_settings:
        Stats.get(this).increment(Stats.ACTION_SETTINGS);
        i = new Intent(AllFeedItems.this, AllPreferencesActivity.class);
        startActivityForResult(i, REQ_PREFS);
        hideShowcase();
        result = true;
        break;
      default:
        result = super.onOptionsItemSelected(item);
    }
    return result;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (REQ_PREFS == requestCode){
      progressBarListener.startRefreshProgress();
      interactor.loadFeedItems();
    } else if (REQ_CATALOG == requestCode) {
      if (data != null) {
        progressBarListener.startRefreshProgress();
        String feedUrl = data.getStringExtra(CatalogActivity.FEED_URL_EXTRA);
        interactor.addFeed(feedUrl);
      }
    }
  }

  private class OnCatalogFabClickListener implements View.OnClickListener {
    @Override
    public void onClick(View view) {
      Stats.get(AllFeedItems.this).increment(Stats.ACTION_CATALOG);
      Intent i = new Intent(AllFeedItems.this, CatalogActivity.class);
      startActivityForResult(i, REQ_CATALOG);
    }
  }
}
