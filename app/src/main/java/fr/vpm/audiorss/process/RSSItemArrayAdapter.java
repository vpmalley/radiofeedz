package fr.vpm.audiorss.process;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.media.PictureLoadedListener;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 05/11/14.
 *
 * An Adapter to convert a RSSItem to a view, displayed for each item in a list.
 */
public class RSSItemArrayAdapter extends ArrayAdapter<RSSItem> {

  private final Activity activity;

  private List<RSSItem> items;

  private Map<RSSItem, RSSChannel> channelsByItem;

  private final int resource;

  public RSSItemArrayAdapter(Activity activity, int resource, List<RSSItem> items, Map<RSSItem,
      RSSChannel> channelsByItem) {
    super(activity, resource, items);
    this.activity = activity;
    this.items = items;
    this.channelsByItem = channelsByItem;
    this.resource = resource;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder itemHolder;
    if (convertView == null) {
      LayoutInflater layoutInflater = activity.getLayoutInflater();
      convertView = layoutInflater.inflate(resource, parent, false);
      ImageView itemImage = (ImageView) convertView.findViewById(R.id.feed_pic);
      TextView itemTitle = (TextView) convertView.findViewById(R.id.item_title);
      TextView itemDate = (TextView) convertView.findViewById(R.id.item_date);

      itemHolder = new ViewHolder(itemTitle, itemDate, itemImage);
      convertView.setTag(itemHolder);
    } else {
      itemHolder = (ViewHolder) convertView.getTag();
    }
    RSSItem rssItem = items.get(position);
    //RSSChannel rssChannel = RSSChannel.fromDbById(rssItem.getChannelId(), getContext());
    RSSChannel rssChannel = channelsByItem.get(rssItem);

    itemHolder.titleView.setText(rssItem.getTitle());
    if (!rssItem.isRead()) {
      itemHolder.titleView.setTypeface(Typeface.DEFAULT_BOLD);
    } else {
      itemHolder.titleView.setTypeface(Typeface.DEFAULT);
    }
    try {
      Date itemDate = new SimpleDateFormat(RSSChannel.DB_DATE_PATTERN).parse(rssItem.getDate());
      Calendar yesterday = Calendar.getInstance();
      yesterday.add(Calendar.HOUR, -24);
      Calendar lastweek = Calendar.getInstance();
      lastweek.add(Calendar.DAY_OF_YEAR, -7);
      String dateText = "";
      if (itemDate.after(yesterday.getTime())){
        dateText = getContext().getResources().getString(R.string.today) + ", " + new SimpleDateFormat("HH:mm").format(itemDate);
      } else if (itemDate.after(lastweek.getTime())){
        dateText = new SimpleDateFormat("EEEE, HH:mm").format(itemDate);
      } else {
        dateText = new SimpleDateFormat("dd MMMM").format(itemDate);
      }
      itemHolder.dateView.setText(dateText);
    } catch (ParseException e) {
      Log.w("date", "could not parse date");
    }

    List<PictureLoadedListener> listeners = new ArrayList<PictureLoadedListener>();
    Bitmap feedPic = null;
    if (rssChannel != null) {
      feedPic = rssChannel.getBitmap(activity, listeners);
    }
    if (feedPic != null) {
      itemHolder.pictureView.setImageBitmap(feedPic);
    } else {
      itemHolder.pictureView.setImageResource(R.drawable.ic_action_picture);
    }
    return convertView;
  }

  public void setItems(List<RSSItem> items) {
    this.items = items;
  }

  public void setChannelsByItem(Map<RSSItem, RSSChannel> channelsByItem) {
    this.channelsByItem = channelsByItem;
  }

  /**
   * Keeps a reference to the views associated with a item. Only views should be stored there,
   * i.e. NO DATA should be linked to that object.
   */
  public class ViewHolder {

    private final TextView titleView;

    private final TextView dateView;

    private final ImageView pictureView;

    public ViewHolder(TextView titleView, TextView dateView, ImageView pictureView) {
      this.titleView = titleView;
      this.dateView = dateView;
      this.pictureView = pictureView;
    }
  }
}
