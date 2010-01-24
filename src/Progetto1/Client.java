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
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class Client {

    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private String state;
    private boolean possible;
    private Coordinates target;

    private int prevCommand;
    private Targets stub;

    /**
     *
     * @param host
     * @param port
     */
    public Client(InetAddress host, int port) {
        try {
            this.socket = new Socket(host, port);
            this.out = this.socket.getOutputStream();
            this.in = this.socket.getInputStream();
            this.state = "registering";
            this.possible = true;

            // TODO: spostarlo nel main e fare scegliere l'indirizzo
            Registry registry = LocateRegistry.getRegistry("localhost");
            this.stub = (Targets) registry.lookup("Targets");
        } catch (NotBoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccessException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConnectException ex) {
            System.err.println(ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param args
     * @throws UnknownHostException
     * @throws RemoteException
     */
    public static void main(String[] args) throws UnknownHostException, RemoteException {
        InetAddress host = InetAddress.getLocalHost();
        int portaTCP = 4000;
        String squadra = "A-Team";
        try {
            squadra = args[0];
            host = InetAddress.getByName(args[1]);
            portaTCP = Integer.parseInt(args[2]);

        } catch (NumberFormatException e) {
            System.err.println("Numero di porta non valido, uso il default (" +
                    portaTCP + ").");
        } catch (ArrayIndexOutOfBoundsException e) {
        } catch (UnknownHostException e) {
            System.err.println("Nome host non valido, uso il default (" +
                    host + ").");
        }

        Client client = new Client(host, portaTCP);
        client.go(squadra);
        //System.out.println(host + " " + portaTCP + " " + squadra);
    }

    private void go(String squadra) throws RemoteException {
        while (possible) {
            try {
                // TODO: trovare il valore più basso della sleep()
                Thread.sleep(130L);
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            switch (State.valueOf(this.state)) {
                case registering:
                    send(7, squadra);
                    Msg id = recv();
                    this.prevCommand = id.command;
                    if (id.data.getShort() == -1) {
                        this.state = "fucked";
                    } else {
                        this.state = "peeking";
                    }
                    break;
                case peeking:
                    send(5);
                    Msg targetsMsg = recv();
                    this.prevCommand = targetsMsg.command;
                    while (targetsMsg.data.hasRemaining()) {
                        Coordinates xy = new Coordinates(targetsMsg.data.getShort(), targetsMsg.data.getShort());
                        this.stub.add(xy);
                    }
                    this.state = "whereami";
                    break;
                case whereami:
                    send(4);
                    Msg whereami = recv();
                    this.prevCommand = whereami.command;
                    this.target = new Coordinates(whereami.data);
                    this.state = "moving";
                    break;
                case moving:
                    // FIXME: race condition!
                    if (this.stub.hasMoreTargets()) {
                        this.target = stub.nearestTo(this.target);
                        send(2, this.target);
                        this.state = "seeking";
                    } else {
                        this.state = "pinging";
                    }
                    break;
                case seeking:
                    send(4);
                    Msg loc = recv();
                    this.prevCommand = loc.command;
                    Coordinates pq = new Coordinates(loc.data);
                    if (pq.equals(this.target)) {
                        this.state = "grabbing";
                    }
                    break;
                case grabbing:
                    send(3);
                    Msg msg = recv();
                    this.prevCommand = msg.command;
                    //System.err.println(msg.data.getShort());
                    this.state = "moving";
                    break;
                case pinging:
                    send(1);
                    Msg pong = recv();
                    if (pong.command != this.prevCommand) {
                        this.prevCommand = pong.command;
                        System.err.println("PING? PONG!");
                    }
                    this.state = "moving";
                    break;
                default:
                    System.err.println("FFFUUUUU-");
                    this.possible = false;
                    break;
            }
        }
    }

    /**
     * Invia il REGISTER.
     *
     * @param command Il comando di REGISTER.
     * @param squadra Il nome della squadra.
     */
    private void send(int command, String squadra) {
        byte[] team = squadra.getBytes();
        byte[] b = new byte[3 + team.length];
        b[0] = (byte) command;
        b[1] = (byte) ((team.length >> 8) & 0xFF);
        b[2] = (byte) (team.length & 0xFF);

        int j = 0;
        for (int i = 3; i < b.length; i++) {
            b[i] = team[j];
            j++;
        }
        try {
            this.out.write(b);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Invia un comando senza dati.
     *
     * @param command Il comando.
     */
    private void send(int command) {
        byte[] b = new byte[3];
        b[0] = (byte) command;
        try {
            this.out.write(b);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Invia il MOVE
     *
     * @param command Il comando di MOVE.
     * @param target Le coordinate.
     */
    private void send(int command, Coordinates target) {
        byte[] b = new byte[7];
        ByteBuffer bb = ByteBuffer.wrap(b);
        b[0] = (byte) command;
        bb.putShort(1, (short) 4);
        bb.putShort(3, target.getX());
        bb.putShort(5, target.getY());
        try {
            this.out.write(b);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Riceve un comando dal server.
     *
     * @return un Msg con comando e (opzionali) dati.
     */
    private Msg recv() {
        Msg msg = null;
        try {
            byte[] command = new byte[1];
            this.in.read(command);
            byte[] length = new byte[2];
            this.in.read(length);
            short n = ByteBuffer.wrap(length).getShort();
            byte[] data = new byte[n];
            this.in.read(data);
            msg = new Msg(command[0], ByteBuffer.wrap(data));
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        return msg;
    }

//    private void findNearest() throws IOException {
//        int index = 0;
//
//        if (this.targets.size() > 1) {
//            synchronized (this.targets) {
//                Iterator i = this.targets.iterator();
//                double nearestDistance = Double.MAX_VALUE;
//
//                while (i.hasNext()) {
//                    Coordinates pq = (Coordinates) i.next();
//                    double distance = Math.pow((target.getX() - pq.getX()), 2) + Math.pow((target.getY() - pq.getY()), 2);
//                    if (distance < nearestDistance) {
//                        nearestDistance = distance;
//                        index = this.targets.indexOf(pq);
//                    }
//                }
//            }
//        }
//
//        synchronized (this.targets) {
//            this.target = this.targets.remove(index);
//        }
//
//        InetAddress ia = InetAddress.getByName("226.0.0.0");
//        byte[] msg = new byte[7];
//        ByteBuffer bbm = ByteBuffer.wrap(msg);
//        msg[0] = 70;
//        bbm.putShort(1, (short) 2);
//        bbm.putShort(3, target.getX());
//        bbm.putShort(5, target.getY());
//        int port = 4001;
//        DatagramPacket dp = new DatagramPacket(msg, msg.length, ia, port);
//        MulticastSocket ms = new MulticastSocket(port);
//        ms.send(dp);
//    }

    /**
     * Gli stati del client.
     */
    public enum State {
        registering,
        pinging,
        moving,
        grabbing,
        whereami,
        peeking,
        seeking,
        fucked;
    }
}
