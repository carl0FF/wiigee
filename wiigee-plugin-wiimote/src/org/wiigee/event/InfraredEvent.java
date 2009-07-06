/*
 * wiigee - accelerometerbased gesture recognition
 * Copyright (C) 2007, 2008, 2009 Benjamin Poppinga
 * 
 * Developed at University of Oldenburg
 * Contact: wiigee@benjaminpoppinga.de
 *
 * This file is part of wiigee.
 *
 * wiigee is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.wiigee.event;

import java.util.EventObject;
import org.wiigee.device.Device;

/**
 * An infrared event consists of a set of coordinates, containing values
 * from [0, 1024] in width to [0, 768] in height. for each point there is
 * a given size and if the detected infrared spot is valid.
 *
 * @author Benjamin 'BePo' Poppinga
 */
public class InfraredEvent extends EventObject {

	protected int[][] coordinates;
	protected int[] size;
	protected boolean[] valid;
	
	public InfraredEvent(Device source, int[][] coordinates, int[] size) {
		super(source);
		this.coordinates=coordinates;
		this.size=size;
		this.valid = new boolean[4];
		for(int i=0; i<this.coordinates.length; i++) {
			this.valid[i] = (this.coordinates[i][0]<1023 && this.coordinates[i][1]<1023);
		}
	}

        public boolean[] getValids() {
            return this.valid;
        }
	
	public boolean isValid(int i) {
		return this.valid[i];
	}
	
	public int[][] getCoordinates() {
		return this.coordinates;
	}
	
	public int[] getSize() {
		return this.size;
	}

}
