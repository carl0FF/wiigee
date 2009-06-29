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
import org.wiigee.logic.ProcessingUnit;

/**
 * This is a StateEvent, telling the listeners in which state of recognition
 * the tool is:
 * 	1 = training,
 *  2 = recognition
 *  
 * @author Benjamin 'BePo' Poppinga
 */
public class StateEvent extends EventObject {

	public final int STATE_LEARNING=1;
	public final int STATE_RECOGNIZING=2;
	
	int state;
	ProcessingUnit analyzer;
	
	/**
	 * Create a StateEvent.
	 * 
	 * @param source The source of which the state has changed.
	 * @param state The state the source has switched to.
	 */
	public StateEvent(ProcessingUnit source, int state) {
		super(source);
		this.analyzer=source;
		this.state=state;
	}
	
	public int getState() {
		return this.state;
	}
	
	public ProcessingUnit getSource() {
		return this.analyzer;
	}
	
}
