package fr.vpm.audiorss.maintenance;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import fr.vpm.audiorss.process.Stats;

/**
 * Created by vince on 27/02/16.
 */
public class RecurrentTaskManager {

  public static final String STORED_PATTERN = "yyyy-MM-dd HH:mmZ";
  public static final int ANALYTICS_DAYS_AGO = 5;
  public static final int CLEAN_ITEMS_DAYS_AGO = 4;
  public static final int REFRESH_DL_DAYS_AGO = 1;

  public void performRecurrentTasks(Context context) {
    if (shouldPerformTask(context, "pushAnalytics", ANALYTICS_DAYS_AGO)) {
      Stats.get(context).pushStats(context);
    }
    if (shouldPerformTask(context, "cleanItems", CLEAN_ITEMS_DAYS_AGO)) {
      new AsyncMaintenance(context).cleanItems();
    }
    if (shouldPerformTask(context, "refreshDownloads", REFRESH_DL_DAYS_AGO)) {
      new AsyncMaintenance(context).refreshDownloadedPodcasts();
    }
  }

  public boolean shouldPerformTask(Context context, String taskKey, int daysAgo) {
    boolean shouldPerformTask = false;
    SimpleDateFormat formatter = new SimpleDateFormat(STORED_PATTERN, Locale.US);
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    String lastExecution = sharedPref.getString(taskKey, "");

    if (lastExecution != null && !lastExecution.isEmpty()) {
      try {
        Date lastExecutionDate = formatter.parse(lastExecution);
        shouldPerformTask = isMoreThanXDaysAgo(lastExecutionDate, daysAgo);
      } catch (ParseException e) {
        Log.e("task." + taskKey, "failed parsing last execution date: " + lastExecution);
      }
    }
    if (shouldPerformTask || lastExecution == null || lastExecution.isEmpty()) {
      sharedPref.edit().putString(taskKey, formatter.format(new Date())).apply();
    }
    return shouldPerformTask;
  }

  private boolean isMoreThanXDaysAgo(Date lastExecutionDate, int daysAgo) {
    Calendar lastExecutionCal = Calendar.getInstance();
    lastExecutionCal.setTime(lastExecutionDate);
    lastExecutionCal.add(Calendar.DAY_OF_YEAR, daysAgo);
    return Calendar.getInstance().after(lastExecutionCal);
  }


}
