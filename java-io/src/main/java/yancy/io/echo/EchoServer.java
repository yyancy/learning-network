package yancy.io.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {

  private final Logger logger = LoggerFactory.getLogger(EchoServer.class);

  private final int port;


  public EchoServer(int port) {
    this.port = port;

  }

  public void start() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {

      logger.info("server is running. port={}", port);
      while (true) {
        Socket clientSock = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSock.getOutputStream()));
        String req = reader.readLine();
        System.out.printf("received message: %s\n", req);
        writer.println(req);
        writer.flush();
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static void main(String[] args) {
    new EchoServer(8888).start();
  }
}
