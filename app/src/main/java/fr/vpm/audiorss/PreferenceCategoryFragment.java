package fr.vpm.audiorss;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by vincent on 05/10/14.
 */
public class PreferenceCategoryFragment extends PreferenceFragment {

  public static final String CATEGORY = "category";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if ("display".equals(getArguments().getString(CATEGORY))) {
      addPreferencesFromResource(R.xml.displaypreferences);
    } else if ("download".equals(getArguments().getString(CATEGORY))) {
      addPreferencesFromResource(R.xml.downloadpreferences);
    } else if ("network".equals(getArguments().getString(CATEGORY))) {
      addPreferencesFromResource(R.xml.networkpreferences);
    } else if ("libraries".equals(getArguments().getString(CATEGORY))) {
      addPreferencesFromResource(R.xml.libraries);
    }
  }

}
