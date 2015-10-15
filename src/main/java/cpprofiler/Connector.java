package cpprofiler;

import java.nio.ByteBuffer;
import java.io.*;
import org.zeromq.ZMQ;
import message.Message.Node;

public class Connector {

  private ZMQ.Context context;
  private ZMQ.Socket socket;

  public Connector() {
    System.out.println("Connector initialized!");
    context = ZMQ.context(1);
    socket = context.socket(ZMQ.PUSH);
    
  }


  public void connectToSocket(int port) {
    socket.connect("tcp://localhost:" + port);
    System.out.println("connected over port: " + port);

  }

  public void disconnectFromSocket() {

    Node msg = Node.newBuilder()
      .setType(Node.MsgType.DONE)
      .build();

    sendOverSocket(msg);

    socket.close();
    context.term();
  }

  public void sendNode(int sid, int pid, int alt, int kids, Node.NodeStatus status, String label, String info) {

    Node node = Node.newBuilder()
      .setType(Node.MsgType.NODE)
      .setSid(sid)
      .setPid(pid)
      .setAlt(alt)
      .setKids(kids)
      .setStatus(status)
      .setLabel(label)
      .setInfo(info)
      .build();

    sendOverSocket(node);

  }

  public void restartGist(int rid) {

    Node msg = Node.newBuilder()
      .setType(Node.MsgType.START)
      .setRestartId(rid)
      .build();

    sendOverSocket(msg);
  }

  private void sendOverSocket(Node msg) {

    socket.send(msg.toByteArray(), 0);

  }
}