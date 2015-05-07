package fr.vpm.audiorss.process;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by vince on 01/05/15.
 *
 * Manages tasks to be executed in the background
 */
public class TaskManager implements AsyncCallbackListener {

  private static final int MAX_TASKS = 3;

  public enum Priority {
    LOW, MEDIUM, HIGH
  }

  public interface Task {
    /**
     * Whether this task should be executed, now or later. If false, this task should never be executed
     * @return whether this task should be executed
     */
    boolean shouldExecute();

    /**
     * Whether all conditions are met to execute this task now
     * @return whether all conditions are met to execute this task now
     */
    boolean canExecute();

    /**
     * Runs the task
     */
    void execute();

    /**
     * Whether the task should be run asynchronously
     * @return whether the task should be run asynchronously
     */
    boolean isAsynch();
  }

  public abstract static class AsynchTask implements Task {
    @Override
    public boolean shouldExecute() {
      return true;
    }

    @Override
    public boolean canExecute() {
      return true;
    }

    @Override
    public boolean isAsynch() {
      return true;
    }
  }

  private final CopyOnWriteArrayList<Task> remainingTasks;

  private final CopyOnWriteArrayList<AsyncTask> launchedTasks;

  private static TaskManager manager;

  public static TaskManager getManager() {
    if (manager == null) {
      manager = new TaskManager();
    }
    return manager;
  }

  private TaskManager() {
    remainingTasks = new CopyOnWriteArrayList<>();
    launchedTasks = new CopyOnWriteArrayList<>();
  }

  /*
   * async callback
   */

  @Override
  public void onPreExecute() {
    // do nothing
  }

  @Override
  public void onPostExecute(Object result) {
    runNextTask();
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

  private void runNextTask() {
    updateTasks();
    if (!remainingTasks.isEmpty()) {
      Task taskToRun = remainingTasks.get(0);
      Log.d("taskmanager", "task to run should run : " + taskToRun.shouldExecute() + ", can run : " + taskToRun.canExecute());
      if  ((launchedTasks.size() < MAX_TASKS) && (taskToRun.canExecute())) {
        if (taskToRun.isAsynch()) {
          AsyncExecution asyncExecution = new AsyncExecution();
          asyncExecution.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, taskToRun);
          launchedTasks.add(asyncExecution);
        } else {
          taskToRun.execute();
        }
        remainingTasks.remove(taskToRun);
        Log.d("taskmanager", "starting a task, remains " + remainingTasks.size() + " and running: " + launchedTasks.size());
      } else {
        Log.d("taskmanager", "postponing a task, remains " + remainingTasks.size() + " and running: " + launchedTasks.size());
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            onPostExecute(null);
          }
        }, 1000);
      }
    }
  }

  private void updateTasks() {
    for (AsyncTask at : launchedTasks) {
      if (AsyncTask.Status.FINISHED == at.getStatus()) {
        launchedTasks.remove(at);
      }
    }
    for (Task t : remainingTasks) {
      if (!t.shouldExecute()) {
        remainingTasks.remove(t);
      }
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
