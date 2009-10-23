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
package Esercitazione2;

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class Pi {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Pi accuracy time");
            return;
        }
        double accuracy = Double.parseDouble(args[0]);
        int time = Integer.parseInt(args[1]);

        CalculatingThread pi = new CalculatingThread(accuracy);
        Thread t = new Thread(pi);
        System.out.println("Sto calcolando pi...");
        t.start();
        try {
            Thread.sleep(time);
        } catch (InterruptedException x) {
        }
        t.interrupt();
    }
}
