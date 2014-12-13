package fr.vpm.audiorss.db.filter;

import android.os.Parcel;
import android.os.Parcelable;

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

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
  }

  public static final Parcelable.Creator<TodayFilter> CREATOR
          = new Parcelable.Creator<TodayFilter>() {
    public TodayFilter createFromParcel(Parcel in) {
      return new TodayFilter();
    }

    public TodayFilter[] newArray(int size) {
      return new TodayFilter[size];
    }
  };
}
