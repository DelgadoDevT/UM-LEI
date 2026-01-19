package guiao4.repeat;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Barrier2 {
    private int n;
    private int current;
    private int gen;
    private ReentrantLock lock;
    private Condition full;

    Barrier2(int n) {
        this.n = n;
        this.current = 0;
        this.gen = 0;
        this.lock = new ReentrantLock();
        this.full = this.lock.newCondition();
    }

    void await() throws InterruptedException {
        try {
            lock.lock();
            int myGen = this.gen;
            current += 1;
            if (current < n) {
                while (myGen == gen)
                    full.await();
            } else {
                current = 0;
                gen += 1;
                full.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
}
