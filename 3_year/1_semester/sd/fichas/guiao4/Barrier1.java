package guiao4.repeat;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Barrier1 {
    private int n;
    private int current;
    private ReentrantLock lock;
    private Condition full;

    Barrier1(int n) {
       this.n = n;
       this.current = 0;
       this.lock = new ReentrantLock();
       this.full = this.lock.newCondition();
    }

    void await() throws InterruptedException {
        try {
            lock.lock();
            current += 1;
            if (current < n) {
                while (current < n)
                    full.await();
            } else {
                full.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
}
