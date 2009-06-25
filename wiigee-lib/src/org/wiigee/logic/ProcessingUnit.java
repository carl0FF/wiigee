package org.wiigee.logic;

import java.util.Vector;

import org.wiigee.event.AccelerationEvent;
import org.wiigee.event.ButtonPressedEvent;
import org.wiigee.event.ButtonReleasedEvent;
import org.wiigee.event.AccelerationListener;
import org.wiigee.event.ButtonListener;
import org.wiigee.event.GestureEvent;
import org.wiigee.event.GestureListener;
import org.wiigee.event.MotionStartEvent;
import org.wiigee.event.MotionStopEvent;
import org.wiigee.event.StateEvent;
import org.wiigee.event.StateListener;
import org.wiigee.util.Log;

public abstract class ProcessingUnit implements AccelerationListener, ButtonListener {

	// Classifier
	protected Classifier classifier;
	
	// Listener
	private Vector<GestureListener> gesturelistener = new Vector<GestureListener>();
    private Vector<StateListener> statelistener = new Vector<StateListener>();
	
	
	public ProcessingUnit() {
		this.classifier = new Classifier();
	}
	
	/** 
	 * Add an GestureListener to receive GestureEvents.
	 * 
	 * @param g
	 * 	Class which implements GestureListener interface.
	 */
	public void addGestureListener(GestureListener g) {
		this.gesturelistener.add(g);
	}

    /**
     * Adds a StateListener to receive StateEvents.
     *
     * @param s Class which implements the StateListener interface.
     */
    public void addStateListener(StateListener s) {
        this.statelistener.add(s);
    }
	
	protected void fireGestureEvent(int id, double probability) {
		GestureEvent w = new GestureEvent(this, id, probability);
		for(int i=0; i<this.gesturelistener.size(); i++) {
			this.gesturelistener.get(i).gestureReceived(w);
		}
	}
	
	protected void fireStateEvent(int state) {
		StateEvent w = new StateEvent(this, state);
		for(int i=0; i<this.statelistener.size(); i++) {
			this.statelistener.get(i).stateReceived(w);
		}
	}
	
	public abstract void accelerationReceived(AccelerationEvent event);

	public abstract void buttonPressReceived(ButtonPressedEvent event);

	public abstract void buttonReleaseReceived(ButtonReleasedEvent event);

	public abstract void motionStartReceived(MotionStartEvent event);

	public abstract void motionStopReceived(MotionStopEvent event);
	
	/**
	 * Resets the complete gesturemodel. After reset no gesture is known
	 * to the system.
	 */
	public void reset() {
		if(this.classifier.getCountOfGestures()>0) {
			this.classifier.clear();
			Log.write("### Model reset ###");
		} else {
			Log.write("There doesn't exist any data to reset.");
		}
	}
	
	// File IO
	public abstract void loadGesture(String filename);
	
	public abstract void saveGesture(int id, String filename);
	
}
