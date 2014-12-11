package fr.vpm.audiorss.process;

/**
 * Created by vince on 03/11/14.
 *
 * Any user of an AsyncTask that should be called back when the async task is over should implement this interface.
 * T is the type of the return of the AsyncTask
 */
public interface AsyncCallbackListener<T> {

  /**
   * The Async Task should call this method before starting the process.
   * It is typically called in its onPreExecute() method.
   */
  void onPreExecute();

  /**
   * The Async Task should call back this method once the process is over.
   * It is typically called in its onPostExecute() method.
   * @param result the result of the processing of the AsyncTask.
   */
  void onPostExecute(T result);

  /**
   * A dummy asynchronous callback listener, because often we do not want to do anything before/after the asynchronous task.
   * @param <T> the type for the listener
   */
  class DummyCallback<T> implements AsyncCallbackListener<T> {

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onPostExecute(T result) {

    }
  }
}
