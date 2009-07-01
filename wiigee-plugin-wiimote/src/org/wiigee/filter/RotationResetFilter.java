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
package org.wiigee.filter;

import org.wiigee.device.Wiimote;
import org.wiigee.util.Log;

/**
 * Removes rotation events which are for all axis
 * under a defined threshold value (which default is 2.0 degrees
 * per second TBD).
 *
 * @author Benjamin 'BePo' Poppinga
 */
public class RotationResetFilter extends Filter {

    private Wiimote device;

    public RotationResetFilter(Wiimote source) {
        super();
        this.device = source;
    }

    @Override
    public void reset() {
        // nothing to reset here
    }

    public double[] filterAlgorithm(double[] vector) {
        double abs = Math.sqrt(vector[0]*vector[0]+
                                vector[1]*vector[1]+
                                vector[2]*vector[2]);
        
        if(abs<=1.05 && abs>=0.95) { // wiimote is idle

            //roll = arctan2(ax,sqrt(ay2+az2))
            //pitch = arctan2(ay,sqrt(ax2+az2))

            double tphi = Math.toDegrees(Math.atan2(vector[0], Math.sqrt(vector[1]*vector[1]+vector[2]*vector[2])));
            double ttheta = Math.toDegrees(Math.atan2(vector[1], Math.sqrt(vector[0]*vector[0]+vector[2]*vector[2])));
            this.device.fireRotationEvent(tphi, ttheta, this.device.getYaw());
            Log.write("reset rotation using acceleration. pitch="+tphi+" roll="+ttheta);
        }
        return vector;
    }

}
