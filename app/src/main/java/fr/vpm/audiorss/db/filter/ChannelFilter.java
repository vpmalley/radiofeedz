package fr.vpm.audiorss.db.filter;

import android.os.Parcel;
import android.os.Parcelable;

import fr.vpm.audiorss.db.DatabaseOpenHelper;

/**
 * Created by vince on 23/11/14.
 */
public class ChannelFilter implements SelectionFilter {

  private final long channelId;

  public ChannelFilter(long channelId) {
    this.channelId = channelId;
  }

  @Override
  public String getSelectionQuery() {
    return DatabaseOpenHelper.CHANNEL_ID_KEY + "=?";
  }

  @Override
  public String[] getSelectionValues() {
    return new String[]{String.valueOf(channelId)};
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(channelId);
  }

  public static final Parcelable.Creator<ChannelFilter> CREATOR
          = new Parcelable.Creator<ChannelFilter>() {
    public ChannelFilter createFromParcel(Parcel in) {
      return new ChannelFilter(in.readLong());
    }

    public ChannelFilter[] newArray(int size) {
      return new ChannelFilter[size];
    }
  };
}
