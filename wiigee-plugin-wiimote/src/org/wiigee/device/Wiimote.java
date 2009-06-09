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

import org.wiigee.event.InfraredEvent;
import org.wiigee.device.*;
import java.io.IOException;
import java.util.Random;
import javax.bluetooth.L2CAPConnection;
import javax.microedition.io.Connector;
import org.wiigee.event.*;
import org.wiigee.filter.DirectionalEquivalenceFilter;
import org.wiigee.filter.HighPassFilter;
import org.wiigee.filter.IdleStateFilter;
import org.wiigee.util.Log;

/**
 * @author Benjamin 'BePo' Poppinga
 * This class represents the basic functions of the wiimote.
 * If you want your wiimote to e.g. vibrate you'll do this here.
 *
 */
public class Wiimote extends Device {

	// Fixed number values.
	public static final int MOTION = 0;
	public static final int BUTTON_2 = 1;
	public static final int BUTTON_1 = 2;
	public static final int BUTTON_B = 3;
	public static final int BUTTON_A = 4;
	public static final int BUTTON_MINUS = 5;
	public static final int BUTTON_HOME = 8;
	public static final int BUTTON_LEFT = 9;
	public static final int BUTTON_RIGHT = 10;
	public static final int BUTTON_DOWN = 11;
	public static final int BUTTON_UP = 12;
	public static final int BUTTON_PLUS = 13;
	
	// Reports
	public static final byte CMD_SET_REPORT = 0x52;
	
	// IR Modes
	public static final byte IR_MODE_STANDARD = 1;
	public static final byte IR_MODE_EXTENDED = 0x03;
	
	// Modes / Channels
	public static final byte MODE_BUTTONS = 0x30;
	public static final byte MODE_BUTTONS_ACCELERATION = 0x31;
	public static final byte MODE_BUTTONS_ACCELERATION_INFRARED = 0x33;
	
	
	// Bluetooth-adress as string representation
	private String btaddress;
	
	// LED encoded as byte
	byte ledencoding;
    
	
	// control connection, send commands to wiimote
	private L2CAPConnection controlCon;
	
	// receive connection, receive answers from wiimote
	private L2CAPConnection receiveCon;
	
	
	// Functional
	private boolean vibrating;
	private boolean calibrated;
	private boolean infraredenabled;
	private WiimoteStreamer wms;
    
	/**
	 * Creates a new wiimote-device with a specific bluetooth mac-adress.
	 *
	 * @param btaddress
	 * 			String representation of the mac-adress e.g. 00191D68B57C.
     * @param autofiltering
     *          If set the wiimote would automatically add the IdleStateFilter.
     * @param autoconnect
     *          If set the wiimote would automatically be connected.
	 */
    public Wiimote(String btaddress, boolean autofiltering, boolean autoconnect) throws IOException {
        this.btaddress = this.removeChar(btaddress, ':');
        this.vibrating = false;
        this.setCloseGestureButton(Wiimote.BUTTON_HOME);
        this.setRecognitionButton(Wiimote.BUTTON_B);
        this.setTrainButton(Wiimote.BUTTON_A);

        // automatic filtering enabled
        if(autofiltering) {
            this.addFilter(new IdleStateFilter());
            this.addFilter(new DirectionalEquivalenceFilter());
        }

        // automatic connect enabled
        if(autoconnect) {
            this.connect();
            this.calibrateAccelerometer();
            this.streamData(true);
            this.enableAccelerationSensors();
            this.setLED(1);
        }
    }
	
	/** 
	 * Creates the two needed connections to send and receive commands
	 * to and from the wiimote-device.
	 * 
	 */
	public void connect() throws IOException {
		this.controlCon = (L2CAPConnection)Connector.open("btl2cap://"+
			this.btaddress+":11;authenticate=false;encrypt=false;master=false",
			Connector.WRITE); // 11
		this.receiveCon = (L2CAPConnection)Connector.open("btl2cap://"+
			this.btaddress+":13;authenticate=false;encrypt=false;master=false",
			Connector.READ); // 13
	}
	
	/**
	 * Disconnects the wiimote and closes the two connections.
	 */
	public void disconnect() {
		this.vibrating=false;
		try {
			this.controlCon.close();
			this.receiveCon.close();
			Log.write("Disconnected wiimote.");
		} catch(Exception e) {
			Log.write("Failure during disconnect of wiimote.");
		}
	}
	
