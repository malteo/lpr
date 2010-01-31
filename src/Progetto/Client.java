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
package Progetto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
 * @see http://www.osnews.com/story/19266/WTFs_m
 */
public class Client
{
  private Socket socket;
  private OutputStream out;
  private InputStream in;
  private String state;
  private boolean possible;
  private Coordinates target;
  private Targets stub;
  private Coordinates whereami;

  /**
   *
   * @param host
   * @param port
   */
  public Client(InetAddress host, int port)
  {
    try
    {
      this.socket = new Socket(host, port);
      this.out = this.socket.getOutputStream();
      this.in = this.socket.getInputStream();
      this.state = "registering";
      this.possible = true;
    }
    catch (IOException ex)
    {
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   *
   * @param args
   * @throws UnknownHostException
   */
  public static void main(String[] args) throws UnknownHostException
  {
    // i valori di default
    InetAddress host = InetAddress.getLocalHost();
    int portaTCP = 4000;
    String squadra = "A-Team";
    String reghost = "localhost";
    String mcgroup = "226.0.0.0";

    try
    {
      squadra = args[0];
      host = InetAddress.getByName(args[1]);
      portaTCP = Integer.parseInt(args[2]);
      reghost = args[3];
      mcgroup = args[4];
    }
    catch (NumberFormatException e)
    {
      System.err.println("Numero di porta non valido, uso il default ("
                          + portaTCP + ").");
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
    }
    catch (UnknownHostException e)
    {
      System.err.println("Nome host non valido, uso il default ("
                          + host + ").");
    }
    Client client = new Client(host, portaTCP);
    client.go(squadra, reghost, mcgroup);
  }

  private void go(String squadra, String reghost, String mcgroup)
  {
    while (possible)
    {
      try
      {
        // non facciamoci chiamare spammer.
        Thread.sleep(150L);
        
        // in che stato siamo? neat trick per switchare su String.
        switch (State.valueOf(this.state))
        {
          case registering:
            send(7, squadra);
            Msg id = recv();

            /*
             * id.data.getShort() è il numero assegnato dal server al
             * giocatore registrato.
             */
            switch (id.data.getShort())
            {
              case -1:
                this.state = "fucked";
                break;
              case 0:
                this.state = "first";
                break;
              default:
                this.state = "whereami";
            }
            break;
          case first:
            send(5);
            Msg firstTargets = recv();

            /*
             * A CountDownLatch initialized with a count of one serves as a
             * simple on/off latch, or gate: all threads invoking await wait at
             * the gate until it is opened by a thread invoking
             * CountDownLatch.countDown.
             */
            CountDownLatch l = new CountDownLatch(1);

            /*
             * passo al TargetServer il latch inizializzato a 1, i
             * TARGETS ricevuti dal server e i parametri da linea di
             * comando (o i default)
             */
            TargetsServer ts = new TargetsServer(l, firstTargets.data, reghost,
                                                 mcgroup, squadra);
            Thread t = new Thread(ts);
            t.start();

            // aspetta il countDown() sul latch parte di TargetsServer
            l.await();
            this.state = "whereami";
            break;
          case whereami:
            send(4);
            Msg locat = recv();
            this.whereami = new Coordinates(locat.data);
            Registry registry = LocateRegistry.getRegistry(reghost);
            this.stub = (Targets) registry.lookup(squadra);
            this.state = "peeking";
            break;
          case peeking:
            send(5);
            Msg targetsMsg = recv();
            this.stub.compare(targetsMsg.data.array());
            this.state = "moving";
            break;
          case moving:
            this.target = stub.nearestTo(this.whereami);
            send(2, this.target);
            this.state = "seeking";
            break;
          case seeking:
            send(4);
            Msg loc = recv();
            this.whereami = new Coordinates(loc.data);

            if (this.whereami.equals(this.target))
            {
              this.state = "grabbing";
            }
            else
            {
              this.state = "peeking";
            }
            break;
          case grabbing:
            send(3);
            Msg msg = recv();
            this.stub.remove(this.target);
            this.state = "peeking";
            break;
          case pinging:
            send(1);
            Msg pong = recv();
            this.state = "moving";
            break;
          default:
            this.possible = false;
            System.err.println("FFFUUUUU-");
        }
      }
      catch (BufferUnderflowException e)
      {
        System.err.println("Probabilmente il server è uscito.");
        this.state = "fucked";
      }
      catch (NotBoundException ex)
      {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (AccessException ex)
      {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (RemoteException ex)
      {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (InterruptedException ex)
      {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  /**
   * Invia il REGISTER.
   * <p>
   * (è il primo pezzo di codice che ho scritto e l'ho voluto
   * lasciare così com'è)
   * </p>
   *
   * @param command Il comando di REGISTER.
   * @param squadra Il nome della squadra.
   */
  private void send(int command, String squadra)
  {
    byte[] team = squadra.getBytes();
    byte[] b = new byte[3 + team.length];
    b[0] = (byte) command;
    b[1] = (byte) ((team.length >> 8) & 0xFF);
    b[2] = (byte) (team.length & 0xFF);

    int j = 0;

    for (int i = 3; i < b.length; i++)
    {
      b[i] = team[j];
      j++;
    }
    try
    {
      this.out.write(b);
    }
    catch (IOException ex)
    {
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Invia un comando senza dati.
   *
   * @param command Il comando.
   */
  private void send(int command)
  {
    byte[] b = new byte[3];
    b[0] = (byte) command;

    try
    {
      this.out.write(b);
    }
    catch (IOException ex)
    {
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Invia il MOVE
   *
   * @param command Il comando di MOVE.
   * @param target Le coordinate.
   */
  private void send(int command, Coordinates target)
  {
    byte[] b = new byte[7];
    ByteBuffer bb = ByteBuffer.wrap(b);
    b[0] = (byte) command;
    bb.putShort(1, (short) 4);
    bb.putShort(3, target.getX());
    bb.putShort(5, target.getY());

    try
    {
      this.out.write(b);
    }
    catch (IOException ex)
    {
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Riceve un comando dal server.
   *
   * @return il Msg di risposta.
   */
  private Msg recv()
  {
    Msg msg = null;

    try
    {
      byte[] command = new byte[1];
      this.in.read(command);
      byte[] length = new byte[2];
      this.in.read(length);
      short n = ByteBuffer.wrap(length).getShort();
      byte[] data = new byte[n];
      this.in.read(data);
      msg = new Msg(command[0], ByteBuffer.wrap(data));
    }
    catch (IOException ex)
    {
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
    return msg;
  }

  /**
   * Il messaggio di risposta dal server al client.
   *
   */
  class Msg
  {
    public int command;
    public ByteBuffer data;

    /**
     * Crea il messaggio ricevuto dal server.
     *
     * @param command Il byte che identifica il comando inviato.
     * @param data Un ByteBuffer che contiene i dati.
     */
    public Msg(byte command, ByteBuffer data)
    {
      this.command = command;
      this.data = data;
    }
  }

  /**
   * Gli stati del client.
   *
   * @see http://www.xefer.com/2006/12/switchonstring
   */
  public enum State
  {
    registering,
    pinging,
    moving,
    grabbing,
    whereami,
    peeking,
    seeking,
    fucked,
    first;
  }
}
