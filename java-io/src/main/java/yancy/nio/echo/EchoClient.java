package yancy.nio.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class EchoClient {

  private final Logger logger = LoggerFactory.getLogger(yancy.io.echo.EchoServer.class);

  private SocketChannel socketChannel;

  private Selector selector;

  private final String host;
  private final int port;

  private long timeOut = 10 * 1000;


  public EchoClient(String host, int port) {
    this.port = port;
    this.host = host;
    try {
      this.selector = Selector.open();
      this.socketChannel = SocketChannel.open();
      socketChannel.configureBlocking(false);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public void start() {


    try {
      doConnect();
      logger.info("connecting to {}. port={}", host, port);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }


    boolean stop = false;
    while (true) {
      try {
        int select = selector.select(1000 * 10);
        if (select == 0) {
          System.out.println("continue..");
          continue;
        }
        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        SelectionKey key = null;
        try {
          if (iterator.hasNext()) {
            key = iterator.next();
            iterator.remove();
            handle(key);
          }
        } catch (IOException e) {
          if (key != null) {
            System.out.println(key.toString() + " is stopping.");
            key.cancel();
            key.channel().close();
          }
        }

      } catch (IOException e) {
        e.printStackTrace();
      }

    }


  }

  private void doConnect() throws IOException {

    if (socketChannel.connect(new InetSocketAddress(host, port))) {
      socketChannel.register(selector, SelectionKey.OP_READ);
    } else {
      socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }
  }


  private void handle(SelectionKey key) throws IOException {
    if (key.isValid()) {
      SocketChannel sc = (SocketChannel) key.channel();
      if (key.isConnectable()) {
        if (sc.finishConnect()) {
          sc.register(selector, SelectionKey.OP_READ);
//          sc.register(selector, SelectionKey.OP_READ);

          doWrite(sc);
        } else {
          System.err.println("connecting error.");
        }
      } else if (key.isReadable()) {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        int rnt = sc.read(buf);

        if (rnt > 0) {
          buf.flip();
          byte[] bytes = new byte[buf.remaining()];
          buf.get(bytes);
          String body = new String(bytes, StandardCharsets.UTF_8);
          System.out.println(body);

          buf.flip();
          sc.write(buf);
        } else if (rnt < 0) { // 对方关闭连接
          key.cancel();
          sc.close();
        }
      }
      else if (key.isWritable()) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buf = ByteBuffer.allocate(1024);
        int rnt = clientChannel.read(buf);
      }
    }
  }

  private void doWrite(SocketChannel sc) {

  }

  public static void main(String[] args) {
    new EchoServer(9999).start();
  }

}
