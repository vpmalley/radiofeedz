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
import fr.vpm.audiorss.media.Media;
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

    // retrieving the view
    if (convertView == null) {
      LayoutInflater layoutInflater = activity.getLayoutInflater();
      convertView = layoutInflater.inflate(resource, parent, false);
      ImageView feedImage = (ImageView) convertView.findViewById(R.id.feed_pic);
      TextView itemTitle = (TextView) convertView.findViewById(R.id.item_title);
      TextView feedTitle = (TextView) convertView.findViewById(R.id.feed_title);
      TextView itemDate = (TextView) convertView.findViewById(R.id.item_date);
      ImageView itemIcon1 = (ImageView) convertView.findViewById(R.id.item_icon_1);
      ImageView itemIcon2 = (ImageView) convertView.findViewById(R.id.item_icon_2);

      itemHolder = new ViewHolder(itemTitle, feedTitle, itemDate, feedImage, itemIcon1, itemIcon2);
      convertView.setTag(itemHolder);
    } else {
      itemHolder = (ViewHolder) convertView.getTag();
    }
    RSSItem rssItem = items.get(position);
    //RSSChannel rssChannel = RSSChannel.fromDbById(rssItem.getChannelId(), getContext());
    RSSChannel rssChannel = channelsByItem.get(rssItem);


    // Title
    itemHolder.titleView.setText(rssItem.getTitle());
    if (!rssItem.isRead()) {
      itemHolder.titleView.setTypeface(Typeface.DEFAULT_BOLD);
    } else {
      itemHolder.titleView.setTypeface(Typeface.DEFAULT);
    }

    // Feed title
    if (itemHolder.feedTitleView != null) {
      itemHolder.feedTitleView.setText(rssChannel.getShortenedTitle());
    }

    // Date
    printDate(itemHolder, rssItem);

    // Feed picture
    setPicture(rssItem, rssChannel, itemHolder);

    // Icons : unread / downloaded
    if (!rssItem.isRead()) {
      itemHolder.iconView1.setVisibility(View.VISIBLE);
    } else {
      itemHolder.iconView1.setVisibility(View.INVISIBLE);
    }
    if ((rssItem.getMedia() != null) && (rssItem.getMedia().isPodcastDownloaded(getContext(), false))) {
      itemHolder.iconView2.setVisibility(View.VISIBLE);
    } else {
      itemHolder.iconView2.setVisibility(View.INVISIBLE);
    }

    return convertView;
  }

  private void setPicture(RSSItem rssItem, RSSChannel rssChannel, ViewHolder itemHolder){
    List<PictureLoadedListener> listeners = new ArrayList<PictureLoadedListener>();
    Bitmap bm = null;
    if ((rssItem != null) && (rssItem.getMedia().isPicture())){
      bm = rssItem.getMedia().getAsBitmap(getContext(), listeners, Media.Folder.INTERNAL_ITEMS_PICS);
    } else if (rssChannel != null){
      bm = rssChannel.getBitmap(getContext(), listeners);
    }
    if (bm != null) {
      itemHolder.pictureView.setImageBitmap(bm);
    } else {
      itemHolder.pictureView.setImageResource(R.drawable.ic_action_picture);
    }
  }

  private void printDate(ViewHolder itemHolder, RSSItem rssItem) {
    try {
      Date itemDate = new SimpleDateFormat(RSSChannel.DB_DATE_PATTERN).parse(rssItem.getDate());
      Calendar yesterday = Calendar.getInstance();
      yesterday.add(Calendar.HOUR, -24);
      Calendar lastweek = Calendar.getInstance();
      lastweek.add(Calendar.DAY_OF_YEAR, -7);
      String dateText = "";
      if (itemDate.after(yesterday.getTime())){
        dateText =  new SimpleDateFormat("HH:mm").format(itemDate);
      } else if (itemDate.after(lastweek.getTime())){
        dateText = new SimpleDateFormat("EEEE").format(itemDate);
      } else {
        dateText = new SimpleDateFormat("dd MMMM").format(itemDate);
      }
      itemHolder.dateView.setText(dateText);
    } catch (ParseException e) {
      Log.w("date", "could not parse date");
    }
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

    private final TextView feedTitleView;

    private final TextView dateView;

    private final ImageView pictureView;

    private final ImageView iconView1;

    private final ImageView iconView2;

    public ViewHolder(TextView titleView, TextView feedTitleView, TextView dateView, ImageView pictureView, ImageView iconView1, ImageView iconView2) {
      this.titleView = titleView;
      this.feedTitleView = feedTitleView;
      this.dateView = dateView;
      this.pictureView = pictureView;
      this.iconView1 = iconView1;
      this.iconView2 = iconView2;
    }
  }
}
