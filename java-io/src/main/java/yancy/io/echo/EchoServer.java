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
        new Thread(()->{
          try {
            handle(clientSock);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }).start();
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }


  private void handle(Socket clientSock) throws IOException {
    while (true) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSock.getOutputStream()));
      String req = reader.readLine();
      System.out.printf("received message: %s\n", req);
      writer.println(req);
      writer.flush();
    }
  }

  public static void main(String[] args) {
    new EchoServer(8888).start();
  }
}
