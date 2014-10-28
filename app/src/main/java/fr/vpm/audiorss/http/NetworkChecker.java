package fr.vpm.audiorss.http;

import android.content.Context;

/**
 * Created by vince on 28/10/14.
 */
public interface NetworkChecker {

    /**
     * Checks whether network is enabled in this context
     * @param context the current Context
     * @return whether the network services are enabled
     */
   boolean checkNetwork(Context context);
}
