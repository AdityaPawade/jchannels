package com.adtsw.jchannels.messaging.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.adtsw.jcommons.execution.BlockingThreadPoolExecutor;
import com.adtsw.jcommons.execution.ThreadPoolStats;

import lombok.Getter;

public class InMemoryMessageQueue<I> extends AbstractMessageQueue<I> {

    @Getter
    private final Logger logger = LogManager.getLogger(InMemoryMessageQueue.class);
    
    private final Map<Integer, BlockingThreadPoolExecutor> executors;
    private final String queueName;
    private final int numPartitions;
    private final QueueFullAction queueFullAction;
    private final long timeout;
    private final TimeUnit unit;

    public InMemoryMessageQueue(String queueName, int numPartitions, int threadPoolSizePerPartition,
                                QueueFullAction queueFullAction) {
        this(queueName, numPartitions, threadPoolSizePerPartition, queueFullAction, 1, TimeUnit.SECONDS);
    }

    public InMemoryMessageQueue(String queueName, int numPartitions, int threadPoolSizePerPartition,
                                QueueFullAction queueFullAction, long timeout, TimeUnit unit) {

        this.queueFullAction = queueFullAction;
        this.executors = new HashMap<>();
        for (int i = 0; i < numPartitions; i++) {
            this.executors.put(i, new BlockingThreadPoolExecutor(
                queueName + "-" + i, threadPoolSizePerPartition, threadPoolSizePerPartition)
            );
        }
        this.queueName = queueName;
        this.numPartitions = numPartitions;
        this.timeout = timeout;
        this.unit = unit;
    }

    public InMemoryMessageQueue(String queueName, QueueFullAction queueFullAction) {
        this(queueName, 1, 10, queueFullAction);
    }

    public InMemoryMessageQueue(String queueName) {
        this(queueName, 1, 10, QueueFullAction.BLOCK);
    }

    @Override
    public boolean pushMessage(String topic, I message) {
        return pushMessage(topic, message, 0);
    }

    @Override
    public boolean pushMessage(String topic, I message, int partition) {
        boolean success = false;
        try {
            partition = Math.max(0, Math.min(partition, numPartitions));
            switch (queueFullAction) {
                case BLOCK: 
                    executors.get(partition).executeButBlockIfFull(() -> {
                        pushMessageToListeners(topic, message);
                    });
                    success = true;
                    break;
                case REJECT:
                    success = executors.get(partition).executeButRejectIfFull(() -> {
                        pushMessageToListeners(topic, message);
                    });
                    break;
                case BLOCK_WITH_TIMEOUT:
                    success = executors.get(partition).executeButBlockWithTimeoutIfFull(() -> {
                        pushMessageToListeners(topic, message);
                    }, timeout, unit);
                    break;
            }
        } catch (InterruptedException e) {
            logger.warn(queueName + " - Push message task interrupted " + e.getMessage());
            throw new RuntimeException(e);
        }
        return success;
    }

    @Override
    public MessageQueueStats getStats() {
        MessageQueueStats stats = new MessageQueueStats();

        int aggregatedSemaphoreQueueLength = 0;
        int aggregatedSemaphorePermitsAvailable = 0;
        int aggregatedWorkerPoolSize = 0;
        int aggregatedTaskQueueLength = 0;
        int aggregatedTaskQueueRemainingCapacity = 0;

        for (int i = 0; i < numPartitions; i++) {
            ThreadPoolStats threadPoolStats = executors.get(i).getStats();
            stats.add(queueName + "_" + i + "_" + "semaphore_queue_length", threadPoolStats.getSemaphoreQueueLength());
            stats.add(queueName + "_" + i + "_" + "semaphore_permits_available", threadPoolStats.getSemaphorePermitsAvailable());
            stats.add(queueName + "_" + i + "_" + "worker_pool_size", threadPoolStats.getWorkerPoolSize());
            stats.add(queueName + "_" + i + "_" + "task_queue_length", threadPoolStats.getTaskQueueLength());
            stats.add(queueName + "_" + i + "_" + "task_queue_remaining_capacity", threadPoolStats.getTaskQueueRemainingCapacity());
            aggregatedSemaphoreQueueLength = aggregatedSemaphoreQueueLength + threadPoolStats.getSemaphoreQueueLength();
            aggregatedSemaphorePermitsAvailable = aggregatedSemaphorePermitsAvailable + threadPoolStats.getSemaphorePermitsAvailable();
            aggregatedWorkerPoolSize = aggregatedWorkerPoolSize + threadPoolStats.getWorkerPoolSize();
            aggregatedTaskQueueLength = aggregatedTaskQueueLength + threadPoolStats.getTaskQueueLength();
            aggregatedTaskQueueRemainingCapacity = aggregatedTaskQueueRemainingCapacity + threadPoolStats.getTaskQueueRemainingCapacity();
        }
        stats.add(queueName + "_" + "semaphore_queue_length", aggregatedSemaphoreQueueLength);
        stats.add(queueName + "_" + "semaphore_permits_available", aggregatedSemaphorePermitsAvailable);
        stats.add(queueName + "_" + "worker_pool_size", aggregatedWorkerPoolSize);
        stats.add(queueName + "_" + "task_queue_length", aggregatedTaskQueueLength);
        stats.add(queueName + "_" + "task_queue_remaining_capacity", aggregatedTaskQueueRemainingCapacity);
        return stats;
    }

    @Override
    public void shutdown() {
        this.executors.forEach((partition, executor) -> {
            logger.info("Shutting down executor pool for " + queueName + " partition " + partition);
            executor.shutdown();
        });
    }
}
