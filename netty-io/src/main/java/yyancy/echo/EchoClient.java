package yyancy.echo;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class EchoClient {
  private final String host;
  private final int port;

  public EchoClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void start() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
          .channel(NioSocketChannel.class)
          .remoteAddress(new InetSocketAddress(host, port))
          .handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch)
                throws Exception {
              ch.pipeline().addLast(
                  new EchoClientHandler());
            }
          });
      ChannelFuture f = b.connect().sync();
      f.channel().closeFuture().sync();
    } finally {
      group.shutdownGracefully().sync();
    }
  }

  public static void main(String[] args) throws Exception {
//    if (args.length != 2) {
//      System.err.println(
//          "Usage: " + EchoClient.class.getSimpleName() +
//              " <host> <port>");
//      return;
//    }
    // Parse options.
    final String host = "127.0.0.1";
    final int port = Integer.parseInt("8080");

    new EchoClient(host, port).start();
  }


  @ChannelHandler.Sharable
  public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
      System.out.println("connection()");
      ctx.write(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
      ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx,
                             ByteBuf in) {
//      System.out.println("Client received: " + ByteBufUtil
//          .hexDump(in.readBytes(in.readableBytes())));
      System.out.println("Client received: " + in.readBytes(in.readableBytes()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
      cause.printStackTrace();
      ctx.close();
    }
  }

}
