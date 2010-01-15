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
package Progetto1;

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
class Coordinates {

    private final short x;
    private final short y;

    Coordinates(short x, short y) {
        this.x = x;
        this.y = y;
    }

    short getX() {
        return x;
    }

    short getY() {
        return y;
    }

    String getXY() {
        return "(" + ")";
    }

    public boolean equals(Coordinates coord) {
        return ((this.x == coord.x) && (this.y == coord.y));
    }
}
