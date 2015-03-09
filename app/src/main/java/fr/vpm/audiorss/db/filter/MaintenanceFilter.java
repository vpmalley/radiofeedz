package fr.vpm.audiorss.db.filter;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

import fr.vpm.audiorss.db.DbMedia;
import fr.vpm.audiorss.process.DateUtils;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 23/11/14.
 */
public class MaintenanceFilter implements SelectionFilter {

  private final int daysExpiry;

  public MaintenanceFilter(int daysExpiry) {
    this.daysExpiry = daysExpiry;
  }

  @Override
  public String getSelectionQuery() {
    StringBuilder selectionBuilder = new StringBuilder();
    // date part of the query
    selectionBuilder.append(RSSItem.DATE_TAG);
    selectionBuilder.append("<'");
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DAY_OF_YEAR, -1 * daysExpiry);
    String yesterdayDate = DateUtils.formatDBDate(yesterday.getTime());
    selectionBuilder.append(yesterdayDate);
    selectionBuilder.append("'");

    selectionBuilder.append(" AND ");

    // download part (we do not delete the items that have been downloaded)
    selectionBuilder.append(DbMedia.T_MEDIA);
    selectionBuilder.append(".");
    selectionBuilder.append(DbMedia.IS_DL_KEY);
    selectionBuilder.append("=0");

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
    dest.writeInt(daysExpiry);
  }

  public static final Parcelable.Creator<MaintenanceFilter> CREATOR
          = new Parcelable.Creator<MaintenanceFilter>() {
    public MaintenanceFilter createFromParcel(Parcel in) {
      return new MaintenanceFilter(in.readInt());
    }

    public MaintenanceFilter[] newArray(int size) {
      return new MaintenanceFilter[size];
    }
  };
}
