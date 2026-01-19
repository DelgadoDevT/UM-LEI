package guiao5.repeat;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class WarehouseEgoista {
    private Map<String, Product> map =  new HashMap<String, Product>();
    private ReentrantLock lock = new ReentrantLock();

    private class Product {
        private int quantity;
        private ReentrantLock lock;
        private Condition waitForProduct;

        private Product() {
            this.quantity = 0;
            this.lock = new ReentrantLock();
            this.waitForProduct = this.lock.newCondition();
        }

        private void addQuantity(int n) {
            lock.lock();
            try {
                quantity += n;
                waitForProduct.signalAll();
            } finally {
                lock.unlock();
            }
        }

        private void removeQuantity(int n) throws InterruptedException {
            lock.lock();
            try {
                while(quantity < n)
                    waitForProduct.await();
                quantity -= n;
            } finally {
                lock.unlock();
            }
        }
    }


    private Product get(String item) {
        lock.lock();
        try {
            Product p = map.get(item);
            if (p == null) {
                p = new Product();
                map.put(item, p);
            }
            return p;
        } finally {
            lock.unlock();
        }
    }

    public void supply(String item, int quantity) {
        Product p = get(item);
        p.addQuantity(quantity);
        System.out.println("Added " + quantity + " " + item);
    }

    public void consume(Set<String> items) throws InterruptedException {
        for (String s : items) {
            Product p = get(s);
            p.removeQuantity(1);
            System.out.println("Consumed 1 " + s);
        }
    }

}
