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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public interface Targets extends Remote
{
  /**
   * Ritorna il TARGET più vicino ad un punto specifico.
   * <p>Se la lista dei TARGET conosciuti è vuota, ritorna il punto stesso.
   *
   * @param point
   * @return Coordinates
   * @throws RemoteException
   */
  public Coordinates nearestTo(Coordinates point) throws RemoteException;

  /**
   * Scorre la lista degli obiettivi e rimuove quelli già catturati.
   *
   * @param targets Gli obiettivi freschi di LOOK
   * @throws RemoteException
   */
  public void compare(byte[] targets) throws RemoteException;

  public void remove(Coordinates target) throws RemoteException;
}
