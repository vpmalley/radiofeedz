package fr.vpm.audiorss.media;

import android.content.Context;

import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 05/04/16.
 */
public class MediaDownloadManager {

    private Context context;

    public MediaDownloadManager(Context context) {
        this.context = context;
    }

    public void downloadMedia(RSSItem rssItem) {
        if (rssItem.getMedia() != null) {
            rssItem.downloadMedia(context, new MediaDownloadListener.DummyMediaDownloadListener());
        }
    }

    public void deleteMedia(RSSItem rssItem) {
        if (rssItem.getMedia() != null) {
            rssItem.getMedia().getMediaFile(context).delete();
        }
    }

}
