package fr.vpm.audiorss;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class FeedItemReader extends Fragment implements PictureLoadedListener {

  public static final String ITEM = "rssItem";

  public static final String CHANNEL = "channel";

  private RSSItem rssItem = null;

  private RSSChannel channel = null;

  private ImageView picView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    View readerView = inflater.inflate(R.layout.fragment_feed, container, false);
    setHasOptionsMenu(true);

    Bundle args = getArguments();
    if (args != null) {
      if (args.containsKey(ITEM)) {
        rssItem = args.getParcelable(ITEM);
      }
      if (args.containsKey(CHANNEL)) {
        channel = args.getParcelable(CHANNEL);
      }
    }

    TextView title = (TextView) readerView.findViewById(R.id.title);

    picView = (ImageView) readerView.findViewById(R.id.channelPic);

    TextView channelTitle = (TextView) readerView.findViewById(R.id.channelTitle);

    TextView date = (TextView) readerView.findViewById(R.id.date);

    TextView description = (TextView) readerView.findViewById(R.id.description);

    if (rssItem != null) {
      //getActivity().setTitle(rssItem.getTitle());
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
      description.setMovementMethod(LinkMovementMethod.getInstance());
    }
    if (channel != null) {
      channelTitle.setText(channel.getTitle());
    }
    setPicture();

    return readerView;
  }

  private void setPicture(){
    List<PictureLoadedListener> listeners = new ArrayList<PictureLoadedListener>();
    listeners.add(this);

    if ((rssItem != null) && (rssItem.getMedia().isPicture())){
      Bitmap itemBitmap = rssItem.getMedia().getAsBitmap(getActivity(), listeners, Media.Folder.INTERNAL_ITEMS_PICS);
      if (itemBitmap != null){
        picView.setImageBitmap(itemBitmap);
      }
    } else if (channel != null){
      Bitmap channelBitmap = channel.getBitmap(getActivity(), listeners);
      if (channelBitmap != null){
        picView.setImageBitmap(channelBitmap);
      }
    }
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if ((isVisibleToUser) && (rssItem != null) && (!rssItem.isRead())) {
      rssItem.setRead(true);
      new AsyncDbSaveRSSItem(new AsyncCallbackListener<List<RSSItem>>() {
        @Override
        public void onPreExecute() {
          // do nothing
        }

        @Override
        public void onPostExecute(List<RSSItem> result) {
          // do nothing
        }
      },
              getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rssItem);
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.feeditem, menu);
    if ((rssItem.getMedia() != null) && (rssItem.getMedia().getMediaFile(getActivity(), Media.Folder.EXTERNAL_DOWNLOADS_PODCASTS).exists())) {
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
    addShareItem(shareItem);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  private void addShareItem(MenuItem shareItem) {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_TEXT, this.rssItem.getLink());
    shareIntent.setType("text/plain");
    ShareActionProvider shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
    shareActionProvider.setShareIntent(shareIntent);
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
        deleteMediaFile(rssItem);
        result = true;
        break;
      case R.id.action_archive:
        archiveItem(rssItem);
        result = true;
        break;
      default:
        result = super.onOptionsItemSelected(item);
    }
    return result;
  }

  /**
   * Deletes the file associated with the media
   * @param rssItem
   */
  private void deleteMediaFile(RSSItem rssItem) {
    if ((rssItem.getMedia() != null) && (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))){
      File mediaFile = rssItem.getMedia().getMediaFile(getActivity(), Media.Folder.EXTERNAL_DOWNLOADS_PODCASTS);
      if (mediaFile.exists()){
        mediaFile.delete();
      }
      rssItem.getMedia().isPodcastDownloaded(getActivity(), true);
    }
  }

  private void playMedia() {
    Intent playIntent = new Intent(Intent.ACTION_VIEW);
    Media m = this.rssItem.getMedia();
    if (m != null) {
      File mediaFile = m.getMediaFile(getActivity(), Media.Folder.EXTERNAL_DOWNLOADS_PODCASTS);
      if (mediaFile.exists()){
        playIntent.setDataAndType(Uri.fromFile(mediaFile), m.getMimeType());
      } else {
        playIntent.setDataAndType(new Uri.Builder().path(m.getInetUrl()).build(), m.getMimeType());
      }
      startActivity(playIntent);
    }
  }

  private void archiveItem(RSSItem rssItem) {
    rssItem.setArchived(true);
    new AsyncDbSaveRSSItem(new AsyncCallbackListener<List<RSSItem>>() {
      @Override
      public void onPreExecute() {
        // do nothing
      }

      @Override
      public void onPostExecute(List<RSSItem> result) {
        getActivity().finish();
      }
    }, getActivity()).executeOnExecutor(AsyncTask
            .THREAD_POOL_EXECUTOR, rssItem);
  }

  private void openWebsite() {
    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(rssItem.getLink()));
    startActivity(i);
  }

  private void downloadMedia() {
    Log.d("Download", rssItem.getMediaUrl());
    rssItem.downloadMedia(getActivity());
  }

  @Override
  public void onPictureLoaded(Bitmap pictureBitmap) {
    picView.setImageBitmap(pictureBitmap);
  }
}
