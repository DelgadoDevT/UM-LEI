package guiao8.repeat.ex2;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable  {
    public static class Frame {
        public int tag;
        public byte[] data;

        public Frame(int tag, byte[] data) {
            this.tag = tag;
            this.data = data;
        }
    }

    private ReentrantLock sendLock;
    private ReentrantLock receiveLock;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public TaggedConnection(Socket s) throws IOException {
        this.sendLock = new ReentrantLock();
        this.receiveLock = new ReentrantLock();
        this.socket = s;
        this.dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void send(Frame frame) throws IOException {
        this.send(frame.tag, frame.data);
    }

    public void send(int tag, byte[] data) throws IOException {
        sendLock.lock();
        try {
            dos.writeInt(tag);
            dos.writeInt(data.length);
            dos.write(data);
            dos.flush();
        } finally {
            sendLock.unlock();
        }
    }

    public Frame receive() throws IOException {
        receiveLock.lock();
        try {
            int tag = dis.readInt();
            byte[] data = new byte[dis.readInt()];
            dis.readFully(data);
            return new Frame(tag, data);
        } finally {
            receiveLock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        dis.close();
        dos.close();
        socket.close();
    }
}
