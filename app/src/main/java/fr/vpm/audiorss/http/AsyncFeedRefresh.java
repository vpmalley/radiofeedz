package fr.vpm.audiorss.http;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;

import fr.vpm.audiorss.process.AsyncCallbackListener;
import fr.vpm.audiorss.process.DataModel;
import fr.vpm.audiorss.process.ItemParser;
import fr.vpm.audiorss.process.TaskManager;
import fr.vpm.audiorss.rss.RSSChannel;

public class AsyncFeedRefresh extends AsyncTask<String, Integer, ItemParser> {

  private final Context context;

  private final AsyncCallbackListener<RSSChannel> asyncCallbackListener;

  private final DataModel dataModel;

  private Exception mE = null;

  public AsyncFeedRefresh(Context context,
                          AsyncCallbackListener<RSSChannel> asyncCallbackListener, DataModel dataModel) {
    this.context = context;
    this.asyncCallbackListener = asyncCallbackListener;
    this.dataModel = dataModel;
  }

  @Override
  protected void onPreExecute() {
    asyncCallbackListener.onPreExecute();
    super.onPreExecute();
  }

  @Override
  protected ItemParser doInBackground(String... params) {
    String url = params[0];
    ItemParser itemParser = null;
    try {
      Log.d("measures", "refresh start");
      long initRefresh = System.currentTimeMillis();
      itemParser = ItemParser.retrieveFeedContent(url);
      itemParser.extractRSSItems(itemParser.getThresholdDate(context), 5);
      Log.d("measures", "refresh -end- " + (System.currentTimeMillis() - initRefresh));
    } catch (XmlPullParserException | IOException | ParseException e) {
      mE = e;
    }
    return itemParser;
  }

  @Override
  protected void onPostExecute(ItemParser itemParser) {
    if (mE != null) {
      Toast.makeText(context, "Could not refresh a feed", Toast.LENGTH_SHORT).show();
      Log.e("Exception", mE.toString());
    }
    RSSChannel rssChannel = null;
    if (itemParser != null) {
      rssChannel = itemParser.getRssChannel();
      itemParser.setCallback(asyncCallbackListener);
      Log.d("postProcess", rssChannel.getTitle());
      dataModel.dataToPostProcess(itemParser);
    }
    // always call callback, with null if nothing is worth returning
    final RSSChannel finalRssChannel = rssChannel;
    TaskManager.getManager().queueTask(new TaskManager.Task() {
      @Override
      public void execute() {
        asyncCallbackListener.onPostExecute(finalRssChannel);
      }
    });
    TaskManager.getManager().onPostExecute(null);
    super.onPostExecute(itemParser);
  }

}
