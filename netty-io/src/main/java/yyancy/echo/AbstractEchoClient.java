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
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class AbstractEchoClient {
  private final String host;
  private final int port;
  private List<ChannelHandler> handlers;

  public AbstractEchoClient(String host, int port, List<ChannelHandler> handlers) {
    this.host = host;
    this.port = port;
    this.handlers = handlers;
  }

  public abstract void handlerChannel(AbstractEchoClient context, Channel channel);

  public void start() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
          .channel(NioSocketChannel.class)
          .option(ChannelOption.TCP_NODELAY, true)
          .remoteAddress(new InetSocketAddress(host, port))
          .handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) {
              ChannelPipeline pipeline = ch.pipeline();
              for (ChannelHandler handler : AbstractEchoClient.this.handlers) {
                pipeline.addLast(handler);
              }
            }
          });
      ChannelFuture f = b.connect().sync();
      Channel channel = f.channel();
      handlerChannel(this, channel);
      System.out.println("close client...");
//      f.channel().closeFuture().sync();
    } finally {
      group.shutdownGracefully().sync();
    }
  }


}
