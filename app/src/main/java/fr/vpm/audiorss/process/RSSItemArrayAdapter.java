package fr.vpm.audiorss.process;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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
import android.widget.Toast;

import java.io.File;
import java.util.Collections;
import java.util.List;

import fr.vpm.audiorss.R;
import fr.vpm.audiorss.interaction.FeedItemsInteraction;
import fr.vpm.audiorss.media.IconDisplay;
import fr.vpm.audiorss.media.Media;
import fr.vpm.audiorss.media.MediaDownloadManager;
import fr.vpm.audiorss.presentation.DisplayedRSSItem;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 05/11/14.
 *
 * An Adapter to convert a RSSItem to a view, displayed for each item in a list.
 */
public class RSSItemArrayAdapter extends ArrayAdapter<DisplayedRSSItem> {

  private final Activity activity;

  private final int resource;

  private final FeedItemsInteraction feedItemsInteraction;

  public RSSItemArrayAdapter(Activity activity, int resource, List<DisplayedRSSItem> items, FeedItemsInteraction feedItemsInteraction) {
    super(activity, resource, items);
    this.activity = activity;
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
    DisplayedRSSItem rssItem = getItem(position);

    displayTitle(itemHolder, rssItem);
    displayFeedTitle(itemHolder, rssItem);
    displayDate(itemHolder, rssItem);
    displayPicture(itemHolder, rssItem);
    displayRightIcons(itemHolder, rssItem);
    displayContent(itemHolder, rssItem);

    displayActions(itemHolder, rssItem);
    setActionListeners(itemHolder);

    return convertView;
  }

  private void displayTitle(RSSItemViewHolder itemHolder, DisplayedRSSItem rssItem) {
    itemHolder.titleView.setText(rssItem.getRssItem().getTitle());
    if (!rssItem.getRssItem().isRead()) {
      itemHolder.titleView.setTypeface(Typeface.DEFAULT_BOLD);
    } else {
      itemHolder.titleView.setTypeface(Typeface.DEFAULT);
    }
  }

  private void displayFeedTitle(RSSItemViewHolder itemHolder, DisplayedRSSItem rssItem) {
    itemHolder.feedTitleView.setText(rssItem.getFeedTitle());
  }

  private void displayDate(RSSItemViewHolder itemHolder, DisplayedRSSItem rssItem) {
    String dateText = DateUtils.getDisplayDate(rssItem.getRssItem().getDate());
    itemHolder.dateView.setText(dateText);
  }

  private void displayPicture(RSSItemViewHolder itemHolder, DisplayedRSSItem rssItem){
    if (rssItem.getIconDisplay() != null) {
      rssItem.getIconDisplay().loadInView(itemHolder.pictureView);
    } else {
      IconDisplay.loadBackupInView(itemHolder.pictureView);
    }
  }

  private void displayRightIcons(RSSItemViewHolder itemHolder, DisplayedRSSItem rssItem) {
    if (!rssItem.getRssItem().isRead()) {
      itemHolder.iconView1.setVisibility(View.VISIBLE);
    } else {
      itemHolder.iconView1.setVisibility(View.INVISIBLE);
    }
    if (DisplayedRSSItem.Media.DOWNLOADED_AUDIO.equals(rssItem.getMediaStatus()) ||
        DisplayedRSSItem.Media.DOWNLOADED_PICTURE.equals(rssItem.getMediaStatus())) {
      itemHolder.iconView2.setVisibility(View.VISIBLE);
    } else {
      itemHolder.iconView2.setVisibility(View.INVISIBLE);
    }
  }

  private void displayContent(RSSItemViewHolder itemHolder, DisplayedRSSItem rssItem) {
    if (itemHolder.contentView != null) {
      String content = Html.fromHtml(rssItem.getRssItem().getDescription()).toString().replace((char) 65532, ' ').trim();
      itemHolder.contentView.setText(content);
    }
  }

