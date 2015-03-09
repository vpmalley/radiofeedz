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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import fr.vpm.audiorss.db.AsyncDbSaveRSSItem;
import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.media.MediaDownloadListener;
import fr.vpm.audiorss.media.PictureLoadedListener;
import fr.vpm.audiorss.persistence.AsyncMaintenance;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DateUtils;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

public class FeedItemReader extends Fragment implements PictureLoadedListener, MediaDownloadListener {

  public static final String ITEM = "rssItem";

  public static final String CHANNEL = "channel";
  public static final String MIME_IMAGE = "image";

  private RSSItem rssItem = null;

  private RSSChannel channel = null;

  private ImageView picView;
  private MenuItem downloadItem;
  private MenuItem playItem;
  private MenuItem displayItem;

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

    WebView description = (WebView) readerView.findViewById(R.id.description);

    if (rssItem != null) {
      title.setText(rssItem.getTitle());

      SimpleDateFormat datePrinter = new SimpleDateFormat(DateUtils.DISPLAY_PATTERN, Locale.getDefault());
      date.setText(datePrinter.format(DateUtils.parseDBDate(rssItem.getDate(), Calendar.getInstance().getTime())));
      String style = "<style type=\"text/css\">body{color: #434343; font-family: sans-serif-light; text-align: justify;}</style>";
      description.loadDataWithBaseURL(null, style + rssItem.getDescription(), "text/html", "utf-8", null);
      description.setBackgroundColor(0x00000000);

      // check the media exists
      if (rssItem.getMedia() != null) {
        rssItem.getMedia().isDownloaded(getActivity(), true);
      }
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
    if ((rssItem != null) && (!rssItem.isRead())) {
      if (isVisibleToUser) {
        markAsRead();
      }
    }
    super.setUserVisibleHint(isVisibleToUser);
  }

  private void markAsRead() {
    rssItem.setRead(true);
    new AsyncDbSaveRSSItem(new AsyncCallbackListener.DummyCallback<List<RSSItem>>(),
            getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rssItem);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.feeditem, menu);
    if (rssItem == null) {
      super.onCreateOptionsMenu(menu, inflater);
      return;
    }

    displayItem = menu.findItem(R.id.action_display);
    playItem = menu.findItem(R.id.action_play);
    downloadItem = menu.findItem(R.id.action_download);

    updateActionMediaItems();
    MenuItem shareItem = menu.findItem(R.id.action_share);
    addShareItem(shareItem);
    super.onCreateOptionsMenu(menu, inflater);
  }

  /**
   * Updates the visibility of the media item in the action bar
   */
  private void updateActionMediaItems() {
    displayItem.setVisible(false);
    playItem.setVisible(false);
    downloadItem.setVisible(false);

    if (rssItem.getMedia() != null){
      File picFile = rssItem.getMedia().getMediaFile(getActivity(), Media.Folder.EXTERNAL_DOWNLOADS_PICTURES);
      File podcastFile = rssItem.getMedia().getMediaFile(getActivity(), Media.Folder.EXTERNAL_DOWNLOADS_PODCASTS);
      if ((picFile != null) && (picFile.exists())){
        displayItem.setVisible(true);
      } else if ((podcastFile != null) && (podcastFile.exists())){
        playItem.setVisible(true);
      } else if (!rssItem.getMedia().getInetUrl().isEmpty()) {
        downloadItem.setVisible(true);
      }
    }
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
      Media.Folder externalDownloadsFolder = Media.Folder.EXTERNAL_DOWNLOADS_PODCASTS;
      if (rssItem.getMedia().getMimeType().startsWith(MIME_IMAGE)){
        externalDownloadsFolder = Media.Folder.EXTERNAL_DOWNLOADS_PICTURES;
      }
      File mediaFile = rssItem.getMedia().getMediaFile(getActivity(), externalDownloadsFolder);
      if (mediaFile.exists()){
        mediaFile.delete();
      }
      rssItem.getMedia().isDownloaded(getActivity(), true);
      updateActionMediaItems();
    }
  }

  private void playMedia() {
    Intent playIntent = new Intent(Intent.ACTION_VIEW);
    Media m = this.rssItem.getMedia();
    if (m != null) {
      Media.Folder externalDownloadsFolder = Media.Folder.EXTERNAL_DOWNLOADS_PODCASTS;
      if (m.getMimeType().startsWith(MIME_IMAGE)){
        externalDownloadsFolder = Media.Folder.EXTERNAL_DOWNLOADS_PICTURES;
      }
      File mediaFile = m.getMediaFile(getActivity(), externalDownloadsFolder);
      if (mediaFile.exists()){
        playIntent.setDataAndType(Uri.fromFile(mediaFile), m.getMimeType());
      } else {
        playIntent.setDataAndType(new Uri.Builder().path(m.getInetUrl()).build(), m.getMimeType());
      }
      startActivity(playIntent);
      // take advantage of media playing to do some maintenance
      new AsyncMaintenance(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Media.getInternalItemsPicsFolder(), Media.getInternalFeedsPicsFolder());
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
        if (FeedItemReader.this.isVisible()) {
          getActivity().finish();
        }
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
    rssItem.downloadMedia(getActivity(), new DummyMediaDownloadListener());
  }

  @Override
  public void onPictureLoaded(Bitmap pictureBitmap) {
    picView.setImageBitmap(pictureBitmap);
  }

  @Override
  public void onMediaDownloaded() {
    if (isVisible()) {
      updateActionMediaItems();
    }
  }
}
