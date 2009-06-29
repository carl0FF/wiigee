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

/**
 * Removes rotation events which are for all axis
 * under a defined threshold value (which default is 2.0 degrees
 * per second TBD).
 *
 * @author Benjamin 'BePo' Poppinga
 */
public class RotationThresholdFilter extends Filter {
	
	private double threshold;

	public RotationThresholdFilter() {
		super();
		this.threshold = 2.0;
	}

    public RotationThresholdFilter(double threshold) {
        super();
        this.threshold = threshold;
    }

    @Override
    public void reset() {
        // nothing to reset here
    }
	
	public double[] filterAlgorithm(double[] vector) {
		if(Math.abs(vector[0])>threshold ||
           Math.abs(vector[1])>threshold ||
           Math.abs(vector[2])>threshold) {
			return vector;
		} else {
			return new double[] { 0.0, 0.0, 0.0 };
		}
	}
	
	public void setSensivity(double sensivity) {
		this.threshold=sensivity;
	}
	
	public double getSensivity() {
		return this.threshold;
	}

}
