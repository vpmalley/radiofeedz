package fr.vpm.audiorss;

import android.app.Application;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by vince on 20/02/16.
 */

@ReportsCrashes(
    formUri = "https://radiofeedz.cloudant.com/acra-radiofeedz/_design/acra-storage/_update/report",
    reportType = org.acra.sender.HttpSender.Type.JSON,
    httpMethod = org.acra.sender.HttpSender.Method.PUT,
    formUriBasicAuthLogin="stionlyoughtsoodurnevess",
    formUriBasicAuthPassword="ca6d3b54fe31351186ca0be0ffde560fb52c835e"
)
public class RFApplication extends Application {

    @Override
    public final void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}
