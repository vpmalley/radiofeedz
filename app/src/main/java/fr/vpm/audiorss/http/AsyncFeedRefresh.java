package fr.vpm.audiorss.http;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;

import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.ItemParser;
import fr.vpm.audiorss.rss.RSSChannel;

public class AsyncFeedRefresh extends AsyncTask<String, Integer, RSSChannel> {

  private final Context context;

  private final AsyncCallbackListener<RSSChannel> asyncCallbackListener;

  private Exception mE = null;

  public AsyncFeedRefresh(Context context,
                          AsyncCallbackListener<RSSChannel> asyncCallbackListener) {
    this.context = context;
    this.asyncCallbackListener = asyncCallbackListener;
    Log.d("measures", "openDialog " + String.valueOf(System.currentTimeMillis()));
  }

  @Override
  protected void onPreExecute() {
    asyncCallbackListener.onPreExecute();
    super.onPreExecute();
  }

  @Override
  protected RSSChannel doInBackground(String... params) {
    String url = params[0];
    RSSChannel newChannel = null;
    try {
      Log.d("refresh", "received a response for feed");
      newChannel = new ItemParser().parseChannel(url);
      Log.d("refresh", "created feed from " + newChannel.getUrl());
    } catch (XmlPullParserException | IOException | ParseException e) {
      mE = e;
    }
    return newChannel;
  }

  @Override
  protected void onPostExecute(RSSChannel newChannel) {
    if (newChannel != null) {
      Log.d("measures", "postRefresh s " + newChannel.getUrl() + String.valueOf(System.currentTimeMillis()));
    }

    if (mE != null) {
      Toast.makeText(context, "Could not refresh a feed", Toast.LENGTH_SHORT).show();
      Log.e("Exception", mE.toString());
    }
    asyncCallbackListener.onPostExecute(newChannel);
    if (newChannel != null) {
      Log.d("measures", "postRefresh e " + newChannel.getUrl() + String.valueOf(System.currentTimeMillis()));
    }
    super.onPostExecute(newChannel);
  }

}
