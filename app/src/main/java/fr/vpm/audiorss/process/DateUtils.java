package fr.vpm.audiorss.process;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vince on 02/03/15.
 */
public class DateUtils {
  public static final String RSS_DATE_MIN_PATTERN = "dd MMM yyyy HH:mm";
  public static final String RSS_DATE_PATTERN_TZ_4D = "dd MMM yyyy HH:mm:ss ZZZ";
  public static final String RSS_DATE_PATTERN_TZ_2D_2D = "dd MMM yyyy HH:mm:ss ZZZZZ";
  public static final String DISPLAY_PATTERN = "EEEE, dd MMMM yyyy - HH:mm";
  public static final String DB_DATE_PATTERN = "yyyy-MM-dd-HH:mm:ss-ZZZZZ"; // should allow sorting date
  public static final String RSS_DATE_PATTERN_GMT = "dd MMM yyyy HH:mm:ss z";

  public static String getDisplayDate(String date) {
    String dateText = "";
    try {
      Date itemDate = new SimpleDateFormat(DB_DATE_PATTERN).parse(date);
      Calendar yesterday = Calendar.getInstance();
      yesterday.add(Calendar.HOUR, -18);
      Calendar lastweek = Calendar.getInstance();
      lastweek.add(Calendar.DAY_OF_YEAR, -6);
      if (itemDate.after(yesterday.getTime())){
        dateText =  new SimpleDateFormat("HH:mm").format(itemDate);
      } else if (itemDate.after(lastweek.getTime())){
        dateText = new SimpleDateFormat("EEEE").format(itemDate);
      } else {
        dateText = new SimpleDateFormat("dd MMMM").format(itemDate);
      }
    } catch (ParseException e) {
      Log.w("date", "could not parse date");
    }
    return dateText;
  }

  /**
   * Parses the date to make sure it is understandable and formats it into our own internal formats
   * @param pubDate the date retrieved from the RSS file
   * @return the date, formatted for internal storage
   */
  public static Date parseDate(String pubDate) {
    Date date;
    try {

      // Wed, 25 Feb 2015 14:00:22 Z
      Pattern expectedZPattern = Pattern.compile("[0-9]{2}\\s[A-Z][a-z]{2}\\s[0-9]{4}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}\\sZ");
      Matcher zM = expectedZPattern.matcher(pubDate);
      if (zM.find()){
        pubDate = pubDate.replace("Z", "GMT");
      }

      // time zone : Z/ZZ/ZZZ
      Pattern expectedNumPattern = Pattern.compile("[0-9]{2}\\s[A-Z][a-z]{2}\\s[0-9]{4}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}\\s[\\+\\-][0-9]{4}");
      Matcher numM = expectedNumPattern.matcher(pubDate);

      // time zone : ZZZZZ
      Pattern expectedTimePattern = Pattern.compile("[0-9]{2}\\s[A-Z][a-z]{2}\\s[0-9]{4}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}\\s[\\+\\-][0-9]{2}:[0-9]{2}");
      Matcher timeM = expectedTimePattern.matcher(pubDate);

      // time zone : z
      Pattern expectedGMTPattern = Pattern.compile("[0-9]{2}\\s[A-Z][a-z]{2}\\s[0-9]{4}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}\\s[A-z]{3}");
      Matcher gmtM = expectedGMTPattern.matcher(pubDate);

      Pattern expectedMinimumPattern = Pattern.compile("[0-9]{2}\\s[A-Z][a-z]{2}\\s[0-9]{4}\\s[0-9]{2}:[0-9]{2}");
      Matcher m = expectedMinimumPattern.matcher(pubDate);
      if (numM.find()){
        date = new SimpleDateFormat(RSS_DATE_PATTERN_TZ_4D, Locale.US).parse(numM.group());
      } else if (timeM.find()){
        date = new SimpleDateFormat(RSS_DATE_PATTERN_TZ_2D_2D, Locale.US).parse(timeM.group());
      } else if (gmtM.find()){
        date = new SimpleDateFormat(RSS_DATE_PATTERN_GMT, Locale.US).parse(gmtM.group());
      } else if (m.find()){
        date = new SimpleDateFormat(RSS_DATE_MIN_PATTERN, Locale.US).parse(m.group());
      } else {
        Log.w("datePattern", "Could not parse the right date, defaulting to current date");
        date = Calendar.getInstance().getTime();
      }
    } catch (ParseException e) {
      Log.w("datePattern", "tried parsing date but failed: " + pubDate + ". " + e.getMessage());
      date = Calendar.getInstance().getTime();
    }
    return date;
  }
}