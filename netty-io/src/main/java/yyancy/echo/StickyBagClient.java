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
import java.util.concurrent.TimeUnit;

/**
 * tcp粘包/粘包解决示例
 *
 * @author dongyang
 * @date 2019-12-13 17:49
 */
public class StickyBagClient extends AbstractEchoClient {
  public StickyBagClient(String host, int port, List<ChannelHandler> handlers) {
    super(host, port, handlers);
  }

  @Override
  public void handlerChannel(AbstractEchoClient context, Channel channel) {

    for (int i = 0; i < 100; i++) {
      channel.writeAndFlush(Unpooled.copiedBuffer("I am your master.\r\n", CharsetUtil.UTF_8));
    }
    try {
      TimeUnit.SECONDS.sleep(10);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }


  @ChannelHandler.Sharable
  public static class EchoClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
      System.out.println("connection()");
////      while (true) {
//      System.out.println("输入q/Q退出:");
//      Scanner scanner = new Scanner(System.in);
//
//      String input = scanner.next();
//      if ("q".equalsIgnoreCase(input)) {
////          break;
//      }
//      ctx.write(Unpooled.copiedBuffer(input, CharsetUtil.UTF_8));
//      ctx.flush();
//      }
//      ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks",
//          CharsetUtil.UTF_8));

    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object body) {
//      String body = new String(req, StandardCharsets.UTF_8);
      System.out.println("client received: " + body);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
      cause.printStackTrace();
      ctx.close();
    }
  }


  public static void main(String[] args) throws Exception {
    // Parse options.
    final String host = "127.0.0.1";
    final int port = Integer.parseInt("8888");
    List<ChannelHandler> handlers = new ArrayList<>();
    handlers.add(new LineBasedFrameDecoder(8096));
    handlers.add(new StringDecoder());
    handlers.add(new EchoClientHandler());

    new StickyBagClient(host, port, handlers).start();
  }
}
