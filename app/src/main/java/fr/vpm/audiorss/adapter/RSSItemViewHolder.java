package fr.vpm.audiorss.adapter;

import android.widget.ImageView;
import android.widget.TextView;

/**
 * Keeps a reference to the views associated with a item. Only views should be stored there,
 * i.e. NO DATA should be linked to that object.
 */
public class RSSItemViewHolder {

  final TextView titleView;
  final TextView feedTitleView;
  final TextView dateView;
  final ImageView pictureView;
  final ImageView iconView1;
  final ImageView iconView2;
  final TextView contentView;
  final ImageView webIconView;
  final ImageView downloadIconView;
  final ImageView playIconView;
  final ImageView displayIconView;
  final ImageView deleteIconView;
  final ImageView shareIconView;

  public RSSItemViewHolder(TextView titleView, TextView feedTitleView, TextView dateView, ImageView pictureView,
                           ImageView iconView1, ImageView iconView2, TextView contentView, ImageView webIconView,
                           ImageView downloadIconView, ImageView playIconView, ImageView displayIconView, ImageView deleteIconView,
                           ImageView shareIconView) {
    this.titleView = titleView;
    this.feedTitleView = feedTitleView;
    this.dateView = dateView;
    this.pictureView = pictureView;
    this.iconView1 = iconView1;
    this.iconView2 = iconView2;
    this.contentView = contentView;
    this.webIconView = webIconView;
    this.downloadIconView = downloadIconView;
    this.playIconView = playIconView;
    this.displayIconView = displayIconView;
    this.deleteIconView = deleteIconView;
    this.shareIconView = shareIconView;
  }
}
