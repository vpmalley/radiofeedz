package fr.vpm.audiorss.process;

import android.content.Context;

/**
 * Created by vince on 05/05/15.
 *
 */
public interface Cachable {

  /**
   * Determines whether the resource should be refreshed
   * @return whether the resource should be refreshed
   */
  boolean shouldRefresh();

  /**
   * Queries the resource
   * @param context the current Android context
   * @pre check the resource should be refreshed with {@link #shouldRefresh()}
   */
  void query(Context context);

  /**
   * Determines whether the query was successful
   * @return whether the query was successful
   */
  boolean isQueried();

  /**
   * Processes the retrieved resource
   * @param context
   * @pre the resource should have been queried and retrieved
   */
  void process(Context context);

  /**
   * Determines whether the processing was successful
   * @return whether the processing was successful
   */
  boolean isProcessed();

  /**
   * Stores the resource in a persistent storage - for offline retrieval
   * @param context the current Android context
   * @pre the resource should have been queried, retrieved and processed
   */
  void persist(Context context);

  /**
   * Post-processes the retrieved resource, once the rest is done
   * @param context
   * @pre the resource should have been queried and retrieved
   */
  void postProcess(Context context);

  /**
   * Stores the the resource in a non-persistent storage, typically in memory,
   * for fast storage and access
   * @pre the resource should have been queried, retrieved and processed
   */
  void staleStore();

}
