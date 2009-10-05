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

import javax.swing.JOptionPane;

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class NPrint {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int K = Integer.parseInt(JOptionPane.showInputDialog("Numero di thread:"));
        int N = Integer.parseInt(JOptionPane.showInputDialog("Numero di numeri:"));

        for (int i = 1; i <= K; i++) {
            NumberPrinter np = new NumberPrinter(N);
            Thread t = new Thread(np, "T" + i);
            t.start();
        }
    }
}
