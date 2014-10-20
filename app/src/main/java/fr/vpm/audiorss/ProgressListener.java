package fr.vpm.audiorss;

/**
 * Created by vince on 20/10/14.
 *
 * Listener for progress. Typically made available by an Activity to AsyncTasks
 */
public interface ProgressListener {

    /**
     * Starts the progress bar
     */
    void startRefreshProgress();

    /**
     * Updates the progress bar
     * @param progress the amount of progress
     */
    void updateProgress(int progress);

    /**
     * Stops the progress bar
     */
    void stopRefreshProgress();
}
