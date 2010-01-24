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
 * Il messaggio di risposta.
 * Ha un byte per il comando e un ByteBuffer per i dati.
 * 
 * @author Matteo Giordano <ilmalteo at gmail.com>
 */
public class Msg implements Serializable {

    public int command;
    public ByteBuffer data;

    public Msg(byte command, ByteBuffer data) {
        this.command = command;
        this.data = data;
    }
}
