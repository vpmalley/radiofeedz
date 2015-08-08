package fr.vpm.audiorss.media;

import android.app.DownloadManager;
import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import fr.vpm.audiorss.R;

/**
 * Created by vince on 25/07/15.
 */
public class IconDisplay {

  private final boolean hasMedia;

  private final Media media;

  private final boolean isDiskCached;

  private File cachedFile;

  private static Picasso picasso;

  public IconDisplay() {
    hasMedia = false;
    media = null;
    isDiskCached = false;
  }

  public IconDisplay(Media media, boolean isDiskCached) {
    this.hasMedia = true;
    this.media = media;
    this.isDiskCached = isDiskCached;
  }

  private static Picasso getPicasso(Context context) {
    if (picasso == null) {
      picasso = Picasso.with(context);
    }
    return picasso;
  }

  public void loadInView(ImageView pictureView) {
    if (hasMedia) {
      if (isDiskCached && (cachedFile != null)) {
        getPicasso(pictureView.getContext()).load(cachedFile)
            .placeholder(R.drawable.ic_article)
            .error(R.drawable.ic_article)
            .into(pictureView);
      } else {
        getPicasso(pictureView.getContext()).load(media.getDistantUrl())
            .placeholder(R.drawable.ic_article)
            .error(R.drawable.ic_article)
            .into(pictureView);
      }
    } else {
      loadBackupInView(pictureView);
    }
  }

  public static void loadBackupInView(ImageView pictureView) {
    getPicasso(pictureView.getContext()).load(R.drawable.ic_article)
        .into(pictureView);
  }

  /**
   * If the icon is to be cached to disk, caching verification happens when this is triggered.
   * The File is also cached to memory for faster access
   * @param context the Android context
   */
  public void loadInDiskCache(final Context context) {
    boolean hasPicture = !media.getDistantUrl().isEmpty();
    if (hasPicture && isDiskCached) {
      if (media.mediaFileExists(context, Media.Folder.INTERNAL_FEEDS_PICS)) {
        cachedFile = media.getMediaFile(context, Media.Folder.INTERNAL_FEEDS_PICS, false);
      } else {
        media.download(context, DownloadManager.Request.VISIBILITY_HIDDEN,
            new MediaDownloadListener() {

              @Override
              public void onMediaDownloaded() {
                cachedFile = media.getMediaFile(context, Media.Folder.INTERNAL_FEEDS_PICS, false);
              }
            }, Media.Folder.INTERNAL_FEEDS_PICS);
      }
    }
  }
}
