package fr.vpm.audiorss.db.filter;

import android.os.Parcel;
import android.os.Parcelable;

import fr.vpm.audiorss.db.DbMedia;

/**
 * Created by vince on 23/11/14.
 */
public class DownloadedFilter implements SelectionFilter {

  @Override
  public String getSelectionQuery() {
    return DbMedia.T_MEDIA + "." + DbMedia.IS_DL_KEY + ">0";
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

  public static final Parcelable.Creator<DownloadedFilter> CREATOR
          = new Parcelable.Creator<DownloadedFilter>() {
    public DownloadedFilter createFromParcel(Parcel in) {
      return new DownloadedFilter();
    }

    public DownloadedFilter[] newArray(int size) {
      return new DownloadedFilter[size];
    }
  };
}
