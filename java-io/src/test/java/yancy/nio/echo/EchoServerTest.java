package yancy.nio.echo;


import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class EchoServerTest {

  @Test
  public void sendBufTest() {
    ByteBuffer allocate = ByteBuffer.allocate(1024);
    ByteBuffer target = ByteBuffer.allocate(1024);
    allocate.put("hello".getBytes());



    allocate.flip();
    target.put(allocate);
    allocate.flip();
    allocate.put("world".getBytes());
    allocate.flip();

    target.put(allocate);
    EchoServer.SendBuff sendBuff = new EchoServer.SendBuff();
    sendBuff.put(allocate);

//    allocate.rewind();
//    allocate.put("world".getBytes());
//    sendBuff.put(allocate);


    String string = new String(target.array(), StandardCharsets.UTF_8);
    System.out.println(string + ".");
  }

}
