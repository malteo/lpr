/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Esercizio2;

/**
 *
 * @author malte
 */
public class ThreadInterrupt {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SleepingThread si = new SleepingThread();
        Thread t = new Thread(si);
        t.start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException x) {
        }
        System.out.println("Interrompo l'altro thread.");
        t.interrupt();
    }
}

class SleepingThread implements Runnable {

    long tempo0;

    public void run() {
        try {
            System.out.println("In sleep() per 10 secondi...");
            tempo0 = System.currentTimeMillis();
            Thread.sleep(10000);
        } catch (InterruptedException x) {
            System.out.print("Sono trascorsi ");
            System.out.print(System.currentTimeMillis() - tempo0);
            System.out.println(" millisecondi.");
            return;
        }
    }
}
