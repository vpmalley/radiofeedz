package fr.vpm.audiorss.db.filter;

import android.os.Parcel;
import android.os.Parcelable;

import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 23/11/14.
 */
public class UnreadFilter implements SelectionFilter {

  @Override
  public String getSelectionQuery() {
    return RSSItem.READ_KEY + "=0";
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

  public static final Parcelable.Creator<UnreadFilter> CREATOR
          = new Parcelable.Creator<UnreadFilter>() {
    public UnreadFilter createFromParcel(Parcel in) {
      return new UnreadFilter();
    }

    public UnreadFilter[] newArray(int size) {
      return new UnreadFilter[size];
    }
  };
}
