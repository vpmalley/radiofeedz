package fr.vpm.audiorss.process;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.R;
import fr.vpm.audiorss.http.AsyncFeedRefresh;
import fr.vpm.audiorss.http.NetworkChecker;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 29/10/14.
 */
public class FeedAdder {

    public static final String E_ADDING_FEED = "Issue adding the feed. Please retry.";

    private final FeedsActivity activity;

    private final NetworkChecker networkChecker;

    public FeedAdder(FeedsActivity activity, NetworkChecker networkChecker) {
        this.activity = activity;
        this.networkChecker = networkChecker;
    }


    public String retrieveFeedFromClipboard() {
        String resultUrl = null;
        ClipData data = ((ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE)).getPrimaryClip();
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
        AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(activity);
        confirmationBuilder.setTitle(R.string.add_feed_clipboard);
        confirmationBuilder.setMessage("Do you want to add the feed located at " + url + " ?");
        confirmationBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                addFeed(channels, url);
            }

        });
        confirmationBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        confirmationBuilder.show();
    }

    public void tellToCopy() {
        AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(activity);
        confirmationBuilder.setTitle(R.string.add_feed_clipboard);
        confirmationBuilder
                .setMessage("You can add a feed by copying it to the clipboard. Then press this button.");
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
            Toast.makeText(activity, E_ADDING_FEED, Toast.LENGTH_SHORT).show();
        } else if (networkChecker.checkNetwork(activity)) {
            new AsyncFeedRefresh(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        }
    }
}
