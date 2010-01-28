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
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.CountDownLatch;
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
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param args
     * @throws RemoteException
     * @throws UnknownHostException
     * @throws InterruptedException 
     */
    public static void main(String[] args)
            throws RemoteException, UnknownHostException, InterruptedException {
        InetAddress host = InetAddress.getLocalHost();
        int portaTCP = 4000;
        String squadra = "A-Team";
        String tshost = "localhost";
        try {
            squadra = args[0];
            host = InetAddress.getByName(args[1]);
            portaTCP = Integer.parseInt(args[2]);
            tshost = args[3];
        } catch (NumberFormatException e) {
            System.err.println("Numero di porta non valido, uso il default (" +
                    portaTCP + ").");
        } catch (ArrayIndexOutOfBoundsException e) {
        } catch (UnknownHostException e) {
            System.err.println("Nome host non valido, uso il default (" +
                    host + ").");
        }

        Client client = new Client(host, portaTCP);
        client.go(squadra, tshost);
        //System.out.println(host + " " + portaTCP + " " + squadra);
    }

    private void go(String squadra, String tshost)
            throws RemoteException, InterruptedException {
        while (possible) {
            Thread.sleep(130L);

            switch (State.valueOf(this.state)) {
                case registering:
                    send(7, squadra);
                    Msg id = recv();
                    CountDownLatch l = null;
                    switch (id.data.getShort()) {
                        case -1:
                            this.state = "fucked";
                            break;
                        case 0:
                            l = new CountDownLatch(1);
                            Thread ts = new Thread(new TargetsServer(l));
                            ts.start();
                            break;
                        default:
                            l = new CountDownLatch(0);
                    }
                    l.await();
                    Registry registry = LocateRegistry.getRegistry(tshost);
                    try {
                        this.stub = (Targets) registry.lookup("Targets");
                    } catch (NotBoundException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (AccessException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    this.state = "peeking";
                    break;
                case peeking:
                    send(5);
                    Msg targetsMsg = recv();
                    while (targetsMsg.data.hasRemaining()) {
                        Coordinates xy = new Coordinates(targetsMsg.data.getShort(), targetsMsg.data.getShort());
                        this.stub.add(xy);
                    }
                    this.state = "whereami";
                    break;
                case whereami:
                    send(4);
                    Msg whereami = recv();
                    this.target = new Coordinates(whereami.data);
                    this.state = "moving";
                    break;
                case moving:
                    this.target = stub.nearestTo(this.target);
                    send(2, this.target);
                    this.state = "seeking";
                    break;
                case seeking:
                    try {
                        send(4);
                        Msg loc = recv();
                        Coordinates pq = new Coordinates(loc.data);
                        if (pq.equals(this.target)) {
                            this.state = "grabbing";
                        }
                    } catch (BufferUnderflowException e) {
                        // Probabilmente il server è uscito.
                        this.state = "fucked";
                    }
                    break;
                case grabbing:
                    send(3);
                    Msg msg = recv();
                    //System.err.println(msg.data.getShort());
                    this.state = "moving";
                    break;
                case pinging:
                    send(1);
                    Msg pong = recv();
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

    /**
     * Il messaggio di risposta dal server al client.
     *
     */
    private class Msg {

        public int command;
        public ByteBuffer data;

        /**
         * Crea il messaggio ricevuto dal server.
         *
         * @param command Il byte che identifica il comando inviato.
         * @param data Un ByteBuffer che contiene i dati.
         */
        public Msg(byte command, ByteBuffer data) {
            this.command = command;
            this.data = data;
        }
    }

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
