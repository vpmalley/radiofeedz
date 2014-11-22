package fr.vpm.audiorss;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import java.util.List;

/**
 * Created by vincent on 05/10/14.
 */
public class AllPreferencesActivity extends PreferenceActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onBuildHeaders(List<android.preference.PreferenceActivity.Header> target) {
    loadHeadersFromResource(R.xml.preferenceheaders, target);
  }

  @Override
  protected boolean isValidFragment(String fragmentName) {
    // any preference screen should be implemented as defined in the headers section of doc.
    if ("fr.vpm.audiorss.PreferenceCategoryFragment".equals(fragmentName)) {
      return true;
    }
    return false;
  }
}
