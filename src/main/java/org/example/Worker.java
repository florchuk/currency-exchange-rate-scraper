package org.example;

import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class Worker {
    private final Thread[] threads;

    /**
     * Constructor.
     * @param numberOfThreads The number of threads, worker is gonna workings with.
     */
    public Worker(int numberOfThreads) {
        // Creating the thread pool.
        this.threads = new Thread[numberOfThreads];
    }

    /**
     * Running the worker.
     * @param task Instance of the task, that will be running in each thread.
     * @param logger Instance of the logger.
     */
    public void run(Runnable task, Logger logger) {
        try {
            logger.info(
                    String.format(
                            "Worker is running. Thread pool size is %d.",
                            this.threads.length
                    )
            );

            // Filling the thread pool, with the tasks.
            for (int i = 0; i < this.threads.length; i++) {
                this.threads[i] = new Thread(task);

                // Starting the thread.
                this.threads[i].start();
            }

            // Waiting when all threads are finish their task.
            for (Thread thread : this.threads) {
                try {
                    thread.join();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } finally {
            // Cleaning the thread pool.
            Arrays.fill(this.threads, null);

            logger.info("Worker is successfully finished the task. Thread pool is cleaned up.");
        }
    }
}