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
package Esercitazione3;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class Esercizio1 {

    // un "trucco" per poter fare uno switch su String
    public enum Command {

        PI, FIB, FACT, QUIT, NOVALUE;

        public static Command toCommand(String str) {
            try {
                // si può scrivere il comando anche in minuscolo
                return valueOf(str.toUpperCase());
            } catch (Exception ex) {
                return NOVALUE;
            }
        }
    }

    public static void main(String[] args) {
        // uno scanner per leggere gli input
        Scanner sc = new Scanner(System.in);
        ExecutorService es = Executors.newCachedThreadPool();
        System.out.println("Sintassi: [ PI:precisione | FIB:n | FACT:n | HELP | QUIT ]");
        try {
            while (sc.hasNext()) {
                // ottiene un array di stringhe splittando il comando sui ":"
                String[] command = sc.nextLine().split(":");
                try {
                    switch (Command.toCommand(command[0])) {
                        case PI:
                            es.submit(new PiTask(Integer.parseInt(command[1])));
                            break;
                        case FIB:
                            es.submit(new FibTask(Integer.parseInt(command[1])));
                            break;
                        case FACT:
                            // qui passo proprio la String, ci penserà il
                            // costruttore di BigInteger a convertirlo in numero
                            es.submit(new FactTask(command[1]));
                            break;
                        case QUIT:
                            es.shutdownNow();
                            sc.close();
                            break;
                        default:
                            System.out.println("Sintassi: [ PI:precisione | FIB:n | FACT:n | QUIT ]");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("L'argomento dev'essere un numero.");
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Manca l'argomento.");
                }
            }
        } catch (IllegalStateException e) {
            System.out.println("Ciao :(");
        }
    }
}

class PiTask implements Runnable {

    double accuracy;

    public PiTask(int precision) {
        accuracy = 1 / Math.pow(10, precision);
    }

    public void run() {
        long time0 = System.currentTimeMillis();
        double pi = 0.0, sign = 1.0, rec = 1.0;
        while (Math.abs(pi - Math.PI) > accuracy) {
            pi += sign * (4.0 / rec);
            sign *= -1.0;
            rec += 2.0;
        }
        long time = System.currentTimeMillis();
        System.out.println("Nome del thread: " + Thread.currentThread().getName());
        // formatta currentTimeMillis() in hh.mm.ss
        System.out.println("Istante di creazione: " + SimpleDateFormat.getTimeInstance().format(time0));
        System.out.println("Istante di completamento: " + SimpleDateFormat.getTimeInstance().format(time));
        System.out.println("Approssimazione di pi = " + pi);
    }
}

class FibTask implements Runnable {

    int n;

    public FibTask(int n) {
        this.n = n;
    }

    private int fib(int n) {
        if (n == 0 || n == 1) {
            return n;
        }
        if (n == 1) {
            return 1;
        }
        return fib(n - 1) + fib(n - 2);
    }

    public void run() {
        long time0 = System.currentTimeMillis();
        int result = fib(n);
        long time = System.currentTimeMillis();
        System.out.println("Nome del thread: " + Thread.currentThread().getName());
        System.out.println("Istante di creazione: " + SimpleDateFormat.getTimeInstance().format(time0));
        System.out.println("Istante di completamento: " + SimpleDateFormat.getTimeInstance().format(time));
        System.out.println("fib(" + n + ") = " + result);
    }
}

class FactTask implements Runnable {

    // uso BigInteger perché mi serve un numero "longer than long"
    BigInteger n;

    public FactTask(String n) {
        this.n = new BigInteger(n);
    }

    private BigInteger fact(BigInteger n) {
        // rappresentazioni di 0 e 1 in BigInteger
        if (n == BigInteger.ZERO) {
            return BigInteger.ONE;
        }
        // operazioni di moltiplicazione e sottrazione per i BigInteger
        // equivale a n * fact(n - 1)
        return n.multiply(fact(n.subtract(BigInteger.ONE)));
    }

    public void run() {
        long time0 = System.currentTimeMillis();
        BigInteger result = fact(n);
        long time = System.currentTimeMillis();
        System.out.println("Nome del thread: " + Thread.currentThread().getName());
        System.out.println("Istante di creazione: " + SimpleDateFormat.getTimeInstance().format(time0));
        System.out.println("Istante di completamento: " + SimpleDateFormat.getTimeInstance().format(time));
        System.out.println("fact(" + n + ") = " + result);
    }
}
