package fr.vpm.audiorss.process;

import android.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by vince on 01/05/15.
 *
 * Manages tasks to be executed in the background
 */
public class TaskManager implements AsyncCallbackListener {

  private static final int MAX_TASKS = 3;

  public interface Task {
    void execute();
  }

  private final CopyOnWriteArrayList<Task> remainingTasks;

  private static TaskManager manager;

  public static TaskManager getManager() {
    if (manager == null) {
      manager = new TaskManager();
    }
    return manager;
  }

  private TaskManager() {
    remainingTasks = new CopyOnWriteArrayList<>();
  }

  @Override
  public void onPreExecute() {
    // do nothing
  }

  @Override
  public void onPostExecute(Object result) {
    runNextTask();
  }

  private void runNextTask() {
    Log.d("taskmanager", "remains " + remainingTasks.size());
    if (!remainingTasks.isEmpty()) {
      remainingTasks.get(0).execute();
      remainingTasks.remove(remainingTasks.get(0));
      Log.d("taskmanager", "starting a task");
    }
  }

  /**
   * Queues a task for background execution
   * @param task
   */
  public void queueTask(Task task) {
    remainingTasks.add(task);
  }

  /**
   * Launch all the queued tasks
   */
  public void startTasks() {
    for (int i = 0; i < MAX_TASKS; i++) {
      runNextTask();
    }
  }

}
