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
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class TargetReceiver implements Runnable {

    private final List<Coordinates> targets;

    TargetReceiver(List<Coordinates> targets) {
        this.targets = Collections.synchronizedList(targets);
    }

    public void run() {
        try {
            InetAddress ia = InetAddress.getByName("226.0.0.0");
            MulticastSocket ms = new MulticastSocket(4001);
            ms.joinGroup(ia);
            byte[] msg = new byte[7];
            ByteBuffer bb = ByteBuffer.wrap(msg);

            while (true) {
                DatagramPacket dp = new DatagramPacket(msg, msg.length);
                ms.receive(dp);
                Coordinates xy = new Coordinates(bb.getShort(3), bb.getShort(5));

                synchronized (this.targets) {
                    switch (msg[0]) {
                        case 67:
                            this.targets.add(xy);
                            break;
                        case 70:
                            this.targets.remove(xy);
                            break;
                        default:
                            System.err.println("FFFFFUUUUUUU-");
                    }
                }
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(TargetReceiver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TargetReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
