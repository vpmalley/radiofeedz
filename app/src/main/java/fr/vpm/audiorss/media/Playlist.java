package fr.vpm.audiorss.media;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vince on 17/03/15.
 */
public class Playlist {

  private List<Media> medias = new ArrayList<>();

  public void add(Media media) {
    medias.add(media);
  }

  public void createPlaylist(Context context){
    boolean created = false;
    if (medias.isEmpty()) {
      return;
    }

    StringBuilder shows = new StringBuilder();
    for (Media media : medias) {
      shows.append(media.getMediaFile(context, Media.Folder.EXTERNAL_DOWNLOADS_PODCASTS, false));
      shows.append('\n');
    }
    String playlistName = "radiofeedz.m3u";
    File dir = Media.getDownloadFolder(context, Media.Folder.EXTERNAL_DOWNLOADS_PODCASTS);
    File playlistFile = new File(dir, playlistName);

    if (isExternalStorageWritable()) {
      int i = 0;
      while (playlistFile.exists()) {
        playlistName = "radiofeedz-" + i++ + ".m3u";
        playlistFile = new File(dir, playlistName);
      }
      try {
        playlistFile.createNewFile();
      } catch (IOException e) {
        Log.w("file", e.getMessage());
      }

      // fill file
      FileWriter playlistWriter = null;
      try {
        playlistWriter = new FileWriter(playlistFile);
        playlistWriter.write(shows.toString());
        created = true;
      } catch (IOException e) {
        Log.w("file", e.getMessage());
        Toast.makeText(context, "Oops ... an issue happened creating your playlist. Please try again soon", Toast.LENGTH_SHORT).show();
      } finally {
        if (playlistWriter != null) {
          try {
            playlistWriter.close();
          } catch (IOException e) {
            Log.w("file", e.getMessage());
          }
        }
      }
    }
    if (created) {
      Toast.makeText(context, "Successfully generated playlist " + playlistName, Toast.LENGTH_SHORT).show();
    }
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
