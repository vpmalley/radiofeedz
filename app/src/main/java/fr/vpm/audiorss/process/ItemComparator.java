package fr.vpm.audiorss.process;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import fr.vpm.audiorss.rss.RSSChannel;
import fr.vpm.audiorss.rss.RSSItem;

/**
 * Created by vince on 27/10/14.
 */
public class ItemComparator implements Comparator<RSSItem> {

    public enum ItemComparison{
        TIME,
        REVERSE_TIME,
        ALPHA,
        REVERSE_ALPHA
    }

    private final ItemComparison itemComparison;

    public ItemComparator(String itemComparison) {
       this.itemComparison = ItemComparison.valueOf(itemComparison);
    }

    @Override
    public int compare(RSSItem rssItem, RSSItem otherRssItem) {
        int comparison = 0;
        Date lhsDate = null;
        Date rhsDate = null;
        try {
            lhsDate = new SimpleDateFormat(RSSChannel.DATE_PATTERN, Locale.US).parse(rssItem.getDate());
            rhsDate = new SimpleDateFormat(RSSChannel.DATE_PATTERN, Locale.US).parse(otherRssItem.getDate());
        } catch (ParseException e) {
            Log.e("Exception", e.toString());
        }

        int comparisonByDate = 0;
        if ((lhsDate != null) && (rhsDate != null)) {
            comparisonByDate = lhsDate.compareTo(rhsDate);
        }
        int comparisonByName = rssItem.getTitle().compareTo(otherRssItem.getTitle());

        if (ItemComparison.ALPHA.equals(itemComparison) || ItemComparison.REVERSE_ALPHA.equals(itemComparison)) {
            comparison = comparisonByName;
        } else {
            comparison = comparisonByDate;
        }

        int factor = 1;
        if (ItemComparison.REVERSE_TIME.equals(itemComparison) || ItemComparison.REVERSE_ALPHA.equals(itemComparison)) {
            factor = -1;
        }

        if (comparison == 0) {
            comparison = comparisonByName + comparisonByDate;
        }

        return factor * comparison;
    }

}
