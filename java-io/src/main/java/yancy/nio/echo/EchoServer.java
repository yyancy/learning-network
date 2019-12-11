package yancy.nio.echo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class EchoServer {

  private final Logger logger = LoggerFactory.getLogger(yancy.io.echo.EchoServer.class);

  private final int port;

  private Selector selector;

  static class SendBuff {
    ByteBuffer buf;
    int offset = 0;
    int length = 1024;
    boolean hasValue = false;

    public SendBuff() {
      this.buf = ByteBuffer.allocate(1024);
    }

    public SendBuff put(ByteBuffer buf) {
      this.buf.put(buf);
      offset = buf.remaining();
      hasValue = true;
      return this;
    }
  }

  private Map<SocketChannel, SendBuff> sendBufTable = new HashMap<>();


  public EchoServer(int port) {
    this.port = port;

    try {
      this.selector = Selector.open();
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public void start() {

    ServerSocketChannel serverChannel;
    try {
      serverChannel = ServerSocketChannel.open();
      serverChannel.configureBlocking(false);
      serverChannel.socket().bind(new InetSocketAddress(port), 1024);
      serverChannel.register(selector, SelectionKey.OP_ACCEPT);
      logger.info("server is running. port={}", port);
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
        Set<SelectionKey> selectionKeys = selector.selectedKeys();

        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        SelectionKey key = null;
        try {
          if (iterator.hasNext()) {
            key = iterator.next();
            iterator.remove();
            handle(key);
          }
        } catch (IOException e) {
          if (key != null) {
            System.out.println(key.toString() + " is stopping");
            key.cancel();
            key.channel().close();
          }
        }

      } catch (IOException e) {
        e.printStackTrace();
      }

    }


  }


  private void handle(SelectionKey key) throws IOException {
    if (key.isValid()) {
      if (key.isAcceptable()) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = serverSocketChannel.accept();
        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
//        accept.register(selector, SelectionKey.OP_WRITE); 一旦开启,不能再用原生的write方法写入.
      } else if (key.isReadable()) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buf = ByteBuffer.allocate(1024);
        int rnt = clientChannel.read(buf);

        if (rnt > 0) {
          buf.flip();
          byte[] bytes = new byte[buf.remaining()];
          buf.get(bytes);
          String body = new String(bytes, StandardCharsets.UTF_8);
          System.out.println(body);

//          ByteBuffer wbuf = ByteBuffer.allocate(1024);
//          wbuf.put(bytes);
//          wbuf.flip();


        } else if (rnt < 0) { // 对方关闭连接
          key.cancel();
          clientChannel.close();
        }
      } else if (key.isWritable()) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buf = ByteBuffer.allocate(1024);
        int rnt = clientChannel.read(buf);
      }
    }
  }

  public static void main(String[] args) {
    new EchoServer(9999).start();
  }

}
