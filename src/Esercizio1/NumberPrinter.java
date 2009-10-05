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
package Esercizio1;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
class NumberPrinter implements Runnable {

    private static int counter = 0;
    final int id = counter++;
    private int enne;

    NumberPrinter(int N) {
        enne = N;
    }

    public void run() {
        for (int i = 1; i <= enne; i++) {
            System.out.print(": " + i + " " + "[" + Thread.currentThread().getName() + "] :");
            Random r = new Random();
            try {
                Thread.sleep(r.nextInt(1000));
            } catch (InterruptedException ex) {
                Logger.getLogger(NumberPrinter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
