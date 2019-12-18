package yyancy.echo;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dongyang
 * @date 2019-12-17 12:55
 */
public class StickyBagEchoServer extends AbstractEchoServer {


  public StickyBagEchoServer(int port, List<ChannelHandler> handlers) {
    super(port, handlers);
  }


  @ChannelHandler.Sharable
  public static class EchoServerHandler extends ChannelInboundHandlerAdapter {

    //    public volatile int count = 0;
    AtomicInteger count = new AtomicInteger(0);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object body) {
      count.incrementAndGet();

      System.out.println(count.get() + " Server received: " + body);
      ctx.writeAndFlush(Unpooled.copiedBuffer(body + "\r\n", CharsetUtil.UTF_8));
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
    List<ChannelHandler> handlers = new ArrayList<>();
    handlers.add(new LineBasedFrameDecoder(8096));
    handlers.add(new StringDecoder());
    handlers.add(new EchoServerHandler());
    new StickyBagEchoServer(port, handlers).start();
  }
}
