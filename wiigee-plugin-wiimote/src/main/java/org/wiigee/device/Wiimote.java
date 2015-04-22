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
package org.wiigee.device;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;
import javax.bluetooth.L2CAPConnection;
import javax.microedition.io.Connector;
import org.wiigee.event.*;
import org.wiigee.filter.Filter;
import org.wiigee.util.Log;

/**
 * This class represents the basic functions of the wiimote.
 * If you want your wiimote to e.g. vibrate you'll do this here.
 *
 * @author Benjamin 'BePo' Poppinga
 */
public class Wiimote extends Device {

    // Fixed number values.
    public static final int BUTTON_2 = 0x0001;
    public static final int BUTTON_1 = 0x0002;
    public static final int BUTTON_B = 0x0004;
    public static final int BUTTON_A = 0x0008;
    public static final int BUTTON_MINUS = 0x0010;
    public static final int BUTTON_HOME = 0x0080;
    public static final int BUTTON_LEFT = 0x0100;
    public static final int BUTTON_RIGHT = 0x0200;
    public static final int BUTTON_DOWN = 0x0400;
    public static final int BUTTON_UP = 0x0800;
    public static final int BUTTON_PLUS = 0x1000;

    // Reports
    public static final byte CMD_SET_REPORT = 0x52;

    // IR Modes
    public static final byte IR_MODE_STANDARD = 0x01;
    public static final byte IR_MODE_EXTENDED = 0x03;

    // Modes / Channels
    public static final byte MODE_BUTTONS = 0x30;
    public static final byte MODE_BUTTONS_ACCELERATION = 0x31;
    public static final byte MODE_BUTTONS_ACCELERATION_INFRARED = 0x33;

    // Bluetooth-adress as string representation
    private String btaddress;

    // LED encoded as byte
    byte ledencoding;

    // Filters, can filter the data stream
    protected Vector<Filter> rotfilters = new Vector<Filter>();

    // control connection, send commands to wiimote
    private L2CAPConnection controlCon;

    // receive connection, receive answers from wiimote
    private L2CAPConnection receiveCon;

    // Listeners, receive generated events
    protected Vector<InfraredListener> infraredlistener = new Vector<InfraredListener>();
    protected Vector<RotationListener> rotationListener = new Vector<RotationListener>();

    // keep track of the orientation
    private double pitch = 0.0;
    private double roll = 0.0;
    private double yaw = 0.0;

    // Functional
    private boolean vibrating;
    private boolean calibrated;
    private boolean infraredEnabled;
    private WiimoteStreamer wms;
    private boolean wiiMotionPlusEnabled;

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
        super(autofiltering);
        this.btaddress = this.removeChar(btaddress, ':');
        this.vibrating = false;
        this.setCloseGestureButton(Wiimote.BUTTON_HOME);
        this.setRecognitionButton(Wiimote.BUTTON_B);
        this.setTrainButton(Wiimote.BUTTON_A);