	/**
	 * @return
	 * 		Receiving data connection 
	 */
	public L2CAPConnection getReceiveConnection() {
		return this.receiveCon;
	}

	/** 
	 * This method makes the Wiimote-Class reacting to incoming data.
	 * For just controlling and sending commands to the wiimote
	 * (vibration, LEDs, ...) it's not necessary to call this method.
	 * 
	 * @param value
	 * 		true, if the class should react to incoming data.
	 * 		false, if you only want to send commands to wiimote and
	 * 		only the control-connection is used.
	 */
	public void streamData(boolean value) {
		if(value==true) {
			if(this.wms==null) {
				this.wms = new WiimoteStreamer(this);
			}
			wms.start(); }
		else if(this.wms!=null) {
				wms.stopThread();
		}
	}
	
	/**
	 * Write data to a register inside of the wiimote.
	 * 
	 * @param offset The memory offset, 3 bytes.
	 * @param data The data to be written, max. 16 bytes.
	 * @throws IOException
	 */
	public void writeRegister(byte[] offset, byte[] data) throws IOException {
		byte[] raw = new byte[23];
		raw[0] = CMD_SET_REPORT;
		raw[1] = 0x16; // Write channel
		raw[2] = 0x04; // Register
		for(int i=0; i<offset.length; i++) {
			raw[3+i] = offset[i];
		}
		raw[6] = (byte)data.length;
		for(int i=0; i<data.length; i++) {
			raw[7+i] = data[i];
		}
		this.sendRaw(raw);
	}

	/**
	 * Makes the Wiimote respond the data of an register. The wiimotestreamer
	 * doesn't react to the reponse yet.
	 * 
	 * @param offset The memory offset.
	 * @param size The size which has to be read out.
	 * @throws IOException
	 */
	public void readRegister(byte[] offset, byte[] size) throws IOException {
		byte[] raw = new byte[8];
		raw[0] = CMD_SET_REPORT;
		raw[1] = 0x17; // Read channel
		raw[2] = 0x04; // Register
		for(int i=0; i<offset.length; i++) {
			raw[3+i] = offset[i];
		}
		for(int i=0; i<size.length; i++) {
			raw[6+i] = size[i];
		}
		this.sendRaw(raw);
	}
	
	/**
	 * Reads data out of the EEPROM of the wiimote.
	 * At the moment this method is only used to read out the
	 * calibration data, so the wiimotestreamer doesn't react for
	 * every answer on this request.
	 * 
	 * @param offset The memory offset.
	 * @param size The size.
	 * @throws IOException
	 */
	public void readEEPROM(byte[] offset, byte[] size) throws IOException {
		byte[] raw = new byte[8];
		raw[0] = CMD_SET_REPORT;
		raw[1] = 0x17; // Read channel
		raw[2] = 0x00; // EEPROM
		for(int i=0; i<offset.length; i++) {
			raw[3+i] = offset[i];
		}
		for(int i=0; i<size.length; i++) {
			raw[6+i] = size[i];
		}
		this.sendRaw(raw);
	}
	
	/**
	 * Sends pure hexdata to the wiimote. If you want your wiimote
	 * to vibrate use sendRaw(new byte[] {0x52, 0x13, 0x01}). For other raw-commands use
	 * the specific wiki-sites around the web (wiili.org, wiibrew.org, ...)
	 * @param raw
	 * 		byte representation of an command
	 */
	public void sendRaw(byte[] raw) throws IOException {
		if(this.controlCon!=null) {
			this.controlCon.send(raw);
			try {
				Thread.sleep(20l);
			} catch (InterruptedException e) {
				System.out.println("sendRaw() interrupted");
			}
		}
	}
	
	
	/**
	 * Enables one or more LEDs, where the value could be between 0 and 8.
	 * If value=1 only the left LED would light up, for value=2 the second
	 * led would light up, for value=3 the first and second led would light up,
	 * and so on...
	 * 
	 * @param value Between 0 and 8, indicating which LEDs should light up
	 * @throws IOException
	 */
	public void setLED(int value) throws IOException {
		if(value<16 && value>0) {
			byte tmp = (byte)value;
			this.ledencoding=(byte)(tmp<<4);
			this.sendRaw(new byte[] {CMD_SET_REPORT, 0x11, this.ledencoding});
		} else {
			// Random LED change :)
			this.setLED(new Random().nextInt(16));
		}
	}
	
