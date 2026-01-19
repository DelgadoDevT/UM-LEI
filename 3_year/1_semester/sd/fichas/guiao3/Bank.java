package guiao3.repeat;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Bank {

    private static class Account {
        private int balance;
        private ReentrantReadWriteLock.WriteLock writeLock;
        private ReentrantReadWriteLock.ReadLock readLock;

        Account(int balance) {
            this.balance = balance;
            ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
            this.writeLock = lock.writeLock();
            this.readLock = lock.readLock();
        }

        int balance() {
            try {
                readLock.lock();
                return balance;
            } finally {
                readLock.unlock();
            }
        }

        boolean deposit(int value) {
            try {
                writeLock.lock();
                balance += value;
                return true;
            } finally {
                writeLock.unlock();
            }
        }

        boolean withdraw(int value) {
            try {
                writeLock.lock();
                if (value > balance)
                    return false;
                balance -= value;
                return true;
            } finally {
                writeLock.unlock();
            }
        }
    }

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;
    private ReentrantReadWriteLock l = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.WriteLock writeLock = l.writeLock();
    private ReentrantReadWriteLock.ReadLock readLock = l.readLock();

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        try {
            writeLock.lock();
            int id = nextId;
            nextId += 1;
            map.put(id, c);
            return id;
        } finally {
            writeLock.unlock();
        }
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        try {
            writeLock.lock();
            c = map.remove(id);
        } finally {
            writeLock.unlock();
        }

        if (c == null)
            return 0;
        return c.balance();
    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        try {
            readLock.lock();
            c = map.get(id);
        } finally {
            readLock.unlock();
        }

        if (c == null)
            return 0;
        return c.balance();
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c;
        try {
            readLock.lock();
            c = map.get(id);
        } finally {
            readLock.unlock();
        }

        if (c == null)
            return false;
        return c.deposit(value);
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account c;
        try {
            readLock.lock();
            c = map.get(id);
        } finally {
            readLock.unlock();
        }

        if (c == null)
            return false;
        return c.withdraw(value);
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account fromAcc, toAcc;

        try {
            readLock.lock();
            fromAcc = map.get(from);
            toAcc = map.get(to);

            if (fromAcc == null || toAcc == null)
                return false;

            int c1 = Math.min(from, to);

            Account firstAcc = (from == c1) ? fromAcc : toAcc;
            Account secondAcc = (from == c1) ? toAcc : fromAcc;

            try {
                firstAcc.writeLock.lock();
                secondAcc.writeLock.lock();
            } finally {
                readLock.unlock();
            }
        } catch (Exception e) {
            return false;
        }

        try {
            if (fromAcc.withdraw(value)) {
                toAcc.deposit(value);
                return true;
            }
            return false;
        } finally {
            fromAcc.writeLock.unlock();
            toAcc.writeLock.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        List<Account> accounts = new ArrayList<>();

        try {
            readLock.lock();
            for (int i : ids) {
                Account t = map.get(i);
                if (t == null) {
                    for (Account a : accounts) {
                        a.readLock.unlock();
                    }
                    return 0;
                }
                t.readLock.lock();
                accounts.add(t);
            }
        } finally {
            readLock.unlock();
        }

        try {
            int total = 0;
            for (Account e : accounts) {
                total += e.balance;
            }
            return total;
        } finally {
            for (Account e : accounts) {
                e.readLock.unlock();
            }
        }
    }
}