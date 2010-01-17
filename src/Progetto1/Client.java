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
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
    List<Coordinates> targets;
    private Coordinates target;
    //private Coordinates nextTarget;

    public Client(String host, int port) throws IOException {
        this.socket = new Socket(InetAddress.getByName(host), port);
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
                Thread.sleep(130L);
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
                    this.targets = Collections.synchronizedList(new ArrayList<Coordinates>());

                    synchronized (this.targets) {
                        while (bb.hasRemaining()) {
                            Coordinates xy = new Coordinates(bb.getShort(), bb.getShort());
                            this.targets.add(xy);
                        }
                    }

                    TargetReceiver tr = new TargetReceiver(this.targets);
                    Thread t = new Thread(tr);
                    t.start();

                    Thread.sleep(100L);
                    
                    send(4);
                    // FIXME: buffer underflow?
                    Msg whereami = recv();
                    bb = ByteBuffer.wrap(whereami.data);

                    // RESPAWN!
                    target = new Coordinates(bb);

                    this.state = "moving";
                    break;
                case moving:
                    if (this.targets.size() > 0) {
                        findNearest();
                        send(2, target);
                        this.state = "seeking";
                    } else {
                        this.state = "pinging";
                    }
                    break;
                case seeking:
                    send(4);
                    Msg loc = recv();
                    bb = ByteBuffer.wrap(loc.data);
                    Coordinates pq = new Coordinates(bb);
                    if (pq.equals(target)) {
                        this.state = "grabbing";
                    }
                    break;
                case grabbing:
                    send(3);
                    Msg msg = recv();
                    //TODO: cosa ritorna qui?
                    this.state = "moving";
                    break;
                case pinging:
                    send(1);
                    System.out.print("PING? ");
                    Msg pong = recv();
                    if (pong.command == 64) {
                        System.out.println("PONG!");
                    }
                    this.state = "moving";
                    break;
                default:
                    System.out.println("FFFUUUUU-");
                    break;
            }
        }
    }

    private void send(int command, String TEAM) throws IOException {
        byte[] team = TEAM.getBytes();
        byte[] b = new byte[3 + team.length];
        //TODO: usare ByteBuffer (ma anche no)
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

    // invia il MOVE
    private void send(int command, Coordinates target) throws IOException {
        byte[] b = new byte[7];
        ByteBuffer bb = ByteBuffer.wrap(b);
        b[0] = (byte) command;
        bb.putShort(1, (short) 4);
        bb.putShort(3, target.getX());
        bb.putShort(5, target.getY());
        this.out.write(b);
    }

    private Msg recv() throws IOException {
        byte[] b = new byte[3];
        // TODO: ehm
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
        // TODO: facciamo tornare un ByteBuffer?
        return m;
    }

    //TODO: questo è da spostare in un thread a parte con i FUTURES!
    private void findNearest() throws IOException {
        int index = 0;

        if (this.targets.size() > 1) {
            Iterator i = this.targets.iterator();
            double nearestDistance = Double.MAX_VALUE;

            while (i.hasNext()) {
                Coordinates pq = (Coordinates) i.next();
                double distance = Math.pow((target.getX() - pq.getX()), 2) + Math.pow((target.getY() - pq.getY()), 2);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    index = this.targets.indexOf(pq);
                }
            }
        }

        synchronized (this.targets) {
            this.target = this.targets.remove(index);
        }

        InetAddress ia = InetAddress.getByName("226.0.0.0");
        byte[] msg = new byte[7];
        ByteBuffer bbm = ByteBuffer.wrap(msg);
        msg[0] = 70;
        bbm.putShort(1, (short) 2);
        bbm.putShort(3, target.getX());
        bbm.putShort(5, target.getY());
        int port = 4001;
        DatagramPacket dp = new DatagramPacket(msg, msg.length, ia, port);
        MulticastSocket ms = new MulticastSocket(port);
        ms.send(dp);
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
