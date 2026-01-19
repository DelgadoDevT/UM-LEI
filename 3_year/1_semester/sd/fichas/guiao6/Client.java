package guiao6.repeat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            System.out.println("===== Client Starting =====");
            Socket s = new Socket("localhost", 12345);

            DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            Scanner scanner = new Scanner(System.in);

            while (true) {
                int op = scanner.nextInt();
                dos.writeInt(op);

                if (op == -1) {
                    dos.flush();
                    break;
                }

                int value = scanner.nextInt();
                dos.writeInt(value);

                dos.flush();

                int response = dis.readInt();
                System.out.println("Server Response: " + response);
            }

            double average = dis.readDouble();
            System.out.println("Average: " + average);

            double globalAverage = dis.readDouble();
            System.out.println("Global Average: " + globalAverage);

            dis.close();
            dos.close();
            s.close();

            System.out.println("===== Client Closing =====");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
