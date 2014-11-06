package fr.vpm.audiorss.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by vincent on 22/10/14.
 * <p/>
 * An asynchronous task to download a picture. It calls a listener once loaded.
 */
public class AsyncPictureLoader extends AsyncTask<String, Integer, Bitmap> {

  private final PictureLoadedListener pictureLoadedListener;

  private final int reqWidth;

  private final int reqHeight;

  public AsyncPictureLoader(PictureLoadedListener pictureLoadedListener, int reqWidth, int reqHeight) {
    this.pictureLoadedListener = pictureLoadedListener;
    this.reqWidth = reqWidth;
    this.reqHeight = reqHeight;
  }

  @Override
  protected Bitmap doInBackground(String... urls) {
    if (urls.length == 0) {
      throw new IllegalArgumentException("only one picture url was expected.");
    }
    String pictureUrl = urls[0];
    InputStream pictureStream = null;
    Bitmap pictureBitmap = null;

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
    } catch (IOException e) {
      throw new IllegalArgumentException("A well-formatted url was expected.");
    } finally {
      if (pictureStream != null) {
        try {
          pictureStream.close();
        } catch (IOException e) {
          Log.e("flickr", e.getMessage());
        }
      }
    }
    return pictureBitmap;
  }

  @Override
  protected void onPostExecute(Bitmap pictureBitmap) {
    pictureLoadedListener.onPictureLoaded(pictureBitmap);
  }
}
