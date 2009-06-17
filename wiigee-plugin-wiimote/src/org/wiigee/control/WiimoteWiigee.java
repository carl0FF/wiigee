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
package org.wiigee.control;

import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;

import org.wiigee.util.Log;
import org.wiigee.device.Wiimote;
import org.wiigee.event.AccelerationListener;
import org.wiigee.event.GestureListener;
import org.wiigee.filter.Filter;

// Singleton
public class WiimoteWiigee extends Wiigee {

    protected static String pluginversion = "1.5 alpha";
    protected static String pluginreleasedate = "20090524";
    private static final Object lock = new Object();
    private Vector<Wiimote> devices;

    public WiimoteWiigee() {
        super();
        String stack;
        String stackVersion;
        String l2capFeature;
        String bluecoveVersion;

        Log.write("This is wiigee-plugin-wiimote version " + pluginversion + " (" + pluginreleasedate + ")");

        // Bluecove
        bluecoveVersion = LocalDevice.getProperty("bluecove");
        if(!bluecoveVersion.equals("")) {

            stack = LocalDevice.getProperty("bluecove.stack");
            stackVersion = LocalDevice.getProperty("bluecove.stack.version");
            Log.write("You are using the "+stack+" Bluetooth stack (Version "+stackVersion+")");

            l2capFeature = LocalDevice.getProperty("bluecove.feature.l2cap");
            Log.write("L2CAP supported: "+l2capFeature);

            if(l2capFeature.equals("true")) {
                Log.write("wiigee: found a supported stack!");

                // set min id for Bluecove
                Log.write(Log.DEBUG, "JSR82 PSM Minimum Restriction -- OFF", null);
                System.setProperty("bluecove.jsr82.psm_minimum_off", "true");
            }
        } else {
            Log.write("No Bluecove Library detected - trying anyway...");
        }
    }

    public Wiimote getDevice() throws IOException {
        this.devices = this.discoverWiimotes();
        return devices.elementAt(0);
    }

    /**
     * Returns an array of discovered wiimotes.
     *
     * @return Array of discovered wiimotes or null if
     * none discoverd.
     */
    public Wiimote[] getDevices() throws IOException {
        this.devices = this.discoverWiimotes();
        for (int i = 0; i < this.devices.size(); i++) {
            this.devices.elementAt(i).setLED(i + 1);
        }
        Wiimote[] out = new Wiimote[this.devices.size()];
        for (int i = 0; i < this.devices.size(); i++) {
            out[i] = this.devices.elementAt(i);
        }
        return out;
    }

    /**
     * Discover the wiimotes around the bluetooth host and
     * make them available public via getWiimotes method.
     *
     * @return Array of discovered wiimotes.
     */
    private Vector<Wiimote> discoverWiimotes() throws IOException {
        WiimoteDeviceDiscovery deviceDiscovery = new WiimoteDeviceDiscovery(lock);
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        Log.write("Your Computers Bluetooth MAC: " + localDevice.getBluetoothAddress());

        Log.write("Starting device inquiry...");
        DiscoveryAgent discoveryAgent = localDevice.getDiscoveryAgent();
        discoveryAgent.startInquiry(DiscoveryAgent.GIAC, deviceDiscovery);


        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            Log.write("Problems during device discovery.");
            e.printStackTrace();
        }

        Log.write("Device discovery completed!");
        return deviceDiscovery.getDiscoveredWiimotes();
    }

    /**
     * Returns the number of wiimotes discovered.
     *
     * @return Number of wiimotes discovered.
     */
    public int getNumberOfDevices() {
        return this.devices.size();
    }

    /**
     * Sets the Trainbutton for all wiimotes;
     *
     * @param b Button encoding, see static Wiimote values
     */
    public void setTrainButton(int b) {
        for (int i = 0; i < this.devices.size(); i++) {
            this.devices.elementAt(i).setTrainButton(b);
        }
    }

    /**
     * Sets the Recognitionbutton for all wiimotes;
     *
     * @param b Button encoding, see static Wiimote values
     */
    public void setRecognitionButton(int b) {
        for (int i = 0; i < this.devices.size(); i++) {
            this.devices.elementAt(i).setRecognitionButton(b);
        }
    }

    /**
     * Sets the CloseGesturebutton for all wiimotes;
     *
     * @param b Button encoding, see static Wiimote values
     */
    public void setCloseGestureButton(int b) {
        for (int i = 0; i < this.devices.size(); i++) {
            this.devices.elementAt(i).setCloseGestureButton(b);
        }
    }

    public void addDeviceListener(AccelerationListener listener) {
        for (int i = 0; i < this.devices.size(); i++) {
            this.devices.elementAt(i).addAccelerationListener(listener);
        }
    }

    public void addGestureListener(GestureListener listener) {
        for (int i = 0; i < this.devices.size(); i++) {
            this.devices.elementAt(i).addGestureListener(listener);
        }
    }

    public void addFilter(Filter filter) {
        for (int i = 0; i < this.devices.size(); i++) {
            this.devices.elementAt(i).addFilter(filter);
        }
    }
}
