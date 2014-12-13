package fr.vpm.audiorss.db.filter;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by vince on 23/11/14.
 */
public class EmptyFilter implements SelectionFilter {

  @Override
  public String getSelectionQuery() {
    return "";
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

  public static final Parcelable.Creator<EmptyFilter> CREATOR
          = new Parcelable.Creator<EmptyFilter>() {
    public EmptyFilter createFromParcel(Parcel in) {
      return new EmptyFilter();
    }

    public EmptyFilter[] newArray(int size) {
      return new EmptyFilter[size];
    }
  };
}
