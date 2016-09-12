package com.github.cpprofiler;

import java.nio.ByteBuffer;
import java.io.*;
import java.net.*;
import com.github.cpprofiler.Message.Node;

public class Connector {

  private Socket clientSocket;
  private boolean _connected = false;

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

    public void send() {
      Node msg = builder.build();
      _connector.sendOverSocket(msg);
    }
  }

  public Connector() {
    System.out.println("Connector initialized v1.2.0");
  }


  public void connect(int port) {
    try {
      clientSocket = new Socket("localhost", port);
      _connected = true;
    } catch (IOException e) {
      System.err.println("couldn't connect to profiler; running solo");
    }
  }

  public void disconnect() {

    if (!_connected) return;

    Node msg = Node.newBuilder()
      .setType(Node.MsgType.DONE)
      .build();

    sendOverSocket(msg);

    try {
      clientSocket.close();
    } catch (IOException e) {
      System.err.println("Caught IOException 2: " + e.getMessage());
    }
  }

  public ExtendedNode createNode(int sid, int pid, int alt, int kids, NodeStatus status) {

    ExtendedNode node = new ExtendedNode(this, sid, pid, alt, kids, status);

    return node;
  }

  public void sendNode(int sid, int pid, int alt, int kids, NodeStatus status, String label, String info) {

    if (!_connected) return;

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
    restart("", rid);
  }

  public void restart(String file_name) {
    restart(file_name, -1);
  }

  private void restart(String file_name, int rid) {

    if (!_connected) return;

    Node msg = Node.newBuilder()
      .setType(Node.MsgType.START)
      .setLabel(file_name)
      .setRestartId(rid)
      .build();

    sendOverSocket(msg);
  }

  public void done() {
    Node msg = Node.newBuilder()
      .setType(Node.MsgType.DONE)
      .build();

    sendOverSocket(msg);
  }

  private void sendOverSocket(Node msg) {

    if (!_connected) return;

    try {

      byte[] b = msg.toByteArray();

      int size = b.length;
      byte[] size_buffer = new byte[4];

      for (int i = 0; i < 4; i++) {
          size_buffer[i] = (byte)(size >>> (i * 8));
      }

      clientSocket.getOutputStream().write(size_buffer);

      clientSocket.getOutputStream().write(b);

    } catch (IOException e) {
      System.err.println("Caught IOException 3: " + e.getMessage());
    }

  }
}