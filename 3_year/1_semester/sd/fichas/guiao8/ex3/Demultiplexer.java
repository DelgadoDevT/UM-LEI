package guiao8.repeat.ex3;

import guiao8.repeat.ex2.TaggedConnection;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable  {
    private class Entry {
        final Condition cond = lock.newCondition();
        final ArrayDeque<byte[]> queue = new ArrayDeque<>();
    }

    private final TaggedConnection conn;
    private final ReentrantLock lock;
    private final Map<Integer, Entry> buf;
    private IOException exception = null;

    public Demultiplexer(TaggedConnection conn) {
        this.conn = conn;
        this.lock = new ReentrantLock();
        this.buf = new HashMap<>();
    }

    private Entry get(int tag) {
        Entry e = buf.get(tag);
        if (e == null) {
            e = new Entry();
            buf.put(tag, e);
        }
        return e;
    }

    public void start() {
        new Thread(() -> {
            try {
                while (true) {
                    TaggedConnection.Frame frame = conn.receive();
                    lock.lock();
                    try {
                        Entry e = get(frame.tag);
                        e.queue.add(frame.data);
                        e.cond.signal();
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (IOException e) {
                lock.lock();
                try {
                    exception = e;
                    buf.forEach((k, v) -> v.cond.signalAll());
                } finally {
                    lock.unlock();
                }
            }
        }).start();
    }

    public void send(TaggedConnection.Frame frame) throws IOException {
        conn.send(frame);
    }

    public void send(int tag, byte[] data) throws IOException {
        conn.send(tag, data);
    }

    public byte[] receive(int tag) throws IOException, InterruptedException {
        lock.lock();

        if (exception != null)
            throw exception;

        try {
            Entry e = get(tag);

            while(e.queue.isEmpty() && exception == null)
                e.cond.await();

            byte[] res = e.queue.poll();

            if (res != null)
                return res;
            else
                throw exception;

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        conn.close();
    }
}
