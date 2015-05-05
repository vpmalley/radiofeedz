package fr.vpm.audiorss.process;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by vince on 01/05/15.
 *
 * Manages tasks to be executed in the background
 */
public class TaskManager implements AsyncCallbackListener {

  private static final int MAX_TASKS = 2;

  public enum Priority {
    LOW, MEDIUM, HIGH
  }

  public interface Task {
    void execute();

    TaskManager.Priority getPriority();

    class TaskComparator implements Comparator<Task> {
      @Override
      public int compare(Task task, Task otherTask) {
        return task.getPriority().compareTo(otherTask.getPriority());
      }
    }
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
      new AsyncExecution().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, remainingTasks.get(0));
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

  public class AsyncExecution extends AsyncTask<Task, Integer, Task[]> {

    @Override
    protected void onPreExecute() {
      TaskManager.getManager().onPreExecute();
      super.onPreExecute();
    }

    @Override
    protected Task[] doInBackground(Task... tasks) {
      for (Task t : tasks) {
        t.execute();
      }
      return tasks;
    }

    @Override
    protected void onPostExecute(Task[] tasks) {
      TaskManager.getManager().onPostExecute(tasks);
      super.onPostExecute(tasks);
    }
  }
}
