package guiao4.repeat;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Agreement {
    private int n;
    private int current;
    private int gen;
    private ReentrantLock lock;
    private Condition full;
    private int[] maxValues;
    private static final int MAX_GENERATIONS = 10000;

    Agreement(int n) {
        this.n = n;
        this.current = 0;
        this.gen = 0;
        this.lock = new ReentrantLock();
        this.full = this.lock.newCondition();
        this.maxValues = new int[MAX_GENERATIONS];
        for (int i = 0; i < MAX_GENERATIONS; i++) {
            maxValues[i] = Integer.MIN_VALUE;
        }
    }

    int await(int choice) throws InterruptedException {
        try {
            lock.lock();
            int myGen = this.gen;
            current += 1;

            maxValues[myGen] = Math.max(choice, maxValues[myGen]);

            if (current < n) {
                while (myGen == gen) {
                    full.await();
                }
                return maxValues[myGen];
            } else {
                System.out.println("Gen: " + myGen + " Max: " + maxValues[myGen]);

                current = 0;
                gen += 1;

                full.signalAll();

                return maxValues[myGen];
            }
        } finally {
            lock.unlock();
        }
    }
}