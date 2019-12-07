package yancy.io.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PoolEchoServer {

  private final Logger logger = LoggerFactory.getLogger(PoolEchoServer.class);

  private final int port;
  private EchoServiceExecutor echoServiceExecutor;

  public PoolEchoServer(int port) {
    this.port = port;
    echoServiceExecutor = new EchoServiceExecutor(1, 2, 1);
  }

  public void start() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {

      logger.info("server is running. port={}", port);
      while (true) {
        Socket clientSock = serverSocket.accept();
        Runnable task = () -> {
          try {
            handle(clientSock);
          } catch (IOException e) {
            logger.error(e.getMessage(), e);
          }
        };
        echoServiceExecutor.execute(task);
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
      System.out.printf(Thread.currentThread().getName() + "received message:  %s\n", req);
      writer.println(req);
      writer.flush();
    }
  }

  public static void main(String[] args) {
    new PoolEchoServer(8888).start();
  }


  /**
   * 伪异步IO模型
   */
  class EchoServiceExecutor {

    private ExecutorService executorService;

    public EchoServiceExecutor(int maxPoolSize, int queueSize) {
      this(Runtime.getRuntime().availableProcessors(), maxPoolSize, queueSize);
    }

    public EchoServiceExecutor(int corePoolSize, int maxPoolSize, int queueSize) {
      executorService =
          new ThreadPoolExecutor(corePoolSize,
              maxPoolSize, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize));
    }

    void execute(Runnable task) {
      executorService.execute(task);
    }
  }

}
