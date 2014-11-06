package fr.vpm.audiorss.http;

import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.util.List;

import fr.vpm.audiorss.FeedsActivity;
import fr.vpm.audiorss.ProgressListener;
import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 06/11/14.
 */
public class SaveFeedCallback implements AsyncCallbackListener<RSSChannel> {

  private final ProgressListener progressListener;

  private final FeedsActivity<List<RSSChannel>> activity;

  /**
   * The counter is used to join all threads for the refresh of multiple feeds.
   * The refresh of the view is done only once, when all feeds are up-to-date.
   */
  private static int feedsCounter = 0;

  public SaveFeedCallback(ProgressListener progressListener, FeedsActivity<List<RSSChannel>> activity) {
    this.progressListener = progressListener;
    this.activity = activity;
  }

  @Override
  public void onPreExecute() {
    progressListener.startRefreshProgress();
  }

  @Override
  public void onPostExecute(RSSChannel result) {
    progressListener.stopRefreshProgress();
    if (feedsAreLoaded() && (result != null)) {
      try {
        result.saveToDb(progressListener, activity);
      } catch (ParseException e) {
        handleException(e);
      }
    }
  }

  private void handleException(Exception e) {
    Toast.makeText(activity.getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    Log.e("Exception", e.toString());
  }

  public synchronized static boolean updateFeedsCounter(int change){
    feedsCounter += change;
    Log.d("refresh", "feeds " + feedsCounter);
    return 0 == feedsCounter;
  }

  private synchronized boolean feedsAreLoaded(){
    return updateFeedsCounter(-1);
  }

}
