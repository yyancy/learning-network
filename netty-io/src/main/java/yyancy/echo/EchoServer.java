package yyancy.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;


public class EchoServer {
  private final int port;

  public EchoServer(int port) {
    this.port = port;
  }

  public void start() throws Exception {
    EventLoopGroup boss = new NioEventLoopGroup();
    EventLoopGroup worker = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(boss, worker)
          .channel(NioServerSocketChannel.class)
          .localAddress(new InetSocketAddress(port))
//          .option(ChannelOption.SO_BACKLOG, 20)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
              ch.pipeline().addLast(new EchoServerHandler());
            }
          });

      ChannelFuture f = b.bind().sync();
      System.out.println(EchoServer.class.getName() +
          " started and listen on " + f.channel().localAddress());

      f.channel().closeFuture().sync();
    } finally {
      boss.shutdownGracefully().sync();
      worker.shutdownGracefully().sync();
    }
  }


  @ChannelHandler.Sharable
  public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    //    public volatile int count = 0;
    AtomicInteger count = new AtomicInteger(0);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
      count.incrementAndGet();
      ByteBuf buf = (ByteBuf) msg;
      byte[] req = new byte[buf.readableBytes()];
      buf.readBytes(req);
      String body = new String(req, StandardCharsets.UTF_8);
      System.out.println(count.get() + " Server received: " + body);
      ByteBuf writeBuf = Unpooled.copiedBuffer(body.getBytes());
      ctx.writeAndFlush(writeBuf);
//      System.out.println("Server received: " + msg);
//      ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
      ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
//          .addListener(ChannelFutureListener.CLOSE);
//      System.out.println("end.");
//      ctx.writeAndFlush("233");
//      ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
      cause.printStackTrace();
      ctx.close();
    }
  }


  public static void main(String[] args) throws Exception {
//    if (args.length != 1) {
//      System.err.println("Usage: " + EchoServer.class.getSimpleName() + " <port>");
//    }
    String _port = "8888";
    int port = Integer.parseInt(_port);
    new EchoServer(port).start();
  }

}
