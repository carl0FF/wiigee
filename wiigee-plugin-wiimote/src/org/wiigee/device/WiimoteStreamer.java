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
import java.util.EventObject;

import java.util.Vector;
import javax.bluetooth.L2CAPConnection;

import org.wiigee.util.Log;

/**
 * This class listens to data sended by the wiimote and generates specific
 * events for acceleration, buttonpress, ...
 * 
 * @author Benjamin 'BePo' Poppinga
 * 
 */
public class WiimoteStreamer extends Thread {

    private boolean running;
    private int buttonstate;
    private double x0, x1, y0, y1, z0, z1;
    private double psi0, theta0, phi0;
    private boolean wmpcalibrated;
    private int calibrationcounter;
    private Vector<double[]> calibrationsequence;
    private Wiimote wiimote;
    private L2CAPConnection receiveCon;
    private EventObject lastevent;

    protected WiimoteStreamer(Wiimote wiimote) {
        this.wiimote = wiimote;
        this.receiveCon = wiimote.getReceiveConnection();
        this.buttonstate = 0;
        Log.write("WiimoteStreamer initialized...");
    }

    @Override
    public void run() {
        Log.write("WiimoteStreamer running...");
        this.running = true;
        this.calibrationcounter = 0;
        this.calibrationsequence = new Vector<double[]>();

        try {
            while (running) {
                // connection has data and we're ready.

                byte[] b = this.getRaw(); // blocks application

                // Log.write("");

                // debug output
				/* for(int i=0; i<b.length; i++) {
                 * System.out.print((int)b[i]&0xFF); if(i==input.length-1) {
                 * System.out.println(""); } else { System.out.print(":"); } }
                 */


                if((b[1] & 0xFF) == 0x31) {
                    this.handleButtonData(new byte[] { b[2], b[3] });
                    this.handleAccelerationData(new byte[] { b[4], b[5], b[6] });
                    //Log.write("0x31: Button + Acc");
                }
                else if ((b[1] & 0xFF) == 0x33) {
                    this.handleButtonData(new byte[] { b[2], b[3] });
                    this.handleAccelerationData(new byte[]{b[4], b[5], b[6]});
                    this.handleInfraredData(new byte[]{b[7], b[8], b[9],
                                            b[10], b[11], b[12],
                                            b[13], b[14], b[15],
                                            b[16], b[17], b[18]});
                    //Log.write("0x33: Button + Acc + Irda");
                }
                else if ((b[1] & 0xFF) == 0x37) {
                    this.handleButtonData(new byte[] { b[2], b[3] });
                    this.handleAccelerationData(new byte[]{b[4], b[5], b[6]});
                    this.handleInfraredData(
                            new byte[]{b[7], b[8], b[9], b[10], b[11], b[12],
                                       b[13], b[14], b[15], b[16]});
                    this.handleWiiMotionPlusData(
                            new byte[]{b[17], b[18], b[19], b[20], b[21], b[22]});
                    //Log.write("0x37: Button + Acc + Ext");
                }
                else if ((b[1] & 0xFF) == 0x21) {
                    this.handleButtonData(new byte[] { b[2], b[3] });

                    // calibration data
                    if (((b[5] & 0xFF) == 0x00) && ((b[6] & 0xFF) == 0x20)) {
                        this.handleCalibrationData(
                                new byte[]{b[7], b[8], b[9], b[11], b[12], b[13]});
                        // Log.write("0x21: Calibration result");
                    } else {
                        this.handleRawDataAnswer(
                                new byte[]{b[5], b[6]},
                                new byte[]{b[7], b[8], b[9], b[10], b[11], b[12],
                                    b[13], b[14], b[15], b[16], b[17], b[18], b[19], b[20],
                                    b[21], b[22]
                                });
                        // Log.write("0x21: Raw data answer");
                    }

                }
                else if ((b[1] & 0xFF) == (byte) 0x3d) {
                    Log.write("0x3D: Ext only");
                }
                else {
                    Log.write("Unknown data retrieved.");
                    this.printBytes(b);
                }



            } // while(running)

        } catch (IOException e) {
            Log.write("Streamer: Connection to Wiimote lost.");
            this.running = false;
        }
    }

