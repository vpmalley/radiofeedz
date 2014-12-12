package fr.vpm.audiorss.http;

import android.os.AsyncTask;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.util.List;

import fr.vpm.audiorss.SearchFeedActivity;

public class AsyncFeedSearch extends AsyncTask<String, Integer, List<SearchResult>> {

  SearchFeedActivity activity;

  public AsyncFeedSearch(SearchFeedActivity activity) {
    this.activity = activity;
  }

  @Override
  protected List<SearchResult> doInBackground(String... query) {
    List<SearchResult> result = null;
    try {
      result = new FeedSearch().query(query[0]);
    } catch (ClientProtocolException e) {
      throw new FeedException(e);
    } catch (IOException e) {
      throw new FeedException(e);
    }
    return result;
  }

  @Override
  protected void onPostExecute(List<SearchResult> result) {

    SearchResult.lastSearch = result;
    activity.refreshView();
    super.onPostExecute(result);
  }

}
