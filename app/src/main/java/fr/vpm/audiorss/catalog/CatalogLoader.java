package fr.vpm.audiorss.catalog;

import android.content.Context;

import java.util.List;

import fr.vpm.audiorss.catalog.json.FeedGroup;

/**
 * Created by vince on 27/04/16.
 */
public interface CatalogLoader {

  /**
   * Loads the catalog from file. To be done before any other action.
   * @param context the current Android context
   */
  List<FeedGroup> loadData(Context context);
}