        // automatic connect enabled
        if (autoconnect) {
            this.connect();
            this.calibrateAccelerometer();
            this.streamData(true);
            this.setLED(1);
            this.setAccelerationEnabled(true);
        }
    }

    /**
     * Creates the two needed connections to send and receive commands
     * to and from the wiimote-device.
     *
     */
    public void connect() throws IOException {
        this.controlCon = (L2CAPConnection) Connector.open("btl2cap://" +
                this.btaddress + ":11;authenticate=false;encrypt=false;master=false",
                Connector.WRITE); // 11
        this.receiveCon = (L2CAPConnection) Connector.open("btl2cap://" +
                this.btaddress + ":13;authenticate=false;encrypt=false;master=false",
                Connector.READ); // 13
    }

    /**
     * Disconnects the wiimote and closes the two connections.
     */
    public void disconnect() {
        this.vibrating = false;
        try {
            this.controlCon.close();
            this.receiveCon.close();
            Log.write("Disconnected wiimote.");
        } catch (Exception e) {
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
        if (value == true) {
            if (this.wms == null) {
                this.wms = new WiimoteStreamer(this);
            }
            wms.start();
        } else if (this.wms != null) {
            wms.stopThread();
        }
    }

    /**
     * The added Listener will be notified about detected infrated
     * events.
     *
     * @param listener The Listener to be added.
     */
    public void addInfraredListener(InfraredListener listener) {
        this.infraredlistener.add(listener);
    }

    /**
     * The added Listener will be notified about detected orientation
     * changes.
     *
     * @param listener The Listener to be added.
     */
    public void addRotationListener(RotationListener listener) {
        this.rotationListener.add(listener);
    }

    /**
     * Adds a filter to process the rotation speed data of the
     * wiimote with an attached Wii Motion Plus.
     *
     * @param filter The Filter to be added.
     */
    public void addRotationFilter(Filter filter) {
        this.rotfilters.add(filter);
    }

    /**
     * Resets all filters which are applied to the rotation data
     * from the Wii Motion Plus. Also resets _all_ determined orientation
     * angles,  which should be extended with a consideration of other
     * external datas - maybe irda events.
     */
    public void resetRotationFilters() {
        this.yaw = 0.0;
        this.pitch = 0.0;
        this.roll = 0.0;
        for (int i = 0; i < this.rotfilters.size(); i++) {
            this.rotfilters.elementAt(i).reset();
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
        for (int i = 0; i < offset.length; i++) {
            raw[3 + i] = offset[i];
        }
        raw[6] = (byte) data.length;
        for (int i = 0; i < data.length; i++) {
            raw[7 + i] = data[i];
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
        for (int i = 0; i < offset.length; i++) {
            raw[3 + i] = offset[i];
        }
        for (int i = 0; i < size.length; i++) {
            raw[6 + i] = size[i];
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
        for (int i = 0; i < offset.length; i++) {
            raw[3 + i] = offset[i];
        }
        for (int i = 0; i < size.length; i++) {
            raw[6 + i] = size[i];
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
        if (this.controlCon != null) {
            this.controlCon.send(raw);
            try {
                Thread.sleep(100l);
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
        if (value < 16 && value > 0) {
            byte tmp = (byte) value;
            this.ledencoding = (byte) (tmp << 4);
            this.sendRaw(new byte[]{CMD_SET_REPORT, 0x11, this.ledencoding});
        } else {
            // Random LED change :)
            this.setLED(new Random().nextInt(16));
        }
    }

    /**
     * Updates the report channel according to the choosen
     * functions that are enabled (acceleration, irda, ...).
     *
     */
    private void updateReportChannel() throws IOException {
        if(!accelerationEnabled
        && !wiiMotionPlusEnabled
        && !infraredEnabled) {
            this.sendRaw(new byte[]{CMD_SET_REPORT, 0x12, 0x00, 0x30});
        }
        else if(accelerationEnabled
             && !wiiMotionPlusEnabled
             && !infraredEnabled) {
            this.sendRaw(new byte[]{CMD_SET_REPORT, 0x12, 0x04, 0x31});
        }
        else if(!accelerationEnabled
             && wiiMotionPlusEnabled
             && !infraredEnabled) {
            this.sendRaw(new byte[]{CMD_SET_REPORT, 0x12, 0x00, 0x32});
        }
        else if(accelerationEnabled
             && wiiMotionPlusEnabled
             && !infraredEnabled) {
            this.sendRaw(new byte[]{CMD_SET_REPORT, 0x12, 0x04, 0x35});
        }
        else if(accelerationEnabled
             && !wiiMotionPlusEnabled
             && infraredEnabled) {
            this.sendRaw(new byte[]{CMD_SET_REPORT, 0x12, 0x04, 0x33});
        }
        else if(accelerationEnabled
             && wiiMotionPlusEnabled
             && infraredEnabled) {
            this.sendRaw(new byte[]{CMD_SET_REPORT, 0x12, 0x04, 0x37});
        }
        else {
            // default channel - fallback to button only.
            Log.write("Invalid Value Configuration: Fallback to Buttons only.");
            this.sendRaw(new byte[]{CMD_SET_REPORT, 0x12, 0x00, 0x30});
        }
    }

    /**
     * Initializes the calibration of the accerlerometer. This is done once
     * per each controller in program lifetime.
     *
     * @throws IOException
     */
    private void calibrateAccelerometer() throws IOException {
        this.readEEPROM(new byte[]{0x00, 0x00, 0x20}, new byte[]{0x00, 0x07});
        this.calibrated = true;
    }

    /**
     * Activates the acceleration sensor. You have to call the
     * streamData(true) method to react to this acceleration data.
     * Otherwise the wiimote would send data the whole time and
     * nothing else would happen.
     *
     */
    @Override
    public void setAccelerationEnabled(boolean enabled) throws IOException {
        super.setAccelerationEnabled(enabled);
        if(enabled) {
            Log.write("Enabling ACCELEROMETER...");
            this.accelerationEnabled = true;
            if (!this.calibrated) {
                this.calibrateAccelerometer();
            }
        } else {
            Log.write("Disabling ACCELEROMETER...");
            this.accelerationEnabled = false;
        }
       
       // change channel dynamically
       this.updateReportChannel();
    }

    /**
     * Enables or disables the infrared camera of the wiimote with
     * the default values.
     *
     * @param e Should the Infrared Camera be enabled.
     * @throws IOException In case of a connection error.
     */
    public void setInfraredCameraEnabled(boolean enabled) throws IOException {
        this.setInfraredCameraEnabled(enabled, Wiimote.IR_MODE_STANDARD);
    }

    /**
     * Enables the infrared camera in front of the wiimote to track
     * IR sources in the field of view of the camera. This could be used
     * to a lot of amazing stuff. Using this Mode could slow down the
     * recognition of acceleration gestures during the increased data
     * size transmitted.
     *
     * @param e Should the Infrared Camera be enabled.
     * @param infraredMode The choosen Infrared Camera Mode.
     * @throws IOException In case of a connection error.
     *
     */
    public void setInfraredCameraEnabled(boolean enabled, byte infraredMode) throws IOException {
        if(enabled) {
            Log.write("Enabling INFRARED CAMERA...");
            this.infraredEnabled = true;

            //write 0x04 to output 0x13
            this.sendRaw(new byte[]{CMD_SET_REPORT, 0x13, 0x04});

            // write 0x04 to output 0x1a
            this.sendRaw(new byte[]{CMD_SET_REPORT, 0x1a, 0x04});

            // write 0x08 to reguster 0xb00030
            this.writeRegister(new byte[]{(byte) 0xb0, 0x00, 0x30}, new byte[]{0x08});

            // write sensivity block 1 to register 0xb00000
            this.writeRegister(new byte[]{(byte) 0xb0, 0x00, 0x00}, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x90, 0x00, (byte) 0x41});

            // write sensivity block 2 to register 0xb0001a
            this.writeRegister(new byte[]{(byte) 0xb0, 0x00, (byte) 0x1a}, new byte[]{0x40, 0x00});

            // write ir-mode to register 0xb00033
            this.writeRegister(new byte[]{(byte) 0xb0, 0x00, 0x33}, new byte[]{infraredMode});
        } else {
            Log.write("Disabling INFRARED CAMERA...");
            this.infraredEnabled = false;
        }

        // change channel dynamically
        this.updateReportChannel();
    }

    /**
     * To enable the Wii Motion Plus extension. The wiimote will further get
     * every other information, like acceleration, infrared camera (loss of precision)
     * and button presses.
     *
     * @throws java.io.IOException
     */
    public void setWiiMotionPlusEnabled(boolean enabled) throws IOException {
        if(enabled) {
            Log.write("Enabling WII MOTION PLUS..");
            this.wiiMotionPlusEnabled = true;
            // write 0x04 to 0x04a600fe to get wii m+ data within extension reports
            this.writeRegister(new byte[]{(byte) 0xa6, 0x00, (byte) 0xfe}, new byte[]{0x04});
        } else {
            Log.write("Disabling WII MOTION PLUS..");
            this.wiiMotionPlusEnabled = false;
        }

        // change channel dynamically
        this.updateReportChannel();
    }
    

    /**
     * With this method you gain access over the vibrate function of
     * the wiimote. You got to try which time in milliseconds would
     * fit your requirements.
     *
     * @param milliseconds Time the wiimote would approx. vibrate.
     */
    public void vibrateForTime(long milliseconds) throws IOException {
        try {
            if (!vibrating) {
                this.vibrating = true;
                byte tmp = (byte) (this.ledencoding | 0x01);
                this.sendRaw(new byte[]{CMD_SET_REPORT, 0x11, tmp});
                Thread.sleep(milliseconds);
                this.sendRaw(new byte[]{CMD_SET_REPORT, 0x11, this.ledencoding});
                this.vibrating = false;
            }
        } catch (InterruptedException e) {
            System.out.println("WiiMoteThread interrupted.");
        }
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

    /**
     * Fires a infrared event, containig coordinate pairs (x,y) and a
     * size of the detected IR spot.
     *
     * @param coordinates X and Y display coordinates.
     * @param size The size of the spot.
     */
    public void fireInfraredEvent(int[][] coordinates, int[] size) {
        InfraredEvent w = new InfraredEvent(this, coordinates, size);
        for (int i = 0; i < this.infraredlistener.size(); i++) {
            this.infraredlistener.get(i).infraredReceived(w);
        }
    }

    /**
     * Fires the current relative orientation of the Wiimote to
     * all RotationListeners.
     *
     * @param yaw Orientation around Z axis.
     * @param roll Orientation around Y axis.
     * @param pitch Orientation around X axis.
     */
    public void fireRotationEvent(double pitch, double roll, double yaw) {
        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;

        RotationEvent w = new RotationEvent(this, pitch, roll, yaw);
        for (int i = 0; i < this.rotationListener.size(); i++) {
            this.rotationListener.elementAt(i).rotationReceived(w);
        }
    }

    /**
     * If a Wii Motion Plus is attached and activated properly this
     * event could be fired within every change of orientation of the
     * device. The orientation is not used to do gesture recognition,
     * yet.
     *
     * @param vector The rotational speed vector, containing:
     *  phi - Rotational speed of x axis (pitch)
     *  theta - Rotational speed of y axis (roll)
     *  psi - Rotational speed of z axis (yaw)
     */
    public void fireRotationSpeedEvent(double[] vector) {
        for (int i = 0; i < this.rotfilters.size(); i++) {
            vector = this.rotfilters.get(i).filter(vector);
            // cannot return here if null, because of time-dependent filters
        }

        if (vector != null) {
            RotationSpeedEvent w = new RotationSpeedEvent(this, vector[0], vector[1], vector[2]);
            for (int i = 0; i < this.rotationListener.size(); i++) {
                this.rotationListener.elementAt(i).rotationSpeedReceived(w);
            }

            // calculate new orientation with integration
            // do not store new global values here, since they
            // need regular updates only depended on acceleration values.
            double tyaw = this.yaw + vector[0] * 0.01;
            double troll = this.roll + vector[1] * 0.01;
            double tpitch = this.pitch + vector[2] * 0.01;
            this.fireRotationEvent(tpitch, troll, tyaw);
        }
    }

    // ###### Hilfsmethoden
    // TODO
    private String removeChar(String s, char c) {
        String r = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != c) {
                r += s.charAt(i);
            }
        }
        return r;
    }
}
