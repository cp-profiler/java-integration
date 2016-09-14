package com.github.cpprofiler;

import java.nio.ByteBuffer;
import java.io.*;
import java.net.*;
import com.github.cpprofiler.Message.Node;

public class Connector {

  private Socket clientSocket;

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

    public ExtendedNode setRestartId(int restart_id) {
      builder.setRestartId(restart_id);
      return this;
    }

    public ExtendedNode setThreadId(int thread_id) {
      builder.setThreadId(thread_id);
      return this;
    }

    public void send() throws IOException {
      Node msg = builder.build();
      _connector.sendOverSocket(msg);
    }
  }

  public Connector() {
    System.out.println("Connector initialized v1.3.0");
  }


  public void connect(int port) throws IOException {
    clientSocket = new Socket("localhost", port);
  }

  public void disconnect() throws IOException {

    Node msg = Node.newBuilder()
      .setType(Node.MsgType.DONE)
      .build();

    sendOverSocket(msg);

    clientSocket.close();
  }

  public ExtendedNode createNode(int sid, int pid, int alt, int kids, NodeStatus status) {

    ExtendedNode node = new ExtendedNode(this, sid, pid, alt, kids, status);

    return node;
  }

  public void sendNode(int sid, int pid, int alt, int kids,
                       NodeStatus status, String label,
                       String info) throws IOException {

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

  public void restart(int rid) throws IOException {
    restart("", rid);
  }

  public void restart(String file_name, int rid) throws IOException {

    Node msg = Node.newBuilder()
      .setType(Node.MsgType.START)
      .setLabel(file_name)
      .setRestartId(rid)
      .build();

    sendOverSocket(msg);
  }

  public void done() throws IOException {
    Node msg = Node.newBuilder()
      .setType(Node.MsgType.DONE)
      .build();

    sendOverSocket(msg);
  }

  private void sendOverSocket(Node msg) throws IOException {

    byte[] b = msg.toByteArray();

    int size = b.length;
    byte[] size_buffer = new byte[4];

    for (int i = 0; i < 4; i++) {
        size_buffer[i] = (byte)(size >>> (i * 8));
    }

    clientSocket.getOutputStream().write(size_buffer);

    clientSocket.getOutputStream().write(b);

  }
}