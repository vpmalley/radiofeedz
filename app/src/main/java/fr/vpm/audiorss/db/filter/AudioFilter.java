package fr.vpm.audiorss.db.filter;

import android.os.Parcel;

import fr.vpm.audiorss.db.DbMedia;

/**
 * Created by vince on 23/11/14.
 */
public class AudioFilter implements SelectionFilter {

  @Override
  public String getSelectionQuery() {
    return DbMedia.T_MEDIA + "." + DbMedia.MIME_KEY + " LIKE 'audio/%'";
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

  public static final Creator<AudioFilter> CREATOR
          = new Creator<AudioFilter>() {
    public AudioFilter createFromParcel(Parcel in) {
      return new AudioFilter();
    }

    public AudioFilter[] newArray(int size) {
      return new AudioFilter[size];
    }
  };
}
