package yyancy.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * @author dongyang
 * @date 2019-12-13 17:44
 */
public class DefaultEchoClient extends AbstractEchoClient {
  public DefaultEchoClient(String host, int port, List<ChannelHandler> handlers) {
    super(host, port, handlers);
  }

  @Override
  public void handlerChannel(AbstractEchoClient context, Channel channel) {
    while (true) {
      System.out.println("输入q/Q退出:");
      Scanner scanner = new Scanner(System.in);
      scanner.useDelimiter("\n");
      String input = scanner.next();
      if ("q".equalsIgnoreCase(input)) {
        break;
      }
      channel.writeAndFlush(Unpooled.copiedBuffer(input, CharsetUtil.UTF_8));
    }
  }

  @ChannelHandler.Sharable
  public static class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
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
    public void channelRead0(ChannelHandlerContext ctx,
                             ByteBuf buf) {
      byte[] req = new byte[buf.readableBytes()];
      buf.readBytes(req);
      String body = new String(req, StandardCharsets.UTF_8);
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

    new DefaultEchoClient(host, port, Collections.singletonList(new EchoClientHandler())).start();
  }

}
