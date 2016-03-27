package fr.vpm.audiorss.process;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.media.IconDisplay;
import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.presentation.FeedItemsInteraction;
import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 05/11/14.
 *
 * An Adapter to convert a RSSItem to a view, displayed for each item in a list.
 */
public class RSSItemArrayAdapter extends ArrayAdapter<RSSItem> {

  private static final String MIME_IMAGE = "image";
  private final Activity activity;

  private List<RSSItem> items;

  private Map<RSSItem, RSSChannel> channelsByItem;

  private final int resource;

  private final FeedItemsInteraction feedItemsInteraction;

  public RSSItemArrayAdapter(Activity activity, int resource, List<RSSItem> items, Map<RSSItem,
      RSSChannel> channelsByItem, FeedItemsInteraction feedItemsInteraction) {
    super(activity, resource, items);
    this.activity = activity;
    this.items = items;
    this.channelsByItem = channelsByItem;
    this.resource = resource;
    this.feedItemsInteraction = feedItemsInteraction;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    RSSItemViewHolder itemHolder;

    // retrieving the view
    if (convertView == null) {
      LayoutInflater layoutInflater = activity.getLayoutInflater();
      convertView = layoutInflater.inflate(resource, parent, false);
      itemHolder = findAllViews(convertView);
      convertView.setTag(itemHolder);
    } else {
      itemHolder = (RSSItemViewHolder) convertView.getTag();
    }
    RSSItem rssItem = items.get(position);
    RSSChannel rssChannel = channelsByItem.get(rssItem);

    displayTitle(itemHolder, rssItem);
    displayFeedTitle(itemHolder, rssChannel);
    displayDate(itemHolder, rssItem);
    displayPicture(rssItem, rssChannel, itemHolder);
    displayRightIcons(itemHolder, rssItem);
    displayContent(itemHolder, rssItem);

    displayActions(itemHolder, rssItem);
    setActionListeners(itemHolder);

    return convertView;
  }

  private void displayTitle(RSSItemViewHolder itemHolder, RSSItem rssItem) {
    itemHolder.titleView.setText(rssItem.getTitle());
    if (!rssItem.isRead()) {
      itemHolder.titleView.setTypeface(Typeface.DEFAULT_BOLD);
    } else {
      itemHolder.titleView.setTypeface(Typeface.DEFAULT);
    }
  }

  private void displayFeedTitle(RSSItemViewHolder itemHolder, RSSChannel rssChannel) {
    if ((itemHolder.feedTitleView != null) && (rssChannel != null)) {
      itemHolder.feedTitleView.setText(rssChannel.getShortenedTitle());
    }
  }

  private void displayDate(RSSItemViewHolder itemHolder, RSSItem rssItem) {
    String dateText = DateUtils.getDisplayDate(rssItem.getDate());
    itemHolder.dateView.setText(dateText);
  }

