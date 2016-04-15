package fr.vpm.audiorss.presentation;

import java.util.Comparator;

import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Created by vince on 15/04/16.
 */
public class ChannelComparator implements Comparator<RSSChannel> {
  @Override
  public int compare(RSSChannel rssChannel, RSSChannel otherRssChannel) {
    return rssChannel.getTitle().compareTo(otherRssChannel.getTitle());
  }
}
