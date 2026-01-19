package guiao6.repeat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

class Register {
    private int total = 0;
    private int num_ops = 0;
    private ReentrantLock lock = new ReentrantLock();

    public void addNumbers(int increment) {
        lock.lock();
        try {
            total += increment;
            num_ops += 1;
        } finally {
            lock.unlock();
        }
    }

    public double getAverage() {
        lock.lock();
        try {
            if (num_ops > 0)
                return (double) total / num_ops;
            else
                return 0;
        } finally {
            lock.unlock();
        }
    }
}

class ServerWorker implements Runnable {
    private Socket s;
    private Register r;

    public ServerWorker(Socket s, Register r) {
        this.s = s;
        this.r = r;
    }

    @Override
    public void run() {
        try {
            int total = 0;
            int length = 0;
            DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));

            int op;
            while ((op = dis.readInt()) != -1) {
                int value = dis.readInt();
                length += 1;
                r.addNumbers(value);
                switch(op) {
                    case 1:
                        total += value;
                        break;
                    case 2:
                        total -= value;
                        break;
                    case 3:
                        total *= value;
                        break;
                    case 4:
                        total /= value;
                        break;
                }

                dos.writeInt(total);
                dos.flush();
            }

            if (length > 0)
                dos.writeDouble((double) total / length);
            else
                dos.writeDouble(0);

            dos.writeDouble(r.getAverage());

            dos.flush();

            dis.close();
            dos.close();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class Server {
    public static void main(String[] args) throws IOException {
        System.out.println("===== Server Starting =====");
        ServerSocket ss = new ServerSocket(12345);
        Register r = new Register();

        while (true) {
            Socket s = ss.accept();
            (new Thread(new ServerWorker(s, r))).start();
        }
    }
}
