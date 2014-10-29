package fr.vpm.audiorss.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by vince on 28/10/14.
 */
public class DefaultNetworkChecker implements NetworkChecker {

  public static final String E_NOT_CONNECTED = "This device does not appear connected to the Internet.";

  @Override
  public boolean checkNetwork(Context context) {
    boolean isConnected = false;
    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      isConnected = true;
    } else {
      Toast.makeText(context, E_NOT_CONNECTED, Toast.LENGTH_SHORT).show();
    }
    return isConnected;
  }
}
