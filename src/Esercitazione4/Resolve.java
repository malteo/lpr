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
package Esercitazione4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class Resolve {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        java.security.Security.setProperty("networkaddress.cache.ttl", "0");

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(args[0]));
        } catch (FileNotFoundException ex) {
            System.out.println("File non trovato.");
        } catch (ArrayIndexOutOfBoundsException ex) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Nome del file (completo di path):");
            reader = new BufferedReader(new FileReader(sc.nextLine()));
        }

        ExecutorService pool = Executors.newCachedThreadPool();
        ArrayList<Future<InetAddress>> myArr =
                new ArrayList<Future<InetAddress>>();
        String nextHost = reader.readLine();
        while (nextHost != null) {
            Future<InetAddress> result = pool.submit(new Task(nextHost));
            myArr.add(result);
            nextHost = reader.readLine();
        }
        reader.close();
        pool.shutdown();

        InetAddress ris;
        for (int i = 0; i < myArr.size(); i++) {
            try {
                ris = myArr.get(i).get(500L, TimeUnit.MILLISECONDS);
                if (ris != null) {
                    System.out.println(ris);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Resolve.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(Resolve.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TimeoutException ex) {
                Logger.getLogger(Resolve.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}

class Task implements Callable<InetAddress> {

    private String host;

    public Task(String host) {
        this.host = host;
    }

    public InetAddress call() {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.out.println(host + " unknown.");
            return null;
        }

    }
}
