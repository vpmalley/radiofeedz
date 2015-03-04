package fr.vpm.audiorss.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.persistence.FilePictureSaver;
import fr.vpm.audiorss.persistence.PictureSaver;

/**
 * Created by vincent on 22/10/14.
 * <p/>
 * An asynchronous task to download a picture. It calls a listener once loaded.
 */
public class AsyncPictureLoader extends AsyncTask<Media, Integer, List<Bitmap>> {

  private final List<PictureLoadedListener> pictureLoadedListeners;

  private final int reqWidth; // in pixels

  private final int reqHeight; // in pixels

  private final Context context;

  private Exception thrownException;

  /**
   * Whether the picture should be persisted in internal storage
   */
  private final Media.Folder folder;

  public AsyncPictureLoader(List<PictureLoadedListener> pictureLoadedListeners, int reqWidth, int reqHeight,
                            Context context, Media.Folder folder) {

    this.reqHeight = dpToPixels(reqHeight, context);
    this.reqWidth = dpToPixels(reqWidth, context);
    this.pictureLoadedListeners = pictureLoadedListeners;
    this.context = context;
    this.folder = folder;
  }

  @Override
  protected List<Bitmap> doInBackground(Media... media) {
    List<Bitmap> pictureBitmaps = new ArrayList<>();
    if (media.length == 0) {
      throw new IllegalArgumentException("at least one picture url was expected.");
    }
    for (Media picture : media) {
      Bitmap pictureBitmap = null;
      try {
        String pictureUrl = picture.getInetUrl();
        pictureBitmap = downloadBitmap(pictureUrl);
        if (pictureBitmap != null) {
          persistBitmap(picture, pictureBitmap);
        }
        pictureBitmaps.add(pictureBitmap);
      } catch (IOException e) {
        thrownException = e;
        Log.w("file", e.toString());
      }
    }
    return pictureBitmaps;
  }

  /**
   * Persists the picture as a file on the file system
   * @param picture the Media for this picture
   * @param picBitmap the Bitmap downloaded
   */
  private void persistBitmap(Media picture, Bitmap picBitmap) throws IOException {
    PictureSaver persister = new FilePictureSaver(context);
    persister.persist(picture.getMediaFile(context, folder), picBitmap);
  }

  /**
   * Downloads the picture to match the required dimensions.
   * @param pictureUrl the url (most likely with http scheme) where the picture is located
   * @return
   * @throws java.lang.IllegalArgumentException caused by a wrongly formatted url
   */
  private Bitmap downloadBitmap(String pictureUrl) throws IOException {
    Bitmap pictureBitmap = null;
    InputStream pictureStream = null;
    if (pictureUrl.isEmpty()){
      return null;
    }
    if (!pictureUrl.contains("://")){
      pictureUrl = "http://" + pictureUrl;
    }
    try {
      pictureStream = new URL(pictureUrl).openStream();
      // first figure how large the picture is, then load only the needed size
      // cf. https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(pictureStream, null, options);
      int inSampleSize = Math.max(options.outWidth / reqWidth, options.outHeight / reqHeight);

      pictureStream.close();
      pictureStream = new URL(pictureUrl).openStream();

      options.inSampleSize = inSampleSize * 2;
      options.inJustDecodeBounds = false;
      pictureBitmap = BitmapFactory.decodeStream(pictureStream, null, options);
    } finally {
      if (pictureStream != null) {
        try {
          pictureStream.close();
        } catch (IOException e) {
          Log.e("picture", e.getMessage());
        }
      }
    }
    return pictureBitmap;
  }

  /**
   * Converts the size in dp to a size in pixels, for downloading pictures
   * @param dpDim the dimension in dp
   * @param context the current Android context
   * @return the dimension in pixels
   */
  private int dpToPixels(int dpDim, Context context) {
    return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpDim,
            context.getResources().getDisplayMetrics()));
  }

  @Override
  protected void onPostExecute(List<Bitmap> pictureBitmaps) {
    if (thrownException != null){
      Toast.makeText(context, context.getResources().getString(R.string.cannot_save_picture), Toast.LENGTH_SHORT).show();
      Log.w("picture", "Picture could not be saved");
    } else if (pictureBitmaps != null) {
      for (PictureLoadedListener pictureLoadedListener : pictureLoadedListeners) {
        for (Bitmap pictureBitmap: pictureBitmaps) {
          pictureLoadedListener.onPictureLoaded(pictureBitmap);
        }
      }
    }
  }
}
