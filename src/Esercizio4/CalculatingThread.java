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

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
class CalculatingThread implements Callable {

    double accuracy;

    CalculatingThread(double accuracy) {
        this.accuracy = accuracy;
    }

    public Double call() {
        double pi = 0.0, sign = 1.0, rec = 1.0;
        while (!(Thread.interrupted()) && Math.abs(pi - Math.PI) > accuracy) {
            pi += sign * (4.0 / rec);
            sign *= -1.0;
            rec += 2.0;
        }
        return pi;
    }
}
