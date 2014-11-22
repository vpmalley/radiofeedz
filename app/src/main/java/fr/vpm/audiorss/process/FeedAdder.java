package fr.vpm.audiorss.process;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.R;
import fr.vpm.audiorss.http.AsyncFeedRefresh;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.http.SaveFeedCallback;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 29/10/14.
 */
public class FeedAdder {

  private final DataModel dataModel;

  private final ProgressListener progressListener;

  private final NetworkChecker networkChecker;

  public FeedAdder(DataModel dataModel, NetworkChecker networkChecker,
                   ProgressListener progressListener) {
    this.progressListener = progressListener;
    this.dataModel = dataModel;
    this.networkChecker = networkChecker;
  }


  public String retrieveFeedFromClipboard() {
    String resultUrl = null;
    ClipData data = ((ClipboardManager) dataModel.getContext().getSystemService(Context.CLIPBOARD_SERVICE)).getPrimaryClip();
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

  public void askForFeedValidation(final List<RSSChannel> channels, final String url) {
    AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(dataModel.getContext());
    confirmationBuilder.setTitle(R.string.add_feed_clipboard);
    confirmationBuilder.setMessage(getResourceString(R.string.ask_add_feed) + " " + url + " ?");
    confirmationBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        addFeed(channels, url);
      }

    });
    confirmationBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {

      }
    });
    confirmationBuilder.show();
  }

  private String getResourceString(int resId) {
    return dataModel.getContext().getResources().getString(resId);
  }

  public void tellToCopy() {
    AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(dataModel.getContext());
    confirmationBuilder.setTitle(R.string.add_feed_clipboard);
    confirmationBuilder
        .setMessage(R.string.please_clip_feed_url);
    confirmationBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
      }

    });
    confirmationBuilder.show();
  }

  private void addFeed(List<RSSChannel> channels, final String url) {
    boolean exists = false;
    for (RSSChannel channel : channels) {
      if (channel.getUrl().equals(url)) {
        exists = true;
      }
    }
    if (exists) {
      Toast.makeText(dataModel.getContext(), R.string.cannot_add_feed, Toast.LENGTH_SHORT).show();
    } else if (networkChecker.checkNetworkForRefresh(dataModel.getContext(), true)) {
      SaveFeedCallback callback = new SaveFeedCallback(progressListener, dataModel);
      new AsyncFeedRefresh(dataModel.getContext(), callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }
  }
}
