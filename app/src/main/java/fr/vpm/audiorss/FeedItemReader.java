package fr.vpm.audiorss;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import fr.vpm.audiorss.http.DefaultNetworkChecker;
import fr.vpm.audiorss.media.AsyncPictureLoader;
import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.media.PictureLoadedListener;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class FeedItemReader extends Activity implements PictureLoadedListener {

  public static final String ITEM = "item";

  public static final String CHANNEL = "channel";

  private RSSItem item = null;

  private RSSChannel channel = null;

  private ImageView channelPic;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feed);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    Intent i = getIntent();
    if (i.hasExtra(ITEM)) {
      item = (RSSItem) i.getExtras().get(ITEM);
    }
    if (i.hasExtra(CHANNEL)) {
      channel = i.getExtras().getParcelable(CHANNEL);
    }


    TextView title = (TextView) findViewById(R.id.title);

    channelPic = (ImageView) findViewById(R.id.channelPic);

    TextView channelTitle = (TextView) findViewById(R.id.channelTitle);

    TextView date = (TextView) findViewById(R.id.date);

    TextView description = (TextView) findViewById(R.id.description);

    if (item != null) {
      setTitle(item.getTitle());
      title.setText(item.getTitle());

      SimpleDateFormat datePrinter = new SimpleDateFormat(RSSChannel.DISPLAY_PATTERN);
      SimpleDateFormat dateParser = new SimpleDateFormat(RSSChannel.DB_DATE_PATTERN, Locale.US);
      try {
        date.setText(datePrinter.format(dateParser.parse(item.getDate())));
      } catch (ParseException e) {
        Log.d("date", "Could not parse date " + item.getDate());
        date.setText(item.getDate());
      }
      description.setText(Html.fromHtml(item.getDescription()));
    }
    if (channel != null) {
      channelTitle.setText(channel.getTitle());
      if (channel.getBitmap() != null){
        channelPic.setImageBitmap(channel.getBitmap());
      } else if ((channel.getImage() != null) && (channel.getImage().getInetUrl() != null) && (new
          DefaultNetworkChecker().checkNetwork(this))) {
        List<PictureLoadedListener> listeners = new ArrayList<PictureLoadedListener>();
        listeners.add(this);
        listeners.add(channel);
        int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150,
            getResources().getDisplayMetrics()));
        // shortcut: we expect 150 dp for the picture for the feed
        new AsyncPictureLoader(listeners, 2 * px, px).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
            channel.getImage().getInetUrl());
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.feeditem, menu);
    MenuItem item = menu.findItem(R.id.action_play);
    if (!isMediaDownloaded()) {
      item.setVisible(false);
    }
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
    Log.d("playLocal", item.getMedia().getDeviceUri());
    Log.d("playOnline", item.getMediaUrl());

    Intent mediaIntent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN,
        Intent.CATEGORY_APP_MUSIC);
    mediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
    // Intent mediaIntent = new Intent(Intent.ACTION_MAIN);
    // mediaIntent.addCategory(Intent.CATEGORY_APP_MUSIC);
    // mediaIntent.setType(AUDIO_TYPE);
    if (isMediaDownloaded()) {
      Log.d("playLocal", item.getMedia().getDeviceUri());
      mediaIntent.setData(Uri.parse(item.getMedia().getDeviceUri()));
      startActivity(Intent.createChooser(mediaIntent, null));
    } else {
      Toast.makeText(this, "Please download the podcast first.", Toast.LENGTH_SHORT);
    }
    /*
     * else { Log.d("playOnline", item.getMediaUrl()); File podcast = new
     * File(item.getMediaUrl()); mediaIntent.setData(Uri.fromFile(podcast)); }
     */

  }

  private boolean isMediaDownloaded() {
    Media media = item.getMedia();
    if (media == null) {
      return false;
    }
    return media.isDownloaded();
  }

  private void openWebsite() {
    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()));
    startActivity(i);
  }

  private void downloadMedia() {
    Log.d("Download", item.getMediaUrl());
    item.downloadMedia(this);
  }

  @Override
  public void onPictureLoaded(Bitmap pictureBitmap) {
    channelPic.setImageBitmap(pictureBitmap);
  }
}
