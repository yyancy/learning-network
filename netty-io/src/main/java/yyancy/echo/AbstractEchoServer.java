package yyancy.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class AbstractEchoServer {
  private final int port;
  private List<ChannelHandler> handlers;

  public AbstractEchoServer(int port, List<ChannelHandler> handlers) {
    this.port = port;
    this.handlers = handlers;
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
              ChannelPipeline pipeline = ch.pipeline();
              for (ChannelHandler handler : handlers) {
                pipeline.addLast(handler);
              }
            }
          });

      ChannelFuture f = b.bind().sync();
      System.out.println(AbstractEchoServer.class.getName() + " started and listen on " + f.channel().localAddress());


      handlerServerChannel(this, f.channel());
      f.channel().closeFuture().sync();
    } finally {
      boss.shutdownGracefully().sync();
      worker.shutdownGracefully().sync();
    }
  }

  protected abstract void handlerServerChannel(AbstractEchoServer context, Channel channel);





}
