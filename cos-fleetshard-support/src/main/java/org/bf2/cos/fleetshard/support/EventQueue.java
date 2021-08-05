package org.bf2.cos.fleetshard.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EventQueue<T extends Comparable<T>, R> {
    private final Logger logger;
    private final ReentrantLock lock;
    private final Condition condition;
    private final Set<T> events;
    private volatile boolean poison;

    public EventQueue() {
        this.logger = LoggerFactory.getLogger(getClass());
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
        this.poison = false;
        this.events = new HashSet<>();
    }

    public int size() {
        this.lock.lock();

        try {
            return this.events.size();
        } finally {
            this.lock.unlock();
        }
    }

    public boolean isPoisoned() {
        this.lock.lock();

        try {
            return poison;
        } finally {
            this.lock.unlock();
        }
    }

    public void submitPoisonPill() {
        this.lock.lock();

        try {
            this.poison = true;
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    public void submit(T element) {
        Objects.requireNonNull(element, "Element must not be null");

        this.lock.lock();

        try {
            this.events.add(element);
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    public Collection<R> poll(long time, TimeUnit unit) throws InterruptedException {
        this.lock.lock();

        try {
            if (events.isEmpty() && !poison) {
                this.condition.await(time, unit);
            }

            Collection<R> answer;

            if (poison) {
                answer = collectAll();
                poison = false;
            } else if (events.isEmpty()) {
                return Collections.emptyList();
            } else {
                answer = collectAll(events);
            }

            events.clear();

            logger.debug("TaskQueue: elements={}", answer.size());

            return answer;
        } finally {
            this.lock.unlock();
        }
    }

    protected abstract Collection<R> collectAll();

    protected abstract Collection<R> collectAll(Collection<T> elements);
}