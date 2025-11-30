package com.pranjal.assign1;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe blocking queue with fair locking and timeout support.
 * 
 * Uses ReentrantLock with fair mode to prevent thread starvation.
 * Provides blocking operations (put/take) and timeout operations (offer/poll).
 * 
 * @param <T> the type of elements held in this queue
 * @author Pranjal
 * @version 2.1
 */
public class AdvancedBlockingQueue<T> {

    private final Queue<T> queue;
    private final int capacity;
    
    // Fair lock ensures FIFO ordering of waiting threads
    private final ReentrantLock lock = new ReentrantLock(true);
    
    // Separate conditions for space available and data available
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    /**
     * Constructs a blocking queue with the specified capacity.
     * 
     * @param capacity maximum number of elements, must be positive
     * @throws IllegalArgumentException if capacity is not positive
     */
    public AdvancedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Queue capacity must be positive, got: " + capacity);
        }
        this.capacity = capacity;
        this.queue = new LinkedList<>();
    }

    /**
     * Inserts the element, waiting if necessary for space.
     * 
     * @param item the element to add, must not be null
     * @throws InterruptedException if interrupted while waiting
     * @throws NullPointerException if item is null
     */
    public void put(T item) throws InterruptedException {
        if (item == null) {
            throw new NullPointerException("Cannot add null elements to queue");
        }
        
        lock.lockInterruptibly();
        try {
            while (queue.size() == capacity) {
                notFull.await();
            }
            queue.add(item);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head, waiting if necessary for an element.
     * 
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting
     */
    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (queue.isEmpty()) {
                notEmpty.await();
            }
            T item = queue.poll();
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts the element, waiting up to the specified time for space.
     * 
     * @param item the element to add, must not be null
     * @param timeout how long to wait before giving up
     * @param unit the time unit of the timeout
     * @return true if successful, false if timeout elapsed
     * @throws InterruptedException if interrupted while waiting
     * @throws NullPointerException if item is null
     */
    public boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
        if (item == null) {
            throw new NullPointerException("Cannot add null elements to queue");
        }
        
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (queue.size() == capacity) {
                if (nanos <= 0) return false;
                nanos = notFull.awaitNanos(nanos);
            }
            queue.add(item);
            notEmpty.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head, waiting up to the specified time.
     * 
     * @param timeout how long to wait before giving up
     * @param unit the time unit of the timeout
     * @return the head of this queue, or null if timeout elapsed
     * @throws InterruptedException if interrupted while waiting
     */
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (queue.isEmpty()) {
                if (nanos <= 0) return null;
                nanos = notEmpty.awaitNanos(nanos);
            }
            T item = queue.poll();
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the current number of elements in the queue.
     * 
     * @return the number of elements currently in the queue
     */
    public int getSize() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Returns the maximum capacity of the queue.
     * 
     * @return the maximum number of elements this queue can hold
     */
    public int getCapacity() {
        return capacity;
    }
}