    /**
     * Handles calibration Data
     *
     * @param data An array of bytes, containing the data.
     */
    private void handleCalibrationData(byte[] data) {
        this.x0 = data[0] & 0xFF;
        this.y0 = data[1] & 0xFF;
        this.z0 = data[2] & 0xFF;
        this.x1 = data[3] & 0xFF;
        this.y1 = data[4] & 0xFF;
        this.z1 = data[5] & 0xFF;
        Log.write("Autocalibration successful!");
    }

    /**
     * Handles a raw data answer: It just prints it out.
     *
     * @param offset The offset of the maybe read data.
     * @param data The data itself.
     */
    private void handleRawDataAnswer(byte[] offset, byte[] data) {
        String out = "";
        String[] o = this.byte2hex(offset);
        String[] d = this.byte2hex(data);
        out += "READ " + o[0] + "" + o[1] + ": ";
        for (int i = 0; i < d.length; i++) {
            out += d[i] + " ";
        }
        Log.write(out);
    }

    /**
     * Handles the Wii Motion Plus data to generate orientation
     * events on the Wiimote.
     *
     * @param data The data containing the raw rotation speeds.
     */
    private void handleWiiMotionPlusData(byte[] data) {

        // fixed values until calibration procedure is known
        //int psi0 = 8265;
        //int theta0 = 7963;
        //int phi0 = 7923;

        //this.printBytes(new byte[]{ data[3], data[4], data[5]});

        int psiL = (data[0] & 0xFF);
        int thetaL = (data[1] & 0xFF);
        int phiL = (data[2] & 0xFF);

        // cut two lower bits of UPPER values, shift right
        int psiU = ((data[3] & 0xFC) << 6);
        int thetaU = ((data[4] & 0xFC) << 6);
        int phiU = ((data[5] & 0xFC) << 6);

        // add the two values
        int psiRAW = psiU + psiL;
        int thetaRAW = thetaU + thetaL;
        int phiRAW = phiU + phiL;

        // average of 50 samples for calibration
        if (!this.wmpcalibrated) {
            if (this.calibrationcounter++ < 50) {
                this.calibrationsequence.add(new double[]{psiRAW, thetaRAW, phiRAW});
            } else {
                this.calibrateWiiMotionPlus();
            }
        } else { // is calibrated
            // calculate degrees per second movement
            double psi = (double) (psiRAW - psi0) / 20.0;
            double theta = (double) (thetaRAW - theta0) / 20.0;
            double phi = (double) (phiRAW - phi0) / 20.0;

            this.wiimote.fireRotationSpeedEvent(new double[]{-psi, -theta, -phi});
        }

    }

    /**
     * Build an average of the presaved rotation sequences to have a reference
     * value for 'not rotating'. This is a kind of calibration until the original
     * calibration procedure for the wii motion plus is known.
     */
    private void calibrateWiiMotionPlus() {
        for (int i = 0; i < this.calibrationsequence.size(); i++) {
            this.psi0 += this.calibrationsequence.elementAt(i)[0];
            this.theta0 += this.calibrationsequence.elementAt(i)[1];
            this.phi0 += this.calibrationsequence.elementAt(i)[2];
        }
        this.psi0 /= this.calibrationsequence.size();
        this.theta0 /= this.calibrationsequence.size();
        this.phi0 /= this.calibrationsequence.size();
        this.wmpcalibrated = true;
        Log.write("Wii Motion Plus calibrated manually!");
    }

    /**
     * Handles the raw infrared data, containing the spot positions
     * and the intensities of the spots. At the moment only the
     * 12 Byte irda raw data is supported.
     *
     * @param data 12 Bytes of raw irda data.
     */
    private void handleInfraredData(byte[] data) {
        int[][] coordinates = new int[4][2];
        int[] size = new int[] { 1, 1, 1, 1 };
        int j = 0;

        // normal mode
        if(data.length == 10) {
            for(int i=0; i<10; i+=5) { // for each IR fife-byte segment
                int tailX1 = data[i] & 0xFF;
                int tailY1 = data[i+1] & 0xFF;
                int preY1 = (data[i+2] & 0xC0) << 2;
                int preX1 = (data[i+2] & 0x30) << 4;
                int preY2 = (data[i+2] & 0x0C) << 6;
                int preX2 = (data[i+2] & 0x03) << 8;
                int tailX2 = data[i+3] & 0xFF;
                int tailY2 = data[i+4] & 0xFF;

                coordinates[j][0] = tailX1 + preX1;
                coordinates[j][1] = tailY1 + preY1;
                coordinates[j+1][0] = tailX2 + preX2;
                coordinates[j+1][1] = tailY2 + preY2;
                j+=2;
            }
            
        } else if (data.length == 12) { // extended mode
            for (int i = 0; i < 12; i += 3) { // for each IR byte
                // triple
                int tailX = data[i] & 0xFF;
                int tailY = data[i + 1] & 0xFF;
                int preY = (data[i + 2] & 0xC0) << 2;
                int preX = (data[i + 2] & 0x30) << 4;

                coordinates[j][0] = tailX + preX;
                coordinates[j][1] = tailY + preY;
                size[j++] = (data[i + 2] & 0x0F);
            }

        }

        this.wiimote.fireInfraredEvent(coordinates, size);

    }

    /**
     * Handles the retrieved acceleration data and fires
     * corresponding Acceleration Event on the Device.
     *
     * @param data
     */
    private void handleAccelerationData(byte[] data) {
        // convert to int.
        int xraw = (data[0] & 0xFF);
        int yraw = (data[1] & 0xFF);
        int zraw = (data[2] & 0xFF);

        // calculate acceleration with calibration data.
        double x = (double) (xraw - x0) / (double) (x1 - x0);
        double y = (double) (yraw - y0) / (double) (y1 - y0);
        double z = (double) (zraw - z0) / (double) (z1 - z0);

        this.wiimote.fireAccelerationEvent(new double[]{x, y, z});
    }

    /**
     * Decodes the two bytes with the button press and release bits.
     *
     * @param a First button byte.
     * @param b Second button byte.
     */
    private void handleButtonData(byte[] data) {
        byte first = (byte)(data[0] & 0xFF);
        byte second = (byte)(data[1] & 0xFF);
        int newbuttons = (first<<8) + second;

        int delta = this.buttonstate ^ newbuttons; // XOR

        int shift = 0x0001;
        while(shift<0x1000) {
            if(shift!=0x0020 && shift!=0x0040) { // reserved bytes
                if((delta&shift)==shift) { // change detected
                    if((newbuttons&shift)==shift) { // press detected
                        this.wiimote.fireButtonPressedEvent(shift);
                    } else { // release detected
                        this.wiimote.fireButtonReleasedEvent(shift);
                    }
                }
            }
            shift<<=1;
        }

        this.buttonstate = newbuttons;
    }

    /**
     * Gets 23 bytes out of the Receive Connection Stream.
     *
     * @return The 23 retrieved bytes.
     * @throws java.io.IOException
     */
    private byte[] getRaw() throws IOException {
        byte[] b = new byte[23];
        this.receiveCon.receive(b);
        return b;
    }

    /**
     * Stops this thread.
     */
    protected void stopThread() {
        this.running = false;
    }

    /**
     * true if thread is running.
     * @return true if thread is running, false otherwise.
     */
    protected boolean isRunning() {
        return this.running;
    }

    /**
     * Prints a byte stream as hex string.
     * @param b
     */
    private void printBytes(byte[] b) {
        String out = "";
        String[] s = this.byte2hex(b);
        for (int i = 0; i < s.length; i++) {
            out += " " + s[i];
        }
        Log.write(out);
    }

    /**
     * Converts a byte array to a string array.
     * 
     * @param b
     * @return
     */
    private String[] byte2hex(byte[] b) {
        String[] out = new String[b.length];
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                out[n] = ("0" + stmp).toUpperCase();
            } else {
                out[n] = stmp.toUpperCase();
            }
        }
        return out;
    }
}
