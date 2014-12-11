package fr.vpm.audiorss.db.filter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 23/11/14.
 */
public class MaintenanceFilter implements QueryFilter.SelectionFilter {

  private final int daysExpiry;

  public MaintenanceFilter(int daysExpiry) {
    this.daysExpiry = daysExpiry;
  }

  @Override
  public int index() {
    return -1;
  }

  @Override
  public String getSelectionQuery() {
    StringBuilder selectionBuilder = new StringBuilder();
    selectionBuilder.append(RSSItem.DATE_TAG);
    selectionBuilder.append("<'");
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DAY_OF_YEAR, -1 * daysExpiry);
    String yesterdayDate = new SimpleDateFormat(RSSChannel.DB_DATE_PATTERN).format(yesterday.getTime());
    selectionBuilder.append(yesterdayDate);
    selectionBuilder.append("'");
    return selectionBuilder.toString();
  }

  @Override
  public String[] getSelectionValues() {
    return new String[0];
  }
}
