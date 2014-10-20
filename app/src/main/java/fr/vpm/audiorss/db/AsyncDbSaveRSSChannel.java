package fr.vpm.audiorss.db;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.text.ParseException;

import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 20/10/14.
 */
public class AsyncDbSaveRSSChannel extends AsyncTask<RSSChannel, Integer, RSSChannel> {

    private final FeedsActivity activity;

    private final DbRSSChannel dbUpdater;

    public AsyncDbSaveRSSChannel(FeedsActivity feedsActivity) {
        this.activity = feedsActivity;
        this.dbUpdater = new DbRSSChannel(feedsActivity);
    }

    @Override
    protected RSSChannel doInBackground(RSSChannel... rssChannels) {
        if (0 == rssChannels.length){
            return null;
        }
        RSSChannel persistedChannel = null;
        RSSChannel newChannel = rssChannels[0];
        Log.d("measures", "save start " + newChannel.getUrl() + String.valueOf(System.currentTimeMillis()));
        try {
            RSSChannel existingChannel = dbUpdater.readByUrl(newChannel.getUrl());
            if (existingChannel != null) {
                persistedChannel = dbUpdater.update(existingChannel, newChannel);
            } else { // new channel
                newChannel.downloadImage(activity);
                persistedChannel = dbUpdater.add(newChannel);
            }
        } catch (ParseException e){
            Log.e("DbIssue", "Could not add the feed to the DB");
        } finally {
            dbUpdater.closeDb();
        }
        Log.d("measures", "save -end- " + newChannel.getUrl() + String.valueOf(System.currentTimeMillis()));
        return persistedChannel;
    }

    @Override
    protected void onPostExecute(RSSChannel rssChannel) {
        activity.refreshView();
    }
}
