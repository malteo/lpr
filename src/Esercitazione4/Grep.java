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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class Grep {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Grep keyword directory");
            return;
        }

        String K = args[0];
        File directory = new File(args[1]);

        if (!directory.isDirectory()) {
            System.out.println(directory + " non è un percorso valido.");
            return;
        }

        ExecutorService pool = Executors.newCachedThreadPool();

        // Un ArrayList che contiene Future i quali restituiscono ciascuno
        // un ArrayList di stringhe, uno per ogni subdirectory.
        ArrayList<Future<ArrayList<String>>> myArr =
                new ArrayList<Future<ArrayList<String>>>();

        submitRecursively(K, directory, myArr, pool);

        pool.shutdown();

        ArrayList<String> results;
        for (int i = 0; i < myArr.size(); i++) {
            try {
                // Ottiene i risultati dal Future...
                results = myArr.get(i).get(1000L, TimeUnit.MILLISECONDS);
                for (String r : results) {
                    // ... e li stampa a video.
                    System.out.println(r);
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TimeoutException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static void submitRecursively(String K,
            File directory,
            ArrayList<Future<ArrayList<String>>> myArr,
            ExecutorService pool) {

        File[] files = directory.listFiles();

        // Per ogni file o subdir contenuto nella directory...
        for (File d : files) {
            // ... se è a sua volta una directory...
            if (d.isDirectory()) {
                // ... invoco il metodo ricorsivamente.
                submitRecursively(K, d, myArr, pool);
            }
        }

        // Infine, sottometto ad un pool la sottodirectory.
        Future<ArrayList<String>> result =
                pool.submit(new FindKeyword(K, directory));

        myArr.add(result);
    }
}

class FindKeyword implements Callable<ArrayList<String>> {

    private File dir;
    private String keyword;

    public FindKeyword(String K, File directory) {
        this.keyword = K;
        this.dir = directory;
    }

    public ArrayList<String> call() throws IOException {
        ArrayList<String> array = new ArrayList<String>();
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                search(keyword, f, array);
            }
        }
        return array;
    }

    private void search(String key, File f, ArrayList<String> array)
            throws IOException {
        CharSequence kw = key.subSequence(0, key.length());
        BufferedReader filebuf = new BufferedReader(new FileReader(f));
        String nextStr;
        nextStr = filebuf.readLine();
        while (nextStr != null) {
            if (nextStr.contains(kw)) {
                array.add(f.getName() + " : " + nextStr);
            }
            nextStr = filebuf.readLine();
        }
        filebuf.close();
    }
}
