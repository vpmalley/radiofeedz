package fr.vpm.audiorss.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import fr.vpm.audiorss.R;

/**
 * Created by vince on 28/10/14.
 */
public class DefaultNetworkChecker implements NetworkChecker {

  @Override
  public boolean checkNetwork(Context context) {
    boolean isConnected = false;
    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      isConnected = true;
    } else {
      Toast.makeText(context, context.getResources().getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
    }
    return isConnected;
  }
}
