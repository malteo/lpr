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
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Si occupa di aggiornare la lista dei TARGETS e condividerla con i client.
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class TargetsServer implements Runnable, Targets
{
  private final List<Coordinates> targets =
          Collections.synchronizedList(new ArrayList<Coordinates>());
  private final CountDownLatch latch;
  private final String reghost;
  private final String mshost;
  private final String name;

  TargetsServer(CountDownLatch l, ByteBuffer data, String reghost,
                String mshost, String name)
  {
    this.latch = l;

    while (data.hasRemaining())
    {
      Coordinates xy = new Coordinates(data.getShort(), data.getShort());
      this.targets.add(xy);
    }
    this.reghost = reghost;
    this.mshost = mshost;
    this.name = name;
  }

  public synchronized Coordinates nearestTo(Coordinates point)
          throws RemoteException
  {
    int index = 0;

    if (this.targets.isEmpty())
    {
      return point;
    }
    if (this.targets.size() > 1)
    {
      Iterator i = this.targets.iterator();
      double nearestDistance = Double.MAX_VALUE;

      while (i.hasNext())
      {
        Coordinates pq = (Coordinates) i.next();
        double distance = point.distance(pq);

        if (distance < nearestDistance)
        {
          nearestDistance = distance;
          index = this.targets.indexOf(pq);
        }
      }
    }
    return this.targets.remove(index);
  }

  public synchronized void compare(byte[] newTargetsArray)
          throws RemoteException
  {
    ByteBuffer newTargetsBB = ByteBuffer.wrap(newTargetsArray);
    ArrayList<Coordinates> newTargets = new ArrayList<Coordinates>();

    while (newTargetsBB.hasRemaining())
    {
      Coordinates xy =
              new Coordinates(newTargetsBB.getShort(), newTargetsBB.getShort());
      newTargets.add(xy);
      this.add(xy);
    }
    ListIterator i = this.targets.listIterator();

    while (i.hasNext())
    {
      Coordinates pq = (Coordinates) i.next();

      if (!newTargets.contains(pq))
      {
        i.remove();
      }
    }
  }

  public synchronized void remove(Coordinates xy) throws RemoteException
  {
    this.targets.remove(xy);
  }

  /**
   * Aggiunge un obiettivo alla lista, se non è già presente.
   *
   * @param target Il nuovo obiettivo
   */
  public synchronized void add(Coordinates target)
  {
    if (!this.targets.contains(target))
    {
      this.targets.add(target);
    }
  }

  private void fetchTargets()
  {
    try
    {
      InetAddress ia = InetAddress.getByName(mshost);
      MulticastSocket ms = new MulticastSocket(4001);
      ms.joinGroup(ia);
      byte[] msg = new byte[7];
      ByteBuffer bb = ByteBuffer.wrap(msg);

      while (true)
      {
        DatagramPacket dp = new DatagramPacket(msg, msg.length);
        ms.receive(dp);
        Coordinates xy = new Coordinates(bb.getShort(3), bb.getShort(5));
        this.add(xy);
      }
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(TargetsServer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (UnknownHostException ex)
    {
      Logger.getLogger(TargetsServer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IOException ex)
    {
      Logger.getLogger(TargetsServer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void run()
  {
    try
    {
      Targets stub = (Targets) UnicastRemoteObject.exportObject(this, 0);
      // FIXME: cambiare registro!
      Random r = new Random();
      Registry registry = LocateRegistry.createRegistry(1099);
      registry.rebind(name, stub);
      System.err.println(this.name + " Server ready.");
      latch.countDown();
      this.fetchTargets();
    }
    catch (AccessException ex)
    {
      Logger.getLogger(TargetsServer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (RemoteException ex)
    {
      Logger.getLogger(TargetsServer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
