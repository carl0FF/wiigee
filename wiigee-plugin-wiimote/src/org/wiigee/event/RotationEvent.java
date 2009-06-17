/*
 * wiigee - accelerometerbased gesture recognition
 * Copyright (C) 2007, 2008, 2009 Benjamin Poppinga
 *
 * Developed at University of Oldenburg
 * Contact: benjamin.poppinga@informatik.uni-oldenburg.de
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

public class RotationEvent extends EventObject {

	protected double yaw;
    protected double roll;
    protected double pitch;

	public RotationEvent(Device source, double yaw, double roll, double pitch) {
		super(source);
        this.yaw = yaw;
        this.roll = roll;
        this.pitch = pitch;
	}

}
