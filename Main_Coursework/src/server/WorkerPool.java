package server;

import shared.util.Log;
import shared.util.RunnableAdapter;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Worker Pool for Running Tasks
 *
 * @author Huw Jones
 * @since 28/03/2016
 */
public final class WorkerPool {

    private final ArrayList<ScheduledFuture> futureTasks = new ArrayList<>();
    private final ScheduledExecutorService workerPool;

    public WorkerPool(int workers) {
        ThreadFactory factory = new WorkerPoolFactory();
        this.workerPool = Executors.newScheduledThreadPool(workers, factory);
        Log.Debug(String.format("Started worker pool: %s", workers));

        this.workerPool.scheduleWithFixedDelay(new TaskCleanup(), 5, 5, TimeUnit.MINUTES);
    }

    /**
     * Returns the status of the WorkerPool.
     *
     * @return Boolean: True = running, false = shutdown
     */
    public boolean isRunning() {
        return !this.workerPool.isShutdown();
    }

    /**
     * Dispatches an event to run in the worker pool
     *
     * @param event Event to Dispatch
     */
    public void dispatchEvent(RunnableAdapter event) {
        if (this.workerPool.isShutdown()) {
            new Thread(event, "EventDispatch").start();
        } else {
            this.workerPool.submit(event);
        }
    }

    /**
     * Runs a task asynchronously in the worker pool
     *
     * @param task Task to run
     */
    public void queueTask(RunnableAdapter task) {
        if (this.workerPool.isShutdown()) {
            Log.Warn(String.format("Failed to queue task (%s). Worker Pool shutting down...", task.toString()));
            return;
        }
        Log.Debug(String.format("Queuing task (%s) to run now", task.toString()));
        this.workerPool.submit(task);
    }

    /**
     * Runs a task asynchronously in the worker pool
     *
     * @param task Task to run
     */
    public void queueTask(Callable<Object> task) {
        if (this.workerPool.isShutdown()) {
            Log.Warn(String.format("Failed to queue task (%s). Worker Pool shutting down...", task.toString()));
            return;
        }
        Log.Debug(String.format("Queuing task (%s) to run now", task.toString()));
        this.workerPool.submit(task);
    }

    /**
     * Schedules a task to run after a delay
     *
     * @param task      Task to run
     * @param timeDelay Time to delay task (in milliseconds);
     */
    public void scheduleTask(RunnableAdapter task, long timeDelay) {
        if (this.workerPool.isShutdown()) {
            Log.Warn(String.format("Failed to schedule task (%s). Worker Pool shutting down...", task.toString()));
            return;
        }
        Log.Debug(String.format("Scheduling task (%s) to run in %dms", task.toString(), timeDelay));
        ScheduledFuture futureTask = this.workerPool.schedule(task, timeDelay, TimeUnit.MILLISECONDS);
        this.futureTasks.add(futureTask);
    }

    /**
     * Schedules a task to run after a delay
     *
     * @param task      Task to run
     * @param timeDelay Time to delay task (in milliseconds);
     */
    public void scheduleTask(Callable<Object> task, long timeDelay) {
        if (this.workerPool.isShutdown()) {
            Log.Warn(String.format("Failed to schedule task (%s). Worker Pool shutting down...", task.toString()));
            return;
        }
        Log.Debug(String.format("Scheduling task (%s) to run in %dms", task.toString(), timeDelay));
        ScheduledFuture futureTask = this.workerPool.schedule(task, timeDelay, TimeUnit.MILLISECONDS);
        this.futureTasks.add(futureTask);
    }

    public void shutdown() {
        Log.Info("Shutting down worker pool...");
        workerPool.shutdown();
        try {
            // Cancel queued tasks
            int cancelledTasks = 0;
            ArrayList<ScheduledFuture> tasks = new ArrayList<>(this.futureTasks);
            for (ScheduledFuture task : tasks) {
                if (!task.isDone()) {
                    task.cancel(false);
                    cancelledTasks++;
                }
            }
            Log.Info(String.format("Cancelled %d task(s).", cancelledTasks));

            // Wait for running tasks to finish
            ThreadPoolExecutor executor = (ThreadPoolExecutor) this.workerPool;
            long numberOfTasks;
            while ((numberOfTasks = executor.getActiveCount()) != 0) {
                Log.Info(String.format("There are %d task(s) running, waiting 5 seconds and trying again...", numberOfTasks));

                this.workerPool.awaitTermination(5 * 1000, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException mayHappen) {
            Log.Trace(mayHappen.toString());
        }
        Log.Info("Worker pool shutdown!");
    }

    /**
     * The default thread factory
     */
    private static class WorkerPoolFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        WorkerPoolFactory() {
            group = new ThreadGroup("WorkerPool");
            namePrefix = "WorkerPool-W";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    /**
     * Cleans up scheduled tasks ArrayList
     */
    private class TaskCleanup extends RunnableAdapter {

        @Override
        public void runSafe() {
            Log.Trace("Cleaning Scheduled Tasks...");
            ArrayList<ScheduledFuture> tasks = new ArrayList<>(WorkerPool.this.futureTasks);
            // Removes the task from the futureTasks array if the task isDone.
            tasks.stream().filter(Future::isDone).forEach(WorkerPool.this.futureTasks::remove);
            Log.Debug(String.format("There are %d scheduled tasks.", WorkerPool.this.futureTasks.size()));
        }
    }
}