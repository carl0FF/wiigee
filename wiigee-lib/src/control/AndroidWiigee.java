/*
 * wiigee - accelerometerbased gesture recognition
 * Copyright (C) 2007, 2008, 2009 Benjamin Poppinga
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
package control;

import device.AndroidDevice;
import event.GestureListener;
import filter.Filter;
import util.Log;

/**
 * This is for using wiigee on Android Smartphones. This port has been
 * initiated by Maarten 'MrSnowflake' Krijn and updated by 'zl25drexel'
 * under the pseudonym 'Andgee'.
 *
 * It has been re-integrated into wiigee on 29th May of 2009.
 *
 * @author Maarten 'MrSnowflake' Krijn
 * @author zl25drexel
 * @author Benjamin 'BePo' Poppinga
 */
public class AndroidWiigee {

    protected static String version = "1.0 alpha";
    protected static String releasedate = "20090529";
    protected static AndroidWiigee instance;

    private AndroidDevice device;

    private AndroidWiigee() {
            device = new AndroidDevice();
    }

    public static synchronized AndroidWiigee getInstance() {
            Log.write("This is AndroidWiigee (Andgee) version "+version+" ("+releasedate+")");
            Log.write("This is an Android adaptation of Wiigee (http://wiigee.sourceforge.net/)");
            Log.write("So many thanks to the Wiigee team for their awsome recognition lib!");

            if(instance == null) {
                    instance = new AndroidWiigee();
                    return instance;
            } else {
                    return instance;
            }
    }

    public void addGestureListener(GestureListener listener) {
            device.addGestureListener(listener);
    }

    public void addFilter(Filter filter) {
            device.addFilter(filter);
    }


    public AndroidDevice getDevice() {
        return device;
    }


    /**
     * Sets the Trainbutton for all wiimotes;
     *
     * @param b Button encoding, see static Wiimote values
     */
    public void setTrainButton(int b) {
            device.setTrainButton(b);
    }

    /**
     * Sets the Recognitionbutton for all wiimotes;
     *
     * @param b Button encoding, see static Wiimote values
     */
    public void setRecognitionButton(int b) {
            device.setRecognitionButton(b);
    }

    /**
     * Sets the CloseGesturebutton for all wiimotes;
     *
     * @param b Button encoding, see static Wiimote values
     */
    public void setCloseGestureButton(int b) {
            device.setCloseGestureButton(b);
    }

}
