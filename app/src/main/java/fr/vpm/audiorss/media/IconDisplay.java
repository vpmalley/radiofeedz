package fr.vpm.audiorss.media;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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

  public void loadInView(ImageView pictureView) {
    if (hasMedia) {
      Glide.with(pictureView.getContext()).load(media.getDistantUrl())
          .placeholder(R.drawable.ic_article)
          .error(R.drawable.ic_article)
          .diskCacheStrategy(DiskCacheStrategy.RESULT)
          .into(pictureView);
    } else {
      loadBackupInView(pictureView);
    }
  }

  public static void loadBackupInView(ImageView pictureView) {
    Glide.with(pictureView.getContext()).load(R.drawable.ic_article)
        .into(pictureView);
  }
}
