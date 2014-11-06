package fr.vpm.audiorss.process;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 05/11/14.
 *
 * An Adapter to convert a RSSItem to a view, displayed for each item in a list.
 */
public class RSSItemArrayAdapter extends ArrayAdapter<RSSItem> {

  private final Activity activity;

  private final List<RSSItem> items;

  private final Map<RSSItem, RSSChannel> channelsByItem;

  public RSSItemArrayAdapter(Activity activity, int resource, List<RSSItem> items, Map<RSSItem,
      RSSChannel> channelsByItem) {
    super(activity, resource, items);
    this.activity = activity;
    this.items = items;
    this.channelsByItem = channelsByItem;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder itemHolder;
    if (convertView == null) {
      LayoutInflater vi = activity.getLayoutInflater();
      convertView = vi.inflate(R.layout.list_rss_item, parent, false);

      ImageView picImage = (ImageView) convertView.findViewById(R.id.feed_pic);
      TextView picTitle = (TextView) convertView.findViewById(R.id.item_title);

      itemHolder = new ViewHolder(picTitle, picImage);
      convertView.setTag(itemHolder);
    } else {
      itemHolder = (ViewHolder) convertView.getTag();
    }
    itemHolder.titleView.setText(items.get(position).getTitle());
    Bitmap feedPic = channelsByItem.get(items.get(position)).getBitmap();
    if (feedPic != null) {
      itemHolder.pictureView.setImageBitmap(feedPic);
    } else {
      itemHolder.pictureView.setImageResource(R.drawable.ic_action_picture);
    }
    return convertView;
  }

  /**
   * Keeps a reference to the views associated with a item. Only views should be stored there,
   * i.e. NO DATA should be linked to that object.
   */
  public class ViewHolder {

    private final TextView titleView;

    private final ImageView pictureView;

    public ViewHolder(TextView titleView, ImageView pictureView) {
      this.titleView = titleView;
      this.pictureView = pictureView;
    }
  }
}
