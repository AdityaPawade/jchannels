package com.adtsw.jchannels.messaging.queue;

import com.adtsw.jcommons.execution.BlockingThreadPoolExecutor;
import com.adtsw.jcommons.execution.ThreadPoolStats;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryMessageQueue<I> implements MessageQueue<I> {

    private static final Logger logger = LogManager.getLogger(InMemoryMessageQueue.class);
    private final Map<String, List<MessageListener<I>>> listeners = new HashMap<>();
    private final Map<Integer, BlockingThreadPoolExecutor> executors;
    private final String queueName;
    private final int numPartitions;
    private final QueueFullAction queueFullAction;

    public InMemoryMessageQueue(String queueName, int numPartitions, int threadPoolSizePerPartition,
                                QueueFullAction queueFullAction) {

        this.queueFullAction = queueFullAction;
        this.executors = new HashMap<>();
        for (int i = 0; i < numPartitions; i++) {
            this.executors.put(i, new BlockingThreadPoolExecutor(
                queueName + "-" + i, threadPoolSizePerPartition, threadPoolSizePerPartition)
            );
        }
        this.queueName = queueName;
        this.numPartitions = numPartitions;
    }

    public InMemoryMessageQueue(String queueName, QueueFullAction queueFullAction) {
        this(queueName, 1, 10, queueFullAction);
    }

    public InMemoryMessageQueue(String queueName) {
        this(queueName, 1, 10, QueueFullAction.BLOCK);
    }

    public void registerTopic(String topic) {
        if(!listeners.containsKey(topic)) {
            listeners.put(topic, new ArrayList<>());
        }
    }

    public void addListener(String topic, MessageListener<I> listener) {
        registerTopic(topic);
        listeners.get(topic).add(listener);
    }

    @Override
    public void removeListener(String topic, MessageListener<I> listener) {
        listeners.get(topic).remove(listener);
    }

    public boolean pushMessage(String topic, I message) {
        return pushMessage(topic, message, 0);
    }

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
            }
        } catch (InterruptedException e) {
            logger.warn(queueName + " - Push message task interrupted " + e.getMessage());
        }
        return success;
    }

    private void pushMessageToListeners(String topic, I message) {
        List<MessageListener<I>> messageListeners = listeners.get(topic);
        if(CollectionUtils.isNotEmpty(messageListeners)) {
            messageListeners.forEach(listener -> listener.onMessage(message));
        } else {
            logger.warn("Sending message to topic without listener " + topic);
        }
    }

    public MessageQueueStats getStats() {
        MessageQueueStats stats = new MessageQueueStats();
        for (int i = 0; i < numPartitions; i++) {
            ThreadPoolStats threadPoolStats = executors.get(i).getStats();
            stats.add(queueName + "_" + i + "_" + "semaphoreQueueLength", threadPoolStats.getSemaphoreQueueLength());
            stats.add(queueName + "_" + i + "_" + "semaphorePermitsAvailable", threadPoolStats.getSemaphorePermitsAvailable());
            stats.add(queueName + "_" + i + "_" + "workerPoolSize", threadPoolStats.getWorkerPoolSize());
            stats.add(queueName + "_" + i + "_" + "taskQueueLength", threadPoolStats.getTaskQueueLength());
            stats.add(queueName + "_" + i + "_" + "taskQueueRemainingCapacity", threadPoolStats.getTaskQueueRemainingCapacity());
        }
        return stats;
    }
}
