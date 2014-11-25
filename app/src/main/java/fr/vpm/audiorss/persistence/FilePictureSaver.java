package fr.vpm.audiorss.persistence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by vincent on 06/10/14.
 */
public class FilePictureSaver implements PictureSaver {

  public static final String FEEDS_PIC_DIR = "feeds-icons";
  public static final String APP_DIR = "RadioFeedz";
  public static final String PNG_EXT = ".png";
  private final Context context;

  public FilePictureSaver(Context context) {
    this.context = context;
  }

  @Override
  public Bitmap retrieve(File pictureFile) throws FileNotFoundException {
    if (!pictureFile.exists()){
      return null;
    }
    FileInputStream pictureStream = null;
    Bitmap pictureBitmap = null;
    try {
      pictureStream = new FileInputStream(pictureFile);
      pictureBitmap = BitmapFactory.decodeStream(pictureStream);
    } finally {
      if (pictureStream != null){
        try {
          pictureStream.close();
        } catch (IOException e) {
          Log.e("file", e.getMessage());
        }
      }
    }
    return pictureBitmap;
  }

  @Override
  public boolean persist(File pictureFile, Bitmap picture) throws IOException {
    boolean saved = false;
    if (isExternalStorageWritable()) {
      if (!pictureFile.exists()) {
        pictureFile.createNewFile();
      }
      // fill file
      FileOutputStream pictureFileOut = null;
      try {
        pictureFileOut = new FileOutputStream(pictureFile);
        picture.compress(Bitmap.CompressFormat.PNG, 100, pictureFileOut);
        saved = true;
      } finally {
        if (pictureFileOut != null) {
          try {
            pictureFileOut.close();
          } catch (IOException e) {
            Log.w("file", e.getMessage());
          }
        }
      }
    }
    return saved;
  }

  @Override
  public boolean delete(File pictureFile) {
    boolean deleted = false;
    if (isExternalStorageWritable()) {
      deleted = pictureFile.delete();
    }
    return deleted;
  }

  /**
   * Returns the file, created if non-existent, for the picture
   *
   * @param picFileName the name we want to store the picture in
   * @return an existing file for the post
   * @pre the external storage should be writable
   */
  private File getFileForPic(String picFileName) {
    File dir = new File(Environment.getExternalStoragePublicDirectory(APP_DIR), FEEDS_PIC_DIR);
    dir.mkdirs();
    // create file if non existent
    File picFile = new File(dir, picFileName.replace(' ', '_') + PNG_EXT);
    return picFile;
  }

  /**
   * Finds out whether the external storage is writable
   *
   * @return whether it is writable
   */
  private boolean isExternalStorageWritable() {
    return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
  }
}
