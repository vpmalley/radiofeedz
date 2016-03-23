package fr.vpm.audiorss.process;

import android.app.Activity;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.media.IconDisplay;
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
      TextView itemContent = (TextView) convertView.findViewById(R.id.item_content);

      itemHolder = new ViewHolder(itemTitle, feedTitle, itemDate, feedImage, itemIcon1, itemIcon2, itemContent);
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
    if ((itemHolder.feedTitleView != null) && (rssChannel != null)) {
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
    if ((rssItem.getMedia() != null) && (rssItem.getMedia().isDownloaded(getContext(), false))) {
      itemHolder.iconView2.setVisibility(View.VISIBLE);
    } else {
      itemHolder.iconView2.setVisibility(View.INVISIBLE);
    }

    // Feed content
    if (itemHolder.contentView != null) {
      itemHolder.contentView.setText(Html.fromHtml(rssItem.getDescription()));
    }

    return convertView;
  }

  private void setPicture(RSSItem rssItem, RSSChannel rssChannel, ViewHolder itemHolder){
    if (rssItem != null) {
      IconDisplay iconDisplay = rssItem.getIconDisplay(rssChannel);
      if (iconDisplay != null) {
        iconDisplay.loadInView(itemHolder.pictureView);
      } else {
        IconDisplay.loadBackupInView(itemHolder.pictureView);
      }
    } else {
      IconDisplay.loadBackupInView(itemHolder.pictureView);
    }
  }

  private void printDate(ViewHolder itemHolder, RSSItem rssItem) {
    String dateText = DateUtils.getDisplayDate(rssItem.getDate());
    itemHolder.dateView.setText(dateText);
  }

  public void setItems(List<RSSItem> items) {
    this.items.clear();
    this.items.addAll(items);
  }

  public void setChannelsByItem(Map<RSSItem, RSSChannel> channelsByItem) {
    this.channelsByItem = channelsByItem;
  }

  @Override
  public int getCount() {
    return items.size();
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

    private final TextView contentView;

    public ViewHolder(TextView titleView, TextView feedTitleView, TextView dateView, ImageView pictureView, ImageView iconView1, ImageView iconView2, TextView contentView) {
      this.titleView = titleView;
      this.feedTitleView = feedTitleView;
      this.dateView = dateView;
      this.pictureView = pictureView;
      this.iconView1 = iconView1;
      this.iconView2 = iconView2;
      this.contentView = contentView;
    }
  }
}
