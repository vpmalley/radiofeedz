package fr.vpm.audiorss;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.vpm.audiorss.db.AsyncDbSaveRSSItem;
import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.media.PictureLoadedListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class FeedItemReader extends Activity implements PictureLoadedListener {

  public static final String ITEM = "rssItem";

  public static final String CHANNEL = "channel";

  private RSSItem rssItem = null;

  private RSSChannel channel = null;

  private ImageView channelPic;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feed);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    Intent i = getIntent();
    if (i.hasExtra(ITEM)) {
      rssItem = (RSSItem) i.getExtras().get(ITEM);
    }
    if (i.hasExtra(CHANNEL)) {
      channel = i.getExtras().getParcelable(CHANNEL);
    }


    TextView title = (TextView) findViewById(R.id.title);

    channelPic = (ImageView) findViewById(R.id.channelPic);

    TextView channelTitle = (TextView) findViewById(R.id.channelTitle);

    TextView date = (TextView) findViewById(R.id.date);

    TextView description = (TextView) findViewById(R.id.description);

    if (rssItem != null) {
      setTitle(rssItem.getTitle());
      title.setText(rssItem.getTitle());

      SimpleDateFormat datePrinter = new SimpleDateFormat(RSSChannel.DISPLAY_PATTERN);
      SimpleDateFormat dateParser = new SimpleDateFormat(RSSChannel.DB_DATE_PATTERN, Locale.US);
      try {
        date.setText(datePrinter.format(dateParser.parse(rssItem.getDate())));
      } catch (ParseException e) {
        Log.d("date", "Could not parse date " + rssItem.getDate());
        date.setText(rssItem.getDate());
      }
      description.setText(Html.fromHtml(rssItem.getDescription()));
    }
    if (channel != null) {
      channelTitle.setText(channel.getTitle());
      List<PictureLoadedListener> listeners = new ArrayList<PictureLoadedListener>();
      listeners.add(this);
      if (channel.getBitmap(this, listeners) != null){
        channelPic.setImageBitmap(channel.getBitmap(this, null));
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.feeditem, menu);
    if ((rssItem.getMedia() != null) && (rssItem.getMedia().getMediaFile(this).exists())) {
      if (rssItem.getMedia().getMimeType().contains("image")) {
        MenuItem displayItem = menu.findItem(R.id.action_display);
        displayItem.setVisible(true);
      } else {
        MenuItem playItem = menu.findItem(R.id.action_play);
        playItem.setVisible(true);
      }
      MenuItem downloadItem = menu.findItem(R.id.action_download);
      downloadItem.setVisible(false);
    }
    MenuItem shareItem = menu.findItem(R.id.action_share);
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_TEXT, this.rssItem.getLink());
    shareIntent.setType("text/plain");
    ShareActionProvider shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
    shareActionProvider.setShareIntent(shareIntent);
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
      case R.id.action_display:
      case R.id.action_play:
        playMedia();
        result = true;
        break;
      case R.id.action_delete:
        deleteItem(rssItem);
        result = true;
        break;
      default:
        result = super.onOptionsItemSelected(item);
    }
    return result;
  }

  private void playMedia() {
    Intent playIntent = new Intent(Intent.ACTION_VIEW);
    Media m = this.rssItem.getMedia();
    if (m != null) {
      File mediaFile = m.getMediaFile(this);
      if (mediaFile.exists()){
        playIntent.setDataAndType(Uri.fromFile(mediaFile), m.getMimeType());
      } else {
        playIntent.setDataAndType(new Uri.Builder().path(m.getInetUrl()).build(), m.getMimeType());
      }
      startActivity(playIntent);
    }
  }

  private void deleteItem(RSSItem rssItem) {
    rssItem.setDeleted(true);
    new AsyncDbSaveRSSItem(new AsyncCallbackListener<List<RSSItem>>() {
      @Override
      public void onPreExecute() {
        // do nothing
      }

      @Override
      public void onPostExecute(List<RSSItem> result) {
        finish();
      }
    }, this).executeOnExecutor(AsyncTask
        .THREAD_POOL_EXECUTOR, rssItem);
  }

  private boolean isMediaDownloaded() {
    Media media = rssItem.getMedia();
    if (media == null) {
      return false;
    }
    return media.isDownloaded();
  }

  private void openWebsite() {
    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(rssItem.getLink()));
    startActivity(i);
  }

  private void downloadMedia() {
    Log.d("Download", rssItem.getMediaUrl());
    rssItem.downloadMedia(this);
  }

  @Override
  public void onPictureLoaded(Bitmap pictureBitmap) {
    channelPic.setImageBitmap(pictureBitmap);
  }
}
