package fr.vpm.audiorss.http;

import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;

import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 06/11/14.
 */
public class SaveFeedCallback implements AsyncCallbackListener<RSSChannel> {

  private final ProgressListener progressListener;

  private final DataModel dataModel;

  public SaveFeedCallback(ProgressListener progressListener, DataModel dataModel) {
    this.progressListener = progressListener;
    this.dataModel = dataModel;
  }

  @Override
  public void onPreExecute() {
    progressListener.startRefreshProgress();
  }

  @Override
  public void onPostExecute(RSSChannel result) {
    progressListener.stopRefreshProgress();
    if (result != null) {
      try {
        result.saveToDb(progressListener, dataModel);
      } catch (ParseException e) {
        handleException(e);
      }
    }
  }

  private void handleException(Exception e) {
    Toast.makeText(dataModel.getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    Log.e("Exception", e.toString());
  }

}
