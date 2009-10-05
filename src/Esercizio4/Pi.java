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
package Esercizio4;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class Pi {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        double accuracy = 0.0000001;
        int time = 3000;

        if (args.length >= 2) {
            accuracy = Double.parseDouble(args[0]);
            time = Integer.parseInt(args[1]);
        }

        CalculatingThread ct = new CalculatingThread(accuracy);
        FutureTask ft = new FutureTask(ct);
        Thread t = new Thread(ft);
        System.out.println("Sto calcolando pi...");
        t.start();
        try {
            Thread.sleep(time);
        } catch (InterruptedException x) {
        }
        t.interrupt();
        try {
            System.out.println("Approssimazione di pi = " + ft.get());
        } catch (InterruptedException ex) {
            Logger.getLogger(Pi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(Pi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

