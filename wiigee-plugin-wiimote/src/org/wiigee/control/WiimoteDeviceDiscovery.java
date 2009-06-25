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
package org.wiigee.control;

import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.wiigee.util.Log;

import org.wiigee.device.Wiimote;

public class WiimoteDeviceDiscovery implements DiscoveryListener {

    private Vector<RemoteDevice> devices;
    private boolean isInquiring;
    private final Object lock;

    public WiimoteDeviceDiscovery(Object lock) {
        super();
        this.lock = lock;
        this.devices = new Vector<RemoteDevice>();
    }

    public void deviceDiscovered(RemoteDevice newdevice, DeviceClass devclass) {
        Log.write("Device discovered: " + newdevice.getBluetoothAddress() + " - ");
        // add the device to the vector
        if (!devices.contains(newdevice) &&
                devclass.getMajorDeviceClass() == 1280 &&
                devclass.getMinorDeviceClass() == 4) {
            Log.write("Is a Wiimote!");
            devices.addElement(newdevice);
        } else {
            Log.write("Is NOT a Wiimote OR you're using an incompatible Bluetooth Stack.");
        }
    }

    public void inquiryCompleted(int discType) {
        switch (discType) {
            case WiimoteDeviceDiscovery.INQUIRY_COMPLETED:
                Log.write("Inquiry completed.");
                break;

            case WiimoteDeviceDiscovery.INQUIRY_ERROR:
                Log.write("Inquiry error.");
                break;

            case WiimoteDeviceDiscovery.INQUIRY_TERMINATED:
                Log.write("Inquiry terminated.");
                break;
        }
        synchronized (this.lock) {
            this.lock.notify();
        }
    }

    public void serviceSearchCompleted(int arg0, int arg1) {
        // not necessary
    }

    public void servicesDiscovered(int arg0, ServiceRecord[] arg1) {
        // not necessary
    }

    public boolean isInquirying() {
        return this.isInquiring;
    }

    public Vector<Wiimote> getDiscoveredWiimotes() throws IOException {
        Vector<Wiimote> wiimotes = new Vector<Wiimote>();

        for (int i = 0; i < devices.size(); i++) {
            wiimotes.add(new Wiimote(devices.elementAt(i).getBluetoothAddress(), true, true));
        }

        return wiimotes;
    }
}
