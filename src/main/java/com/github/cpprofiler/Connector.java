package com.github.cpprofiler;

import java.nio.ByteBuffer;
import java.io.*;
import org.zeromq.ZMQ;
import com.github.cpprofiler.Message.Node;

public class Connector {

  private ZMQ.Context context;
  private ZMQ.Socket socket;

  public enum NodeStatus {
    SOLVED(0), FAILED(1), BRANCH(2), SKIPPED(6);
    private final int id;
    private NodeStatus(int id) { this.id = id; }
    public int getNumber() { return id; }
  }

  public class ExtendedNode {

    Node.Builder builder;
    Connector _connector;

    public ExtendedNode(Connector connector, int sid, int pid, int alt, int kids, NodeStatus status) {
      builder = Node.newBuilder();
      builder.setType(Node.MsgType.NODE).setSid(sid).setPid(pid).setAlt(alt).setKids(kids)
             .setStatus(Node.NodeStatus.valueOf(status.getNumber()));
      _connector = connector;
    }

    public ExtendedNode setLabel(String label) {
      builder.setLabel(label);
      return this;
    }

    public ExtendedNode setInfo(String info) {
      builder.setInfo(info);
      return this;
    }

    public void send() {
      Node msg = builder.build();
      _connector.sendOverSocket(msg);
    }
  }

  public Connector() {
    System.out.println("Connector initialized!");
    context = ZMQ.context(1);
    socket = context.socket(ZMQ.PUSH);
  }


  public void connect(int port) {
    socket.connect("tcp://localhost:" + port);
  }

  public void disconnect() {

    Node msg = Node.newBuilder()
      .setType(Node.MsgType.DONE)
      .build();

    sendOverSocket(msg);

    socket.close();
    context.term();
  }

  public ExtendedNode createNode(int sid, int pid, int alt, int kids, NodeStatus status) {

    ExtendedNode node = new ExtendedNode(this, sid, pid, alt, kids, status);

    return node;
  }

  public void sendNode(int sid, int pid, int alt, int kids, NodeStatus status, String label, String info) {

    Node node = Node.newBuilder()
      .setType(Node.MsgType.NODE)
      .setSid(sid)
      .setPid(pid)
      .setAlt(alt)
      .setKids(kids)
      .setStatus(Node.NodeStatus.valueOf(status.getNumber()))
      .setLabel(label)
      .setInfo(info)
      .build();

    sendOverSocket(node);

  }

  public void restart(int rid) {

    Node msg = Node.newBuilder()
      .setType(Node.MsgType.START)
      .setRestartId(rid)
      .build();

    sendOverSocket(msg);
  }

  public void restart() {
    restart(-1);
  }

  private void sendOverSocket(Node msg) {

    socket.send(msg.toByteArray(), 0);

  }
}