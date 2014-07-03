package fr.vpm.audiorss;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import fr.vpm.audiorss.db.DbRSSChannel;
import fr.vpm.audiorss.http.AsyncFeedRefresh;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class FeedsActivity extends Activity {

  private static final String PREF_FEED_ORDERING = "pref_feed_ordering";

  public static final String E_RETRIEVING_FEEDS = "Issue retrieving the feeds. Please retry.";

  public static final String E_ADDING_FEED = "Issue adding the feed. Please retry.";

  public static final String E_DUPLICATE_FEED = "This feed already exists in your list.";

  public static final String E_NOT_CONNECTED = "This device does not appear connected to the Internet.";

  List<RSSChannel> channels = new ArrayList<RSSChannel>();

  ListView mFeeds;

  ImageButton mRefreshButton;

  ImageButton mAddButton;

  ImageButton mTestButton;
  
  ProgressBar mRefreshProgress;

  RSSItem[] items;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feeds);

    mRefreshProgress = (ProgressBar) findViewById(R.id.refreshprogress);
    
    refreshView();
  }

  public void refreshView() {
    Log.d("FeedsActivity", "refreshing view");
    channels = loadAllFromDB();
    SortedSet<RSSItem> allItems = new TreeSet<RSSItem>(
        new Comparator<RSSItem>() {
          @Override
          public int compare(RSSItem lhs, RSSItem rhs) {

            SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(FeedsActivity.this);
            String ordering = sharedPref.getString(PREF_FEED_ORDERING,
                "reverse_time");

            int comparison = 0;
            Date lhsDate = null;
            Date rhsDate = null;
            try {
              lhsDate = new SimpleDateFormat(RSSChannel.DATE_PATTERN, Locale.US).parse(lhs
                  .getDate());
              rhsDate = new SimpleDateFormat(RSSChannel.DATE_PATTERN, Locale.US).parse(rhs
                  .getDate());
            } catch (ParseException e) {
              Log.e("Exception", e.toString());
            }

            // int comparisonByDate = lhs.getDate().compareTo(rhs.getDate());
            int comparisonByDate = 0;
            if ((lhsDate != null) && (rhsDate != null)) {
              comparisonByDate = lhsDate.compareTo(rhsDate);
            }
            int comparisonByName = lhs.getTitle().compareTo(rhs.getTitle());

            if (ordering.contains("alpha")) {
              comparison = comparisonByName;
            } else {
              comparison = comparisonByDate;
            }

            int factor = 1;
            if (ordering.contains("reverse")) {
              factor = -1;
            }

            if (comparison == 0) {
              comparison = comparisonByName + comparisonByDate;
            }

            return factor * comparison;
          }
        });
    for (RSSChannel channel : channels) {
      allItems.addAll(channel.getItems());
    }

    items = allItems.toArray(new RSSItem[allItems.size()]);
    ArrayAdapter<RSSItem> itemAdapter = new ArrayAdapter<RSSItem>(this,
        R.layout.activity_item, items);
    // fill ListView with all the items
    if (mFeeds == null) {
      mFeeds = (ListView) findViewById(R.id.list);
      mFeeds.setTextFilterEnabled(true);
      mFeeds.setOnItemClickListener(new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
            long arg3) {
          Intent i = new Intent(FeedsActivity.this, FeedItemActivity.class);
          i.putExtra(FeedItemActivity.ITEM, items[position]);
          //i.putExtra(FeedItemActivity.CHANNEL, null); // TODO switch to a channel ASAP
          startActivity(i);

        }

      });
    }
    mFeeds.setAdapter(itemAdapter);

  }

  public void startRefreshProgress(){
    //mRefreshProgress.setIndeterminate(true);
    mRefreshProgress.setVisibility(View.VISIBLE);
  }
  
  public void updateProgress(int progress){
    mRefreshProgress.setProgress(progress);
  }
  
  public void stopRefreshProgress(){
    //mRefreshProgress.setIndeterminate(false);
    mRefreshProgress.setVisibility(View.GONE);
  }
  
  private List<RSSChannel> loadAllFromDB() {
    // return RSSChannel.allChannels;
    List<RSSChannel> channels = new ArrayList<RSSChannel>();
    try {
      DbRSSChannel dbReader = new DbRSSChannel(this);
      channels = dbReader.readAll();
      dbReader.closeDb();
    } catch (ParseException e) {
      displayError(E_RETRIEVING_FEEDS);
    }
    Log.d("retrieving feeds", channels.size() + " channels");
    return channels;
  }

  /**
   * Displays error, as a Toast
   * 
   * @param error
   */
  public void displayError(String error) {
    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
  }

  // launch when click on refresh button
  private void launchFeedRefresh() {
    Log.d("FeedsActivity", "launching feed refresh");
    for (RSSChannel channel : channels) {
      if (checkNetwork()) {
        new AsyncFeedRefresh(FeedsActivity.this).execute(channel.getUrl());
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.welcome, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent i = null;
    boolean result = false;
    switch (item.getItemId()) {
    case R.id.action_search:
      i = new Intent(FeedsActivity.this, SearchFeedActivity.class);
      startActivity(i);
      result = true;
      break;
    case R.id.action_add:
      String feedUrl = retrieveFeedFromClipboard();
      if (feedUrl != null) {
        askForFeedValidation(feedUrl);
      } else {
        tellToCopy();
      }
      result = true;
      break;
    case R.id.action_refresh:
      launchFeedRefresh();
      result = true;
      break;
    case R.id.action_settings:
      i = new Intent(FeedsActivity.this, PreferencesActivity.class);
      startActivity(i);
      result = true;
      break;
    default:
      result = super.onOptionsItemSelected(item);
    }
    return result;
  }

  private String retrieveFeedFromClipboard() {
    String resultUrl = null;
    ClipData data = ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
        .getPrimaryClip();
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

  private void askForFeedValidation(final String url) {
    AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(this);
    confirmationBuilder.setTitle(R.string.add_feed_clipboard);
    confirmationBuilder.setMessage("Do you want to add the feed located at "
        + url + " ?");
    confirmationBuilder.setPositiveButton("Yes",
        new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            addFeed(url);
          }

        });
    confirmationBuilder.setNegativeButton("No", new OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {

      }
    });
    confirmationBuilder.show();
  }

  private void tellToCopy() {
    AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(this);
    confirmationBuilder.setTitle(R.string.add_feed_clipboard);
    confirmationBuilder
        .setMessage("You can add a feed by copying it to the clipboard. Then press this button.");
    confirmationBuilder.setPositiveButton("Yes",
        new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
          }

        });
    confirmationBuilder.show();
  }

  private void addFeed(final String url) {
    boolean exists = false;
    for (RSSChannel channel : channels) {
      if (channel.getUrl().equals(url)) {
        exists = true;
      }
    }
    if (exists) {
      displayError(E_DUPLICATE_FEED);
    } else if (checkNetwork()) {
      new AsyncFeedRefresh(FeedsActivity.this).execute(url);
    }
  }

  private boolean checkNetwork() {
    boolean isConnected = false;
    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      isConnected = true;
    } else {
      displayError(E_NOT_CONNECTED);
    }
    return isConnected;
  }
}
