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

package org.wiigee.device;

import org.wiigee.logic.ProcessingUnit;
import org.wiigee.logic.TriggeredProcessingUnit;
import java.io.IOException;
import java.util.Vector;

import org.wiigee.logic.*;
import org.wiigee.event.*;
import org.wiigee.filter.*;

public class Device {
	
	// Fixed number values.
	public static final int MOTION = 0;

	// Buttons for action coordination
	protected int recognitionbutton;
	protected int trainbutton;
	protected int closegesturebutton;
	
	// Functional
	protected boolean accelerationenabled;
	
	// Filters, can filter the data stream
	protected Vector<Filter> filters = new Vector<Filter>();
	
	// Listeners, receive generated events
	protected Vector<DeviceListener> devicelistener = new Vector<DeviceListener>();
	protected ProcessingUnit processingunit = new TriggeredProcessingUnit();
	
	public Device() {
		this.addFilter(new IdleStateFilter());
		this.addFilter(new MotionDetectFilter(this));
		this.addFilter(new DirectionalEquivalenceFilter());
		this.addDeviceListener(this.processingunit);
	}
	
	/**
	 * Adds a Filter for processing the acceleration values.
	 * @param filter The Filter instance.
	 */
	public void addFilter(Filter filter) {
		this.filters.add(filter);
	}
	
	/**
	 * Resets all the filters, which are resetable.
	 * Sometimes they have to be resettet if a new gesture starts.
	 */
	public void resetFilters() {
		for(int i=0; i<this.filters.size(); i++) {
			this.filters.elementAt(i).reset();
		}
	}
	
	/**
	 * Adds an WiimoteListener to the wiimote. Everytime an action
	 * on the wiimote is performed the WiimoteListener would receive
	 * an event of this action.
	 * 
	 */
	public void addDeviceListener(DeviceListener listener) {
		this.devicelistener.add(listener);
	}
	
	/**
	 * Adds a GestureListener to the wiimote. Everytime a gesture
	 * is performed the GestureListener would receive an event of
	 * this gesture.
	 */
	public void addGestureListener(GestureListener listener) {
		this.processingunit.addGestureListener(listener);
	}
	
	public int getRecognitionButton() {
		return this.recognitionbutton;
	}
	
	public void setRecognitionButton(int b) {
		this.recognitionbutton=b;
	}
	
	public int getTrainButton() {
		return this.trainbutton;
	}
	
	public void setTrainButton(int b) {
		this.trainbutton=b;
	}
	
	public int getCloseGestureButton() {
		return this.closegesturebutton;
	}
	
	public void setCloseGestureButton(int b) {
		this.closegesturebutton=b;
	}

	public ProcessingUnit getProcessingUnit() {
		return this.processingunit;
	}
	
	public boolean accelerationEnabled() {
		return this.accelerationenabled;
	}
	
	public void enableAccelerationSensors() throws IOException {
		this.accelerationenabled=true;
	}
	
	public void disableAccelerationSensors() throws IOException {
		this.accelerationenabled=false;
	}

    public void loadGesture(String filename) {
        this.processingunit.loadGesture(filename);
    }

    public void saveGesture(int id, String filename) {
        this.processingunit.saveGesture(id, filename);
    }
	
	// ###### Event-Methoden
	
	/** Fires an acceleration event.
	 * @param x
	 * 		Acceleration in x direction
	 * @param y
	 * 		Acceleration in y direction
	 * @param z
	 * 		Acceleration in z direction
	 */
	public void fireAccelerationEvent(double[] vector) {
		for(int i=0; i<this.filters.size(); i++) {
			vector = this.filters.get(i).filter(vector);
			// cannot return here if null, because of time-dependent filters
		}
		
		// don't need to create an event if filtered away
		if(vector!=null) {
				// 	calculate the absolute value for the accelerationevent
			double absvalue = Math.sqrt((vector[0]*vector[0])+
					(vector[1]*vector[1])+(vector[2]*vector[2]));
		
			AccelerationEvent w = new AccelerationEvent(this,
					vector[0], vector[1], vector[2], absvalue);
			for(int i=0; i<this.devicelistener.size(); i++) {
				this.devicelistener.get(i).accelerationReceived(w);
			}
		}

	} // fireaccelerationevent
	
	/** Fires a button pressed event.
	 * @param button
	 * 		Integer value of the pressed button.
	 */
	public void fireButtonPressedEvent(int button) {
		ButtonPressedEvent w = new ButtonPressedEvent(this, button);
		for(int i=0; i<this.devicelistener.size(); i++) {
			this.devicelistener.get(i).buttonPressReceived(w);
		}
		
		if(w.isRecognitionInitEvent() || w.isTrainInitEvent()) {
			this.resetFilters();
		}
	}
	
	/** Fires a button released event.
	 */
	public void fireButtonReleasedEvent() {
		ButtonReleasedEvent w = new ButtonReleasedEvent(this);
		for(int i=0; i<this.devicelistener.size(); i++) {
			this.devicelistener.get(i).buttonReleaseReceived(w);
		}
	}
	
	/**
	 * Fires a motion start event.
	 */
	public void fireMotionStartEvent() {
		MotionStartEvent w = new MotionStartEvent(this);
		for(int i=0; i<this.devicelistener.size(); i++) {
			this.devicelistener.get(i).motionStartReceived(w);
		}
	}
	
	/**
	 * Fires a motion stop event.
	 */
	public void fireMotionStopEvent() {
		MotionStopEvent w = new MotionStopEvent(this);
		for(int i=0; i<this.devicelistener.size(); i++) {
			this.devicelistener.get(i).motionStopReceived(w);
		}
	}	
	
}
