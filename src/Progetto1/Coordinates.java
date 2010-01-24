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

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 *
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class Coordinates implements Serializable {

    private final short x;
    private final short y;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Coordinates other = (Coordinates) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + this.x;
        hash = 41 * hash + this.y;
        return hash;
    }

    Coordinates(short x, short y) {
        this.x = x;
        this.y = y;
    }

    Coordinates(ByteBuffer bb) {
        this.x = bb.getShort();
        this.y = bb.getShort();
    }

    public short getX() {
        return x;
    }

    public short getY() {
        return y;
    }
}
