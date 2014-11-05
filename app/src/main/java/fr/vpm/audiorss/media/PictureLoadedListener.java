package fr.vpm.audiorss.media;

import android.graphics.Bitmap;

/**
 * Created by vincent on 23/10/14.
 * <p/>
 * Listener to the end of the load of a picture
 */
public interface PictureLoadedListener {

  void onPictureLoaded(Bitmap pictureBitmap);
}
