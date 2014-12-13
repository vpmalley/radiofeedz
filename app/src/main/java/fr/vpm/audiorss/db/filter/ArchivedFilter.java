package fr.vpm.audiorss.db.filter;

import android.os.Parcel;
import android.os.Parcelable;

import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 23/11/14.
 */
public class ArchivedFilter implements SelectionFilter {

  @Override
  public String getSelectionQuery() {
    return RSSItem.ARCHIVED_KEY + "<>0";
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

  public static final Parcelable.Creator<ArchivedFilter> CREATOR
          = new Parcelable.Creator<ArchivedFilter>() {
    public ArchivedFilter createFromParcel(Parcel in) {
      return new ArchivedFilter();
    }

    public ArchivedFilter[] newArray(int size) {
      return new ArchivedFilter[size];
    }
  };
}
