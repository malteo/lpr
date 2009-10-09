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

import com.sun.org.apache.xml.internal.serializer.ToUnknownStream;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class Esercizio1 {

    public enum Command {

        PI, FIB, FACT, QUIT, HELP, NOVALUE;

        public static Command toCommand(String str) {
            try {
                return valueOf(str.toUpperCase());
            } catch (Exception ex) {
                return NOVALUE;
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ExecutorService es = Executors.newCachedThreadPool();
        System.out.println("Sintassi: [ PI:precisione | FIB:n | FACT:n | HELP | QUIT ]");
        try {
            while (sc.hasNext()) {
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
                            es.submit(new FactTask(Integer.parseInt(command[1])));
                            break;
                        case QUIT:
                            es.shutdownNow();
                            sc.close();
                            break;
                        case HELP:
                            System.out.println("Sintassi: [ PI:precisione | FIB:n | FACT:n | HELP | QUIT ]");
                            break;
                        default:
                            System.out.println("command not found (cit.)");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("L'argomento dev'essere un Integer.");
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

    PiTask(int precision) {
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

    long n;

    public FactTask(int n) {
        this.n = n;
    }

    private long fact(long n) {
        if (n == 0) {
            return 1;
        }
        return n * fact(n - 1);
    }

    public void run() {
        long time0 = System.currentTimeMillis();
        long result = fact(n);
        long time = System.currentTimeMillis();
        System.out.println("Nome del thread: " + Thread.currentThread().getName());
        System.out.println("Istante di creazione: " + SimpleDateFormat.getTimeInstance().format(time0));
        System.out.println("Istante di completamento: " + SimpleDateFormat.getTimeInstance().format(time));
        System.out.println("fact(" + n + ") = " + result);
    }
}
