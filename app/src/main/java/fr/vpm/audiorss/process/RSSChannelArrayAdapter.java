package fr.vpm.audiorss.process;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.media.PictureLoadedListener;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 05/11/14.
 *
 * An Adapter to convert a RSSChannel to a view, displayed for each item in a list.
 */
public class RSSChannelArrayAdapter extends ArrayAdapter<RSSChannel> {

  private final Activity activity;

  private final List<RSSChannel> channels;

  private final int resource;

  public RSSChannelArrayAdapter(Activity activity, int resource, List<RSSChannel> channels) {
    super(activity, resource, channels);
    this.activity = activity;
    this.channels = channels;
    this.resource = resource;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder itemHolder;
    if (convertView == null) {
      LayoutInflater vi = activity.getLayoutInflater();
      convertView = vi.inflate(resource, parent, false);

      ImageView picImage = (ImageView) convertView.findViewById(R.id.feed_pic);
      TextView picTitle = (TextView) convertView.findViewById(R.id.item_title);

      itemHolder = new ViewHolder(picTitle, picImage);
      convertView.setTag(itemHolder);
    } else {
      itemHolder = (ViewHolder) convertView.getTag();
    }
    RSSChannel rssChannel = channels.get(position);

    itemHolder.titleView.setText(rssChannel.getTitle());
    List<PictureLoadedListener> listeners = new ArrayList<PictureLoadedListener>();
    Bitmap feedPic = rssChannel.getBitmap(activity, listeners);
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
