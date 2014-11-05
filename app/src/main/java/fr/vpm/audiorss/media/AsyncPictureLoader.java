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

  public AsyncPictureLoader(PictureLoadedListener pictureLoadedListener) {
    this.pictureLoadedListener = pictureLoadedListener;
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
      pictureBitmap = BitmapFactory.decodeStream(pictureStream);
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