  private void displayActions(RSSItemViewHolder itemHolder, DisplayedRSSItem rssItem) {
    if (rssItem.getRssItem().isRead()) {
      itemHolder.readIconView.setVisibility(View.GONE);
      itemHolder.unreadIconView.setVisibility(View.VISIBLE);
    } else {
      itemHolder.readIconView.setVisibility(View.VISIBLE);
      itemHolder.unreadIconView.setVisibility(View.GONE);
    }

    switch(rssItem.getMediaStatus()) {
      case DOWNLOADABLE:
        itemHolder.playIconView.setVisibility(View.GONE);
        itemHolder.displayIconView.setVisibility(View.GONE);
        itemHolder.downloadIconView.setVisibility(View.VISIBLE);
        itemHolder.deleteIconView.setVisibility(View.GONE);
        break;
      case DOWNLOADED_AUDIO:
        itemHolder.playIconView.setVisibility(View.VISIBLE);
        itemHolder.displayIconView.setVisibility(View.GONE);
        itemHolder.downloadIconView.setVisibility(View.GONE);
        itemHolder.deleteIconView.setVisibility(View.VISIBLE);
        break;
      case DOWNLOADED_PICTURE:
        itemHolder.playIconView.setVisibility(View.GONE);
        itemHolder.displayIconView.setVisibility(View.VISIBLE);
        itemHolder.downloadIconView.setVisibility(View.GONE);
        itemHolder.deleteIconView.setVisibility(View.VISIBLE);
        break;
      case NONE:
      default:
        itemHolder.playIconView.setVisibility(View.GONE);
        itemHolder.displayIconView.setVisibility(View.GONE);
        itemHolder.downloadIconView.setVisibility(View.GONE);
        itemHolder.deleteIconView.setVisibility(View.GONE);
        break;
    }
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
    ImageView deleteIconView = (ImageView) convertView.findViewById(R.id.action_delete);
    ImageView readIconView = (ImageView) convertView.findViewById(R.id.action_read);
    ImageView unreadIconView = (ImageView) convertView.findViewById(R.id.action_unread);
    ImageView shareIconView = (ImageView) convertView.findViewById(R.id.action_share);

    itemHolder = new RSSItemViewHolder(itemTitle, feedTitle, itemDate, feedImage, itemIcon1, itemIcon2, itemContent, webIconView, downloadIconView, playIconView, displayIconView, deleteIconView, readIconView, unreadIconView, shareIconView);
    return itemHolder;
  }


  // Actions

  private void setActionListeners(RSSItemViewHolder itemHolder) {
    itemHolder.webIconView.setOnClickListener(onOpenWeb);
    itemHolder.downloadIconView.setOnClickListener(onDownload);
    itemHolder.playIconView.setOnClickListener(onPlay);
    itemHolder.displayIconView.setOnClickListener(onDisplay);
    itemHolder.deleteIconView.setOnClickListener(onDeleteMedia);
    itemHolder.readIconView.setOnClickListener(onMarkAsRead);
    itemHolder.unreadIconView.setOnClickListener(onMarkAsUnread);
    itemHolder.shareIconView.setOnClickListener(onShare);
  }

  private View.OnClickListener onOpenWeb = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Stats.get(getContext()).increment(Stats.ACTION_WEB);
      RSSItem rssItem = getFeedItemForButton(view);
      Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(rssItem.getLink()));
      activity.startActivity(i);
    }
  };

  private View.OnClickListener onDownload = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Stats.get(getContext()).increment(Stats.ACTION_DOWNLOAD);
      RSSItem rssItem = getFeedItemForButton(view);
      feedItemsInteraction.downloadMedia(rssItem);
    }
  };

  private View.OnClickListener onPlay = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Stats.get(getContext()).increment(Stats.ACTION_PLAY);
      RSSItem rssItem = getFeedItemForButton(view);
      playMedia(rssItem);
    }
  };

  private View.OnClickListener onDisplay = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Stats.get(getContext()).increment(Stats.ACTION_PLAY);
      RSSItem rssItem = getFeedItemForButton(view);
      playMedia(rssItem);
    }
  };

  private View.OnClickListener onDeleteMedia = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Stats.get(getContext()).increment(Stats.ACTION_DELETE);
      RSSItem rssItem = getFeedItemForButton(view);
      feedItemsInteraction.deleteMedia(rssItem);
      View item = (View) view.getParent().getParent();
      int position = ((ListView) item.getParent()).getPositionForView(item);
      getItem(position).setMediaStatus(DisplayedRSSItem.Media.NONE);
      notifyDataSetChanged();
    }
  };

  private View.OnClickListener onMarkAsRead = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Stats.get(getContext()).increment(Stats.ACTION_MARK_READ);
      RSSItem rssItem = getFeedItemForButton(view);
      feedItemsInteraction.markAsRead(Collections.singletonList(rssItem), true);
      rssItem.setRead(true);
      notifyDataSetChanged();
    }
  };

  private View.OnClickListener onMarkAsUnread = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Stats.get(getContext()).increment(Stats.ACTION_MARK_READ);
      RSSItem rssItem = getFeedItemForButton(view);
      feedItemsInteraction.markAsRead(Collections.singletonList(rssItem), false);
      rssItem.setRead(false);
      notifyDataSetChanged();
    }
  };

  private View.OnClickListener onShare = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Stats.get(getContext()).increment(Stats.ACTION_SHARE);
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
    return getItem(position).getRssItem();
  }

  private void playMedia(RSSItem rssItem) {
    Intent playIntent = new Intent(Intent.ACTION_VIEW);
    Media m = rssItem.getMedia();
    if (m != null) {
      File mediaFile = new MediaDownloadManager(activity).getDownloadFile(m);
      if (mediaFile.exists()) {
        playIntent.setDataAndType(Uri.fromFile(mediaFile), m.getMimeType());
      } else {
        playIntent.setDataAndType(new Uri.Builder().path(m.getDistantUrl()).build(), m.getMimeType());
      }
      try {
        activity.startActivity(playIntent);
      } catch (ActivityNotFoundException e) {
        Toast.makeText(activity, R.string.error_no_app_for_media, Toast.LENGTH_SHORT).show();
      }
    }
  }

}
