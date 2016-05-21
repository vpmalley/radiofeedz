package fr.vpm.audiorss.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.regex.Pattern;

/**
 * Created by vince on 21/05/16.
 */
public class MaxItemsPreference {

  public int get(Context context) {
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    String limit = sharedPrefs.getString("pref_disp_max_items", "80");
    if (limit == null || !Pattern.compile("\\d+").matcher(limit).matches()){
      limit = "80";
    }
    return Integer.valueOf(limit);
  }
}
