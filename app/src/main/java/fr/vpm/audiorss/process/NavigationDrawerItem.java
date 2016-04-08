package fr.vpm.audiorss.process;

import fr.vpm.audiorss.db.filter.SelectionFilter;
import fr.vpm.audiorss.rss.RSSChannel;

/**
 * Simple data structure for any item to show up in the navigation drawer
 */
public class NavigationDrawerItem {

  private final SelectionFilter filter;

  private final String title;

  private final RSSChannel boundChannel;

  private String statTag;

  public NavigationDrawerItem(SelectionFilter filter, String title, RSSChannel boundChannel, String statTag) {
    this.filter = filter;
    this.title = title;
    this.boundChannel = boundChannel;
    this.statTag = statTag;
  }

  public String getTitle() {
    return title;
  }

  public SelectionFilter getFilter(){
    return filter;
  }

  public boolean hasBoundChannel(){
    return boundChannel != null;
  }

  public RSSChannel getBoundChannel() {
    return boundChannel;
  }

  public String getStatTag() {
    return statTag;
  }

  @Override
  public String toString() {
    return title;
  }

}
