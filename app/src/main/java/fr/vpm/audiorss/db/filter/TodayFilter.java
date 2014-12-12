package fr.vpm.audiorss.db.filter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 23/11/14.
 */
public class TodayFilter implements SelectionFilter {

  @Override
  public String getSelectionQuery() {
    StringBuilder selectionBuilder = new StringBuilder();
    selectionBuilder.append(RSSItem.DATE_TAG);
    selectionBuilder.append(">'");
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.HOUR, -24);
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
