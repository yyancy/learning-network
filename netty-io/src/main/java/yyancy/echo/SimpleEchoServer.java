package yyancy.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单的echo服务器示例
 *
 * @author dongyang
 * @date 2019-12-17 12:55
 */
public class SimpleEchoServer extends AbstractEchoServer {


  public SimpleEchoServer(int port, List<ChannelHandler> handlers) {
    super(port, handlers);
  }


  @ChannelHandler.Sharable
  public static class EchoServerHandler extends ChannelInboundHandlerAdapter {

    //    public volatile int count = 0;
    AtomicInteger count = new AtomicInteger(0);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
      count.incrementAndGet();
//      System.out.println("233");
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


  @Override
  protected void handlerServerChannel(AbstractEchoServer context, Channel channel) {

  }

  public static void main(String[] args) throws Exception {
    String _port = "8888";
    int port = Integer.parseInt(_port);

    new SimpleEchoServer(port, Collections.singletonList(new EchoServerHandler())).start();
  }
}
