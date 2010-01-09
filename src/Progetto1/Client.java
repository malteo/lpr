/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details:
 * http://www.gnu.org/licenses/gpl.txt
 */
package Progetto1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class Client {

  private static final String SERVER = "localhost";
  private static final int PORT = 4000;
  private static final String TEAM = "A-Team";
  private Socket socket;
  private OutputStream out;
  private InputStream in;
  private String state;
  private ArrayList<short[]> targets;
  short x;
  short y;
  private short[] coord;

  public Client(String host, int port) throws IOException {
    try {
      this.socket = new Socket(InetAddress.getByName(host), port);
    } catch (UnknownHostException ex) {
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
    this.out = this.socket.getOutputStream();
    this.in = this.socket.getInputStream();
    this.state = "registering";
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    Client client = new Client(SERVER, PORT);
    client.go(TEAM);
  }

  private void go(String TEAM) throws IOException, InterruptedException {

    ByteBuffer bb;

    while (true) {
      try {
        Thread.sleep(150L);
      } catch (InterruptedException ex) {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
      }

      switch (State.valueOf(this.state)) {
        case registering:
          send(7, TEAM);
          Msg id = recv();
          bb = ByteBuffer.wrap(id.data);
          System.out.println("YOURID: " + bb.getShort());
          this.state = "looking";
          break;
        case looking:
          send(5);
          Msg m = recv();
          bb = ByteBuffer.wrap(m.data);
          //FIXME: questi target finiscono!
          this.targets = new ArrayList<short[]>();
          while (bb.hasRemaining()) {
            short[] xy = new short[2];
            xy[0] = bb.getShort();
            xy[1] = bb.getShort();
            this.targets.add(xy);
          }
          this.state = "moving";
          break;
        case moving:
          coordinate();
          send(2, 4, this.x, this.y);
          this.state = "seeking";
          break;
        case seeking:
          send(4);
          Msg loc = recv();
          bb = ByteBuffer.wrap(loc.data);
          if ((bb.getShort() == this.x) && (bb.getShort() == this.y)) {
            this.state = "grabbing";
          }
          break;
        case grabbing:
          send(3);
          this.state = "moving";
          break;
        case pinging:
          send(1);
          System.out.print("PING? ");
          Msg pong = recv();
          if (pong.command == 64) {
            System.out.println("PONG!");
          }
          Thread.sleep(800L);
        default:
          System.out.println("FFFUUUUU-");
          break;
      }
    }
  }

  private void send(int command, String TEAM) throws IOException {
    byte[] team = TEAM.getBytes();
    byte[] b = new byte[3 + team.length];
    b[0] = (byte) command;
    b[1] = (byte) ((team.length >> 8) & 0xFF);
    b[2] = (byte) (team.length & 0xFF);

    int j = 0;
    for (int i = 3; i < b.length; i++) {
      b[i] = team[j];
      j++;
    }

    this.out.write(b);
  }

  private void send(int command) throws IOException {
    byte[] b = new byte[3];
    b[0] = (byte) command;
    this.out.write(b);
  }

  private void send(int command, int len, short x, short y) throws IOException {
    byte[] b = new byte[7];
    ByteBuffer bb = ByteBuffer.wrap(b);
    b[0] = (byte) command;
    bb.putShort(1, (short) len);
    bb.putShort(3, x);
    bb.putShort(5, y);
    this.out.write(b);
  }

  private Msg recv() throws IOException {
    byte[] b = new byte[3];
    // TODO: antani
    int r = this.in.read(b);
    if (r == -1) {
      throw new IOException("connection closed");
    }
    if (r != 3) {
      throw new IOException("bad command length");
    }
    byte[] len = new byte[2];
    System.arraycopy(b, 1, len, 0, len.length);
    ByteBuffer bb = ByteBuffer.wrap(len);
    short n = bb.getShort();
    byte[] data = new byte[n];
    r = this.in.read(data);
    if (r != n) {
      throw new IOException("bad data length");
    }
    Msg m = new Msg(b[0], data);

    return m;
  }

  private void coordinate() {
    int target;
    Random r = new Random();

    target = r.nextInt(this.targets.size());

    if (Arrays.equals(this.coord, this.targets.get(target)))
      System.err.println("YES");

    this.coord = this.targets.remove(target);

    System.err.println("x = " + this.coord[0] + "; y = " + this.coord[1]);
    this.x = this.coord[0];
    this.y = this.coord[1];
  }

  public enum State {

    registering, pinging, moving, grabbing, GETPOS, looking, LOAD, seeking;
  }

  private class Msg {

    public static final byte PING = 1;
    public static final byte MOVE = 2;
    public static final byte GRAB = 3;
    public static final byte GETPOS = 4;
    public static final byte LOOK = 5;
    public static final byte LOAD = 6;
    public static final byte REGISTER = 7;
    public static final byte PONG = 64;
    public static final byte LOC = 65;
    public static final byte YOURLOAD = 66;
    public static final byte FRUITS = 67;
    public static final byte GRABRESULT = 68;
    public static final byte YOURID = 69;
    public int command;
    public byte[] data;

    public Msg(byte paramByte, byte[] paramArrayOfByte) {
      this.command = paramByte;
      this.data = paramArrayOfByte;
    }
  }
}