	/**
	 * Initializes the calibration of the accerlarometer. This is done once
	 * per each controller in program lifetime.
	 * 
	 * @throws IOException
	 */
	private void calibrateAccelerometer() throws IOException {
		// calibration command
		this.readEEPROM(new byte[] {0x00, 0x00, 0x20}, new byte[] {0x00, 0x07});
		this.calibrated=true;
	}
	
	/**
	 * Activates the acceleration sensor. You have to call the
	 * streamData(true) method to react to this acceleration data.
	 * Otherwise the wiimote would send data the whole time and
	 * nothing else would happen.
	 * 
	 */
    @Override
	public void enableAccelerationSensors() throws IOException {
		super.enableAccelerationSensors();
		if(!this.calibrated) {
			this.calibrateAccelerometer();
		}
		
		 // enable acceleration in continuous mode
		this.sendRaw(new byte[] {CMD_SET_REPORT, 0x12, 0x04, 0x31});
	}
	
	/**
	 * Deactivates the acceleration sensors.
	 * 
	 */
    @Override
	public void disableAccelerationSensors() throws IOException {
		super.disableAccelerationSensors();
		this.sendRaw(new byte[] {CMD_SET_REPORT, 0x12, 0x00, 0x30});
	}
	
	/**
	 * Enables the infrared camera in front of the wiimote to track
	 * IR sources in the field of view of the camera. This could be used
	 * to a lot of amazing stuff. Using this Mode could slow down the
	 * recognition of acceleration gestures during the increased data
	 * size transmitted.
	 */
	public void enableInfraredCamera() throws IOException {
		this.accelerationenabled=true;
		this.infraredenabled=true;
		if(!this.calibrated) {
			this.calibrateAccelerometer();
		}
		
		//write 0x04 to output 0x13
		this.sendRaw(new byte[] {CMD_SET_REPORT, 0x13, 0x04});
		
		// write 0x04 to output 0x1a
		this.sendRaw(new byte[] {CMD_SET_REPORT, 0x1a, 0x04});
		
		// write 0x08 to reguster 0xb00030
		this.writeRegister(new byte[] {(byte)0xb0, 0x00, 0x30}, new byte[] {0x08});
		
		// write sensivity block 1 to register 0xb00000
		this.writeRegister(new byte[] {(byte)0xb0, 0x00, 0x00}, new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0x90, 0x00, (byte)0xc0});
		
		// write sensivity block 2 to register 0xb0001a
		this.writeRegister(new byte[] {(byte)0xb0, 0x00, (byte)0x1a}, new byte[] {0x40, 0x00});
		
		// write ir-mode to register 0xb00033
		this.writeRegister(new byte[] {(byte)0xb0, 0x00, 0x33}, new byte[] {0x03});		
		
		// enable continuous acceleration and IR cam on channel 33
		this.sendRaw(new byte[] {CMD_SET_REPORT, 0x12, 0x00, 0x33});
	
	}
	
	
	/**
	 * With this method you gain access over the vibrate function of
	 * the wiimote. You got to try which time in milliseconds would
	 * fit your requirements.
	 * 
	 * @param milliseconds
	 * 		time the wiimote would vibrate
	 */
	public void vibrateForTime(long milliseconds) throws IOException {
		try {
		 if(!vibrating) {
			this.vibrating=true;
			byte tmp = (byte)(this.ledencoding | 0x01);
			this.sendRaw(new byte[] {CMD_SET_REPORT, 0x11, tmp});
			Thread.sleep(milliseconds);
			this.sendRaw(new byte[] {CMD_SET_REPORT, 0x11, this.ledencoding});
			this.vibrating=false;
		 }
		} catch (InterruptedException e) {
			System.out.println("WiiMoteThread interrupted.");
		}
	}
	
	public boolean infraredEnabled() {
		return this.infraredenabled;
	}
	
	// ###### Hilfsmethoden
	// TODO	
	private String removeChar(String s, char c) {
	    String r = "";
	    for (int i = 0; i < s.length(); i ++) {
	       if (s.charAt(i) != c) r += s.charAt(i);
	       }
	    return r;
	}
	
	/**
	 * Fires a infrared event
	 * 
	 * @param coordinates
	 * @param size
	 */
	public void fireInfraredEvent(int[][] coordinates, int[] size) {
		InfraredEvent w = new InfraredEvent(this, coordinates, size);
		for(int i=0; i<this.devicelistener.size(); i++) {
			this.devicelistener.get(i).infraredReceived(w);
		}
	}


	
}