  private void displayPicture(RSSItem rssItem, RSSChannel rssChannel, RSSItemViewHolder itemHolder){
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

  private void displayRightIcons(RSSItemViewHolder itemHolder, RSSItem rssItem) {
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
  }

  private void displayContent(RSSItemViewHolder itemHolder, RSSItem rssItem) {
    if (itemHolder.contentView != null) {
      String content = Html.fromHtml(rssItem.getDescription()).toString().replace((char) 65532, ' ').trim();
      itemHolder.contentView.setText(content);
    }
  }

  private void displayActions(RSSItemViewHolder itemHolder, RSSItem rssItem) {
    if (rssItem.isRead()) {
      itemHolder.readIconView.setVisibility(View.GONE);
      itemHolder.unreadIconView.setVisibility(View.VISIBLE);
    } else {
      itemHolder.readIconView.setVisibility(View.VISIBLE);
      itemHolder.unreadIconView.setVisibility(View.GONE);
    }

    itemHolder.playIconView.setVisibility(View.GONE);
    itemHolder.displayIconView.setVisibility(View.GONE);
  }

  @NonNull
  private RSSItemViewHolder findAllViews(View convertView) {
    RSSItemViewHolder itemHolder;ImageView feedImage = (ImageView) convertView.findViewById(R.id.feed_pic);
    TextView itemTitle = (TextView) convertView.findViewById(R.id.item_title);
    TextView feedTitle = (TextView) convertView.findViewById(R.id.feed_title);
    TextView itemDate = (TextView) convertView.findViewById(R.id.item_date);
    ImageView itemIcon1 = (ImageView) convertView.findViewById(R.id.item_icon_1);
    ImageView itemIcon2 = (ImageView) convertView.findViewById(R.id.item_icon_2);
    TextView itemContent = (TextView) convertView.findViewById(R.id.item_content);
    ImageView webIconView = (ImageView) convertView.findViewById(R.id.action_web);
    ImageView downloadIconView = (ImageView) convertView.findViewById(R.id.action_download);
    ImageView playIconView = (ImageView) convertView.findViewById(R.id.action_play);
    ImageView displayIconView = (ImageView) convertView.findViewById(R.id.action_display);
    ImageView readIconView = (ImageView) convertView.findViewById(R.id.action_read);
    ImageView unreadIconView = (ImageView) convertView.findViewById(R.id.action_unread);
    ImageView shareIconView = (ImageView) convertView.findViewById(R.id.action_share);

    itemHolder = new RSSItemViewHolder(itemTitle, feedTitle, itemDate, feedImage, itemIcon1, itemIcon2, itemContent, webIconView, downloadIconView, playIconView, displayIconView, readIconView, unreadIconView, shareIconView);
    return itemHolder;
  }


  // Actions

  private void setActionListeners(RSSItemViewHolder itemHolder) {
    itemHolder.webIconView.setOnClickListener(onOpenWeb);
    itemHolder.downloadIconView.setOnClickListener(onDownload);
    itemHolder.playIconView.setOnClickListener(onPlay);
    itemHolder.displayIconView.setOnClickListener(onDisplay);
    itemHolder.readIconView.setOnClickListener(onMarkAsRead);
    itemHolder.unreadIconView.setOnClickListener(onMarkAsUnread);
    itemHolder.shareIconView.setOnClickListener(onShare);
  }

  private View.OnClickListener onOpenWeb = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      RSSItem rssItem = getFeedItemForButton(view);
      Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(rssItem.getLink()));
      activity.startActivity(i);
    }
  };

  private View.OnClickListener onDownload = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      RSSItem rssItem = getFeedItemForButton(view);
      feedItemsInteraction.downloadMedia(Collections.singletonList(rssItem));
    }
  };

  private View.OnClickListener onPlay = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      RSSItem rssItem = getFeedItemForButton(view);
      playMedia(rssItem);
    }
  };

  private View.OnClickListener onDisplay = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      RSSItem rssItem = getFeedItemForButton(view);
      playMedia(rssItem);
    }
  };

  private View.OnClickListener onMarkAsRead = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      RSSItem rssItem = getFeedItemForButton(view);
      feedItemsInteraction.markAsRead(Collections.singletonList(rssItem), true);
    }
  };

  private View.OnClickListener onMarkAsUnread = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      RSSItem rssItem = getFeedItemForButton(view);
      feedItemsInteraction.markAsRead(Collections.singletonList(rssItem), false);
    }
  };

  private View.OnClickListener onShare = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      RSSItem rssItem = getFeedItemForButton(view);
      Intent shareIntent = new Intent();
      shareIntent.setAction(Intent.ACTION_SEND);
      shareIntent.putExtra(Intent.EXTRA_TEXT, rssItem.getLink());
      shareIntent.setType("text/plain");
      activity.startActivity(shareIntent);
    }
  };

  private RSSItem getFeedItemForButton(View view) {
    View item = (View) view.getParent().getParent();
    int position = ((ListView) item.getParent()).getPositionForView(item);
    return items.get(position);
  }

  private void playMedia(RSSItem rssItem) {
    Intent playIntent = new Intent(Intent.ACTION_VIEW);
    Media m = rssItem.getMedia();
    if (m != null) {
      Media.Folder externalDownloadsFolder = Media.Folder.EXTERNAL_DOWNLOADS_PODCASTS;
      if (m.getMimeType().startsWith(MIME_IMAGE)){
        externalDownloadsFolder = Media.Folder.EXTERNAL_DOWNLOADS_PICTURES;
      }
      File mediaFile = m.getMediaFile(activity, externalDownloadsFolder, false);
      if (mediaFile.exists()){
        playIntent.setDataAndType(Uri.fromFile(mediaFile), m.getMimeType());
      } else {
        playIntent.setDataAndType(new Uri.Builder().path(m.getDistantUrl()).build(), m.getMimeType());
      }
      activity.startActivity(playIntent);
    }
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

}
