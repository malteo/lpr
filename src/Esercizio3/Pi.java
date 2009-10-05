/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Esercizio3;

/**
 *
 * @author malte
 */
public class Pi {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Esercizio2 accuracy time");
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

class CalculatingThread implements Runnable {

    double accuracy;

    CalculatingThread(double accuracy) {
        this.accuracy = accuracy;
    }

    public void run() {
        double pi = 0.0, sign = 1.0, rec = 1.0;
        while (!(Thread.interrupted()) && Math.abs(pi - Math.PI) > accuracy) {
            pi += sign * (4.0 / rec);
            sign *= -1.0;
            rec += 2.0;
        }
        System.out.println("Approssimazione di pi = " + pi);
    }
}
