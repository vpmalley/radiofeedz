package fr.vpm.audiorss.maintenance;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * Created by vince on 19/04/16.
 */
public class FileScanner {


  private static final String STORAGE_TAG = "storage";

  public void analyseStorage(Context context) {
    deleteFiles(new File(context.getFilesDir().getParentFile(), "app_webview"));

    long totalSize = 0;
    File appBaseFolder = context.getFilesDir().getParentFile();
    for (File f: appBaseFolder.listFiles()) {
      if (f.isDirectory()) {
        long dirSize = browseFiles(f);
        totalSize += dirSize;
        Log.d(STORAGE_TAG, f.getPath() + " uses " + dirSize + " bytes");
      } else {
        totalSize += f.length();
      }
    }
    Log.d(STORAGE_TAG, "App uses " + totalSize + " total bytes");
  }

  private long browseFiles(File dir) {
    long dirSize = 0;
    for (File f: dir.listFiles()) {
      dirSize += f.length();
      //Log.d(STORAGE_TAG, dir.getAbsolutePath() + "/" + f.getName() + " weighs " + f.length());
      if (f.isDirectory()) {
        dirSize += browseFiles(f);
      }
    }
    return dirSize;
  }

  private void deleteFiles(File dir) {
    for (File f: dir.listFiles()) {
      if (f.isDirectory()) {
        deleteFiles(f);
      }
      f.delete();
    }
  }
}
