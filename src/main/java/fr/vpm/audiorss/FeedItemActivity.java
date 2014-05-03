package fr.vpm.audiorss;

import java.io.File;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import fr.vpm.audiorss.process.ItemParser;
import fr.vpm.audiorss.rss.RSSItem;

public class FeedItemActivity extends Activity {

  private static final String AUDIO_TYPE = "audio/*";

  public static final String ITEM = "item";

  RSSItem item = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feed);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    Intent i = getIntent();
    if (i.hasExtra(ITEM)) {
      item = (RSSItem) i.getExtras().get(ITEM);
    }

    TextView title = (TextView) findViewById(R.id.title);

    ImageView channelPic = (ImageView) findViewById(R.id.channelPic);

    TextView channelTitle = (TextView) findViewById(R.id.channelTitle);

    TextView date = (TextView) findViewById(R.id.date);

    TextView description = (TextView) findViewById(R.id.description);

    if (item != null) {
      setTitle(item.getTitle());
      title.setText(item.getTitle());
      channelPic.setImageURI(item.getChannelImage());

      channelTitle.setText(item.getChannelTitle());
      date.setText(item.getDate());
      description.setText(item.getDescription());
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.feeditem, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    boolean result = false;
    switch (item.getItemId()) {
    case R.id.action_download:
      downloadMedia();
      result = true;
      break;
    case R.id.action_web:
      openWebsite();
      result = true;
      break;
    case R.id.action_play:
      playMedia();
      result = true;
      break;
    default:
      result = super.onOptionsItemSelected(item);
    }
    return result;
  }

  private void playMedia() {
    Log.d("playLocal", item.getMediaUri().toString());
    Log.d("playOnline", item.getMediaUrl());
    Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
    mediaIntent.setType(AUDIO_TYPE);
    if (isMediaDownloaded()) {
      Log.d("playLocal", item.getMediaUri().toString());
      mediaIntent.setData(item.getMediaUri());
    } else {
      Log.d("playOnline", item.getMediaUrl());
      File podcast = new File(item.getMediaUrl());
      mediaIntent.setData(Uri.fromFile(podcast));
    }
    startActivity(Intent.createChooser(mediaIntent, null));
  }

  private boolean isMediaDownloaded() {
    if (item.getMediaUri() == null) {
      long fileId = item.getMediaId();
      Uri mediaUri = ItemParser.retrieveUri(fileId, this);
      if (mediaUri != null) {
        item.setMediaUri(mediaUri.toString());
      }
    }
    return item.getMediaUri() != null;
  }

  private void openWebsite() {
    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()));
    startActivity(i);
  }

  private void downloadMedia() {
    Log.d("Download", item.getMediaUrl());

    String fileExtension = item.getMediaUrl().substring(
        item.getMediaUrl().lastIndexOf('.'));

    String fileName = item.getLink().substring(
        item.getLink().lastIndexOf('/') + 1)
        + fileExtension;

    // retrieve download folder from the preferences
    SharedPreferences sharedPref = PreferenceManager
        .getDefaultSharedPreferences(FeedItemActivity.this);
    String downloadFolder = sharedPref.getString("pref_download_folder",
        Environment.DIRECTORY_PODCASTS);

    DownloadManager.Request r = new DownloadManager.Request(Uri.parse(item
        .getMediaUrl()));

    // This put the download in the same Download dir the browser uses
    r.setDestinationInExternalPublicDir(downloadFolder, fileName);

    // When downloading music and videos they will be listed in the player
    // (Seems to be available since Honeycomb only)
    r.allowScanningByMediaScanner();

    // Notify user when download is completed
    // (Seems to be available since Honeycomb only)
    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

    // Start download
    long fileId = downloadFile(r);
    item.setMediaId(fileId);

    BroadcastReceiver downloadFinished = new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d("BReceiver", "The download is over for some file.");
        long fileId = intent
            .getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        // query the status of the file
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(fileId);
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Cursor c = dm.query(query);
        c.moveToFirst();
        long id = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

        String mediaUri = "";
        String localUri = "";
        if (DownloadManager.STATUS_SUCCESSFUL == status) {
          mediaUri = c.getString(c
              .getColumnIndex(DownloadManager.COLUMN_MEDIAPROVIDER_URI));
          localUri = c.getString(c
              .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
        }
        Log.d("BReceiver", "The status for " + id + " is " + status
            + ". It is located at " + mediaUri + " / " + localUri);
      }
    };
    registerReceiver(downloadFinished, new IntentFilter(
        DownloadManager.ACTION_DOWNLOAD_COMPLETE));
  }

  private long downloadFile(DownloadManager.Request r) {
    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
    long fileId = dm.enqueue(r);
    return fileId;
  }

}
