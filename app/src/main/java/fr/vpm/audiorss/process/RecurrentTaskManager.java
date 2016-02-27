package fr.vpm.audiorss.process;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by vince on 27/02/16.
 */
public class RecurrentTaskManager {

  public static final String STORED_PATTERN = "yyyy-MM-dd HH:mmZ";
  public static final int DAYS_AGO = 5;

  public void performRecurrentTasks(Context context) {
    if (shouldPerformTask(context, "pushAnalytics")) {
      Stats.get(context).pushStats(context);
    }
  }

  public boolean shouldPerformTask(Context context, String taskKey) {
    boolean shouldPerformTask = false;
    SimpleDateFormat formatter = new SimpleDateFormat(STORED_PATTERN, Locale.US);
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    String lastExecution = sharedPref.getString(taskKey, "");

    if (lastExecution != null && !lastExecution.isEmpty()) {
      try {
        Date lastExecutionDate = formatter.parse(lastExecution);
        shouldPerformTask = isMoreThanXDaysAgo(lastExecutionDate);
      } catch (ParseException e) {
        Log.e("task." + taskKey, "failed parsing last execution date: " + lastExecution);
      }
    }
    if (shouldPerformTask || lastExecution == null || lastExecution.isEmpty()) {
      sharedPref.edit().putString(taskKey, formatter.format(new Date())).apply();
    }
    return shouldPerformTask;
  }

  private boolean isMoreThanXDaysAgo(Date lastExecutionDate) {
    Calendar lastExecutionCal = Calendar.getInstance();
    lastExecutionCal.setTime(lastExecutionDate);
    lastExecutionCal.add(Calendar.DAY_OF_YEAR, DAYS_AGO);
    return Calendar.getInstance().after(lastExecutionCal);
  }


}
