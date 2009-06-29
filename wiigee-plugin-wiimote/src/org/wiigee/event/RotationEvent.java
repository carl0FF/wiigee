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
 * A RotationEvents contains the current relative rotation to the last
 * given reset position. If the device has never been resetted before,
 * the last position is the Wiimotes initial position. This event contains
 * all three angles - pitch, yaw, roll - which are only determined using
 * the Wii Motion Plus extension. There wouldn't be a RotationEvent without
 * this extension.
 *
 * @author Benjamin 'BePo' Poppinga
 */
public class RotationEvent extends EventObject {

    protected double pitch;
	protected double yaw;
    protected double roll;

	public RotationEvent(Device source, double pitch, double roll, double yaw) {
		super(source);
        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
	}

    public double getPitch() {
        return this.pitch;
    }

    public double getYaw() {
        return this.yaw;
    }

    public double getRoll() {
        return this.roll;
    }

}
