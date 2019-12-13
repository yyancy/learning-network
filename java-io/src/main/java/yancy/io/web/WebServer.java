package yancy.io.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yancy.io.echo.EchoServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 一个能解析http请求的web服务器.
 * 使用一个连接一个线程模型
 */
public class WebServer {

  private final Logger logger = LoggerFactory.getLogger(EchoServer.class);

  private final int port;


  public WebServer(int port) {
    this.port = port;
  }

  public void start() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      logger.info("server is running . port={}", port);
      while (true) {
        Socket clientSock = serverSocket.accept();
        new Thread(() -> {
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
      if (req == null) {
        break;
      }
      String[] reqHeader = req.split(" ");
      if ("get".equalsIgnoreCase(reqHeader[0])) {
        unSupportMethod(reader, writer);
        break;
      }
      getHeader(reader);

      System.out.printf("received message: %s\n", req);
      writer.println(req);
      writer.flush();
    }

    logger.info("client close connection.");
  }

  private Map<String, String> getHeader(BufferedReader reader) {
    Map<String, String> headers = reader.lines()
        .filter(s -> !s.isEmpty())
        .map(s -> s.split(": "))
        .collect(Collectors.toMap((s) -> s[0], (s) -> s[1]));
    headers.forEach((k, v) -> System.out.println(k + ":" + v));
    return headers;
  }

  private void unSupportMethod(BufferedReader reader, PrintWriter writer) {
    writer.print("HTTP/1.1 405\r\n");
    String resp = "{\"message\":\"not support,开心233 (*^▽^*)\"}";

    writer.print("Content-Type: text/json;charset=UTF-8\r\n");
//    writer.print("Content-Length: " + (resp.getBytes().length) + "\r\n");
    writer.print("\r\n");
    writer.print(new Date().toString());
    writer.flush();
  }

  public static void main(String[] args) {
    new WebServer(80).start();
  }
}
