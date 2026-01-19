package guiao8.repeat.ex1;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class FramedConnection implements AutoCloseable {
    private ReentrantLock sendLock;
    private ReentrantLock receiveLock;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public FramedConnection(Socket socket) throws IOException {
        this.sendLock = new ReentrantLock();
        this.receiveLock = new ReentrantLock();
        this.socket = socket;
        this.dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void send(byte[] bytes) throws IOException {
        sendLock.lock();
        try {
            dos.writeInt(bytes.length);
            dos.write(bytes);
            dos.flush();
        } finally {
            sendLock.unlock();
        }
    }

    public byte[] receive() throws IOException {
        receiveLock.lock();
        try {
            byte[] data;

            data = new byte[dis.readInt()];
            dis.readFully(data);

            return data;
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
