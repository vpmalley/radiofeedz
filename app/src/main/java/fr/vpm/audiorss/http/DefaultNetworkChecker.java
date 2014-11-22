package fr.vpm.audiorss.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

import fr.vpm.audiorss.R;

/**
 * Created by vince on 28/10/14.
 */
public class DefaultNetworkChecker implements NetworkChecker {

  public static final String PREF_MEDIA_WIFI_NETWORK_ENABLED = "pref_media_wifi_network_enabled";
  public static final String PREF_MEDIA_MOBILE_NETWORK_ENABLED = "pref_media_mobile_network_enabled";
  public static final String PREF_REFRESH_WIFI_NETWORK_ENABLED = "pref_refresh_wifi_network_enabled";
  public static final String PREF_REFREESH_MOBILE_NETWORK_ENABLED = "pref_refreesh_mobile_network_enabled";

  /**
   * Retrieves the current information about the network (active connection, network type)
   * @param context the current Context
   * @return the current information about network
   */
  private NetworkInfo getNetworkInfo(Context context) {
    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return connMgr.getActiveNetworkInfo();
  }

  /**
   * Checks whether the network connection is active
   * @param context the current Context
   * @param displayError whether an error should be displayed when the connection is inactive
   * @param networkInfo the current information about network
   * @return whether the connection is active
   */
  private boolean checkConnection(Context context, boolean displayError, NetworkInfo networkInfo) {
    boolean isConnected = false;
    if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
      isConnected = true;
    } else if (displayError) {
      Toast.makeText(context, context.getResources().getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
    }
    return isConnected;
  }

  @Override
  public boolean checkNetworkForRefresh(Context context, boolean displayError) {
    boolean isConnected = false;
    NetworkInfo networkInfo = getNetworkInfo(context);
    if (checkConnection(context, displayError, networkInfo)) {
      if ((isWifiConnection(networkInfo) && isRefreshOverWifiAllowed(context)) ||
              (isMobileConnection(networkInfo) && isRefreshOverMobileAllowed(context)))
      {
        isConnected = true;
      } else if (displayError) {
        Toast.makeText(context, context.getResources().getString(R.string.disabled_refresh), Toast.LENGTH_SHORT).show();
      }
    }
    return isConnected;
  }

  @Override
  public boolean checkNetworkForDownload(Context context, boolean displayError) {
    boolean isConnected = false;
    NetworkInfo networkInfo = getNetworkInfo(context);
    if (checkConnection(context, displayError, networkInfo)) {
      if ((isWifiConnection(networkInfo) && isDownloadOverWifiAllowed(context)) ||
              (isMobileConnection(networkInfo) && isDownloadOverMobileAllowed(context)))
      {
        isConnected = true;
      } else if (displayError) {
        Toast.makeText(context, context.getResources().getString(R.string.disabled_download), Toast.LENGTH_SHORT).show();
      }
    }
    return isConnected;
  }

  /**
   * Determines whether the current connection is a Wifi connection (or Ethernet or Wimax)
   * @param networkInfo the current information about network
   * @return whether the current connection is a Wifi connection
   */
  private boolean isWifiConnection(NetworkInfo networkInfo) {
    int connType = networkInfo.getType();
    return (ConnectivityManager.TYPE_ETHERNET == connType) || (ConnectivityManager.TYPE_WIFI == connType) || (ConnectivityManager.TYPE_WIMAX == connType);
  }

  /**
   * Determines whether the current connection is a mobile connection (or Bluetooth)
   * @param networkInfo the current information about network
   * @return whether the current connection is a mobile connection
   */
  private boolean isMobileConnection(NetworkInfo networkInfo) {
    int connType = networkInfo.getType();
    return (ConnectivityManager.TYPE_MOBILE == connType) || (ConnectivityManager.TYPE_BLUETOOTH == connType);
  }

  /**
   * Retrieves the value for a preference
   * @param context the current Context
   * @param prefKey the key to the preference
   * @param defaultValue the default value for the preference
   * @return the current or default value for the preference
   */
  private boolean getPreference(Context context, String prefKey, boolean defaultValue){
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    return sharedPref.getBoolean(prefKey, defaultValue);
  }

  @Override
  public boolean isDownloadOverWifiAllowed(Context context) {
    return getPreference(context, PREF_MEDIA_WIFI_NETWORK_ENABLED, true);
  }

  @Override
  public boolean isDownloadOverMobileAllowed(Context context) {
    return getPreference(context, PREF_MEDIA_MOBILE_NETWORK_ENABLED, false);
  }

  @Override
  public boolean isRefreshOverWifiAllowed(Context context) {
    return getPreference(context, PREF_REFRESH_WIFI_NETWORK_ENABLED, true);
  }

  @Override
  public boolean isRefreshOverMobileAllowed(Context context) {
    return getPreference(context, PREF_REFREESH_MOBILE_NETWORK_ENABLED, true);
  }
}
