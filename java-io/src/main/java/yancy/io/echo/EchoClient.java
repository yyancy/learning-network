package yancy.io.echo;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class EchoClient {
  private final int port;
  private final String host;

  public EchoClient(int port, String host) {
    this.port = port;
    this.host = host;
  }

  private void start() {
    try (Socket socket = new Socket(host, port)) {
      while (true) {
        Scanner scanner = new Scanner(System.in);
        String message = scanner.next();
        System.out.println("your input message is: " + message);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        writer.println(message);
        writer.flush();
        String receivedMessage = reader.readLine();
        System.out.println("your received message is: " + receivedMessage);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new EchoClient(8888, "127.0.0.1").start();
  }
}
