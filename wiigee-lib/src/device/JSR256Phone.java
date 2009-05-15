/*
 * wiigee - accelerometerbased gesture recognition
 * Copyright (C) 2007, 2008 Benjamin Poppinga
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

package device;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.sensor.Data;
import javax.microedition.sensor.DataListener;
import javax.microedition.sensor.SensorConnection;
import javax.microedition.sensor.SensorInfo;
import javax.microedition.sensor.SensorManager;

/**
 * @author Benjamin 'BePo' Poppinga
 * This class represents the basic functions of a JSR256 enabled mobile phone.
 * You need the JSR256 specifications to compile this code correctly. Your device
 * should be J2ME v1.5 enabled, most mobile phones are at J2ME 1.4.2 at the moment.
 * So, with this restrictions, this is more or less only a template you can continue
 * developing with.
 *
 */
public class JSR256Phone extends Device implements DataListener {

	private SensorConnection sensor;
	
	public JSR256Phone() throws IOException {
		super();
		// TODO: define buttons, depending on device
		this.sensor = (SensorConnection) Connector.open(this.getSensorURL());
		this.sensor.setDataListener(this, 1);
	}
	
	/**
	 * @author Benjamin 'BePo' Poppinga
	 * Called from DataListener, JSR 256, if an acceleration happend.
	 */
	public void dataReceived(SensorConnection sensor, Data[] data, boolean arg2) {
		
		int x=0, y=0, z=0;
		double[] acc = new double[3];
		
		for(int i=0; i<data.length; i++) {
			if(data[i].getChannelInfo().getName().compareTo("axis_x") == 0) {
				x = data[i].getIntValues()[0];
			} else if(data[i].getChannelInfo().getName().compareTo("axis_y") == 0) {
				y = data[i].getIntValues()[0];
			} else if(data[i].getChannelInfo().getName().compareTo("axis_y") == 0) {
				z = data[i].getIntValues()[0];
			}
		}
		
		// calibration has to be done here
		// at the moment: fixed values for Sony Ericsson K850i
		//  -- may work for other devices, too
		int x0 = -63;
		int x3 = -1044;
		int y0 = 45;
		int y2 = -936;
		int z0 = 45;
		int z1 = -936;
		
		acc[0] = (double) (x - x0) / (double) (x3 - x0);
		acc[1] = (double) (y - y0) / (double) (y2 - y0);
		acc[2] = (double) (z - z0) / (double) (z1 - z0);
		
		this.fireAccelerationEvent(acc);		
	}
	
	// TODO:
	// Button press events should be delegated from current GUI
	// to the fireButtonPress() methods. I don't know at the moment,
	// if there exist different value encodings for different mobile
	// phones.
	
	/*
	 * @author Benjamin 'BePo' Poppinga
	 * Helper to determine the accelerometer URL
	 */
	private String getSensorURL() {
		SensorInfo[] si = SensorManager.findSensors("acceleration",
				SensorInfo.CONTEXT_TYPE_USER);
		return si[0].getUrl();		
	}

}
