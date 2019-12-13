package yancy.aio.echo;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;


public class EchoServer {
  static class AsyncTimeServerHandler implements Runnable {

    private final int port;
    private CountDownLatch latch;
    private AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    public AsyncTimeServerHandler(int port) {
      this.port = port;
      try {
        asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
        asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
        System.out.println("The time server is start in port :" + port);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void run() {
      latch = new CountDownLatch(1);
      doAccept();
      try {
        latch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    private void doAccept() {
      asynchronousServerSocketChannel.accept(this, new AcceptCompletionHandler());
    }
  }

  /**
   * 监听客户端连接的回调函数
   */
  static class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler> {


    @Override
    public void completed(AsynchronousSocketChannel result, AsyncTimeServerHandler attachment) {
      // 再次监听客户的连接
      attachment.asynchronousServerSocketChannel.accept(attachment, this);

      ByteBuffer buffer = ByteBuffer.allocate(1024);
      result.read(buffer, buffer, new ReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, AsyncTimeServerHandler attachment) {
      exc.printStackTrace();
      attachment.latch.countDown();
    }
  }


  /**
   * 读操作完成的回调函数
   */
  static class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

    private final AsynchronousSocketChannel asynchronousSocketChannel;

    ReadCompletionHandler(AsynchronousSocketChannel asynchronousSocketChannel) {
      this.asynchronousSocketChannel = asynchronousSocketChannel;
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
      attachment.flip();
      byte[] body = new byte[attachment.remaining()];
      attachment.get(body);
      System.out.println("server received: " + new String(body, StandardCharsets.UTF_8));
      doWrite(body);
      ByteBuffer buffer = ByteBuffer.allocate(1024);
      asynchronousSocketChannel.read(buffer, buffer, this);
    }

    private void doWrite(byte[] body) {
      ByteBuffer buffer = ByteBuffer.allocate(1024);
      buffer.put(body);
      buffer.flip();
      asynchronousSocketChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
        @Override
        public void completed(Integer result, ByteBuffer attachment) {
          if (attachment.hasRemaining()) {
            asynchronousSocketChannel.write(attachment, attachment, this);
          }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
          try {
            asynchronousSocketChannel.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
      exc.printStackTrace();
    }
  }

  public static void main(String[] args) {
    int port = 8888;
    AsyncTimeServerHandler serverHandler = new AsyncTimeServerHandler(port);
    new Thread(serverHandler, "EchoServer").start();
  }
}
