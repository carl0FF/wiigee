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
import java.util.EventObject;

import javax.bluetooth.L2CAPConnection;

import event.ButtonPressedEvent;
import event.ButtonReleasedEvent;
import util.Log;

/**
 * This class listens to data sended by the wiimote and generates specific
 * events for acceleration, buttonpress, ...
 * 
 * @author Benjamin 'BePo' Poppinga
 * 
 */
public class WiimoteStreamer extends Thread {

	boolean running;
	double x0, x1, y0, y1, z0, z1;

	Wiimote wiimote;

	private L2CAPConnection receiveCon;

	EventObject lastevent;

	protected WiimoteStreamer(Wiimote wiimote) {
		this.wiimote = wiimote;
		this.receiveCon = wiimote.getReceiveConnection();
	}

	public void run() {
		this.running = true;
		int xraw, yraw, zraw;
		double x, y, z;

		try {
			while (running) {
				// connection has data and we're ready.
				
				byte[] b = this.getRaw(); // blocks application
				String[] input = this.byte2hex(b);

				// debug output
				/*
				 * for(int i=0; i<input.length; i++) {
				 * System.out.print(input[i]); if(i==input.length-1) {
				 * System.out.println(""); } else { System.out.print(":"); } }
				 *  /* for(int i=0; i<b.length; i++) {
				 * System.out.print((int)b[i]&0xFF); if(i==input.length-1) {
				 * System.out.println(""); } else { System.out.print(":"); } }
				 */

				// wiimote wants to tell the calibration data
				if ((b[1] & 0xFF) == 33) {
					// if(((b[1] & 0xFF) & 0x21 ) == 0x21) {
					this.x0 = b[7] & 0xFF;
					this.y0 = b[8] & 0xFF;
					this.z0 = b[9] & 0xFF;
					this.x1 = b[11] & 0xFF;
					this.y1 = b[12] & 0xFF;
					this.z1 = b[13] & 0xFF;
					Log.write("Autocalibration successful!");
					continue;
				}

				// infrared is enabled, so have a look at the last bytes
				if (this.wiimote.infraredEnabled()
						&& (((b[1] & 0xFF) & 0x31) == 0x31 || ((b[1] & 0xFF) & 0x33) == 0x33)) {
					int[][] coordinates = new int[4][2];
					int[] size = new int[4];
					int j = 0;

					for (int i = 7; i < 18; i += 3) { // for each IR byte
														// triple
						int tailX = b[i] & 0xFF;
						int tailY = b[i + 1] & 0xFF;
						int preY = (b[i + 2] & 0xC0) << 2;
						int preX = (b[i + 2] & 0x30) << 4;

						coordinates[j][0] = tailX + preX;
						coordinates[j][1] = tailY + preY;
						size[j++] = (b[i + 2] & 0x0F);
					}

					this.wiimote.fireInfraredEvent(coordinates, size);
				}

				// if the wiimote is sending acceleration data...
				if (this.wiimote.accelerationEnabled()
						&& (((b[1] & 0xFF) & 0x31) == 0x31 || ((b[1] & 0xFF) & 0x33) == 0x33)) {

					/*
					 * calculation of acceleration vectors starts here. further
					 * information about normation exist in the public papers or
					 * the various www-sources.
					 * 
					 */
					xraw = (b[4] & 0xFF);
					yraw = (b[5] & 0xFF);
					zraw = (b[6] & 0xFF);

					x = (double) (xraw - x0) / (double) (x1 - x0);
					y = (double) (yraw - y0) / (double) (y1 - y0);
					z = (double) (zraw - z0) / (double) (z1 - z0);

					// try to fire event, there could be filters added to the
					// wiimote class which may prevents from firing.
					this.wiimote.fireAccelerationEvent(new double[] {x, y, z});

				}

				// Button 1 matches button "2" on wiimote
				if (((b[3] & 0xFF) & 0x01) == 0x01
						&& !(this.lastevent instanceof ButtonPressedEvent)) {
					this.wiimote.fireButtonPressedEvent(1);
					this.lastevent = new ButtonPressedEvent(
							this.wiimote, 1);
					continue;
				}

				// Button 2 matches button "1" on wiimote
				else if (((b[3] & 0xFF) & 0x02) == 0x02
						&& !(this.lastevent instanceof ButtonPressedEvent)) {
					this.wiimote.fireButtonPressedEvent(2);
					this.lastevent = new ButtonPressedEvent(
							this.wiimote, 2);
					continue;
				}

				// Button 3 matches button "B" on wiimote
				else if (((b[3] & 0xFF) & 0x04) == 0x04
						&& !(this.lastevent instanceof ButtonPressedEvent)) {
					this.wiimote.fireButtonPressedEvent(3);
					this.lastevent = new ButtonPressedEvent(
							this.wiimote, 3);
					continue;
				}

				// Button 4 matches button "A" on wiimote
				else if (((b[3] & 0xFF) & 0x08) == 0x08
						&& !(this.lastevent instanceof ButtonPressedEvent)) {
					this.wiimote.fireButtonPressedEvent(4);
					this.lastevent = new ButtonPressedEvent(
							this.wiimote, 4);
					continue;
				}

				// Button 5 matches button "MINUS" on wiimote
				else if (((b[3] & 0xFF) & 0x10) == 0x10
						&& !(this.lastevent instanceof ButtonPressedEvent)) {
					this.wiimote.fireButtonPressedEvent(5);
					this.lastevent = new ButtonPressedEvent(
							this.wiimote, 5);
					continue;
				}

				// Button 6 unknown
				// Button 7 unknown

				// Button 8 matches button "HOME" on wiimote
				else if (((b[3] & 0xFF) & 0x80) == 0x80
						&& !(this.lastevent instanceof ButtonPressedEvent)) {
					this.wiimote.fireButtonPressedEvent(8);
					this.lastevent = new ButtonPressedEvent(
							this.wiimote, 8);
					continue;
				}

				// Button 9 matches "CROSS LEFT" on wiimote
				else if (((b[2] & 0xFF) & 0x01) == 0x01
						&& !(this.lastevent instanceof ButtonPressedEvent)) {
					this.wiimote.fireButtonPressedEvent(9);
					this.lastevent = new ButtonPressedEvent(
							this.wiimote, 9);
					continue;
				}

				// Button 10 matches "CROSS RIGHT" on wiimote
				else if (((b[2] & 0xFF) & 0x02) == 0x02
						&& !(this.lastevent instanceof ButtonPressedEvent)) {
					this.wiimote.fireButtonPressedEvent(10);
					this.lastevent = new ButtonPressedEvent(
							this.wiimote, 10);
					continue;
				}

				// Button 11 matches "CROSS DOWN" on wiimote
				else if (((b[2] & 0xFF) & 0x04) == 0x04
						&& !(this.lastevent instanceof ButtonPressedEvent)) {
					this.wiimote.fireButtonPressedEvent(11);
					this.lastevent = new ButtonPressedEvent(
							this.wiimote, 11);
					continue;
				}

				// Button 12 matches "CROSS UP" on wiimote
				else if (((b[2] & 0xFF) & 0x08) == 0x08
						&& !(this.lastevent instanceof ButtonPressedEvent)) {
					this.wiimote.fireButtonPressedEvent(12);
					this.lastevent = new ButtonPressedEvent(
							this.wiimote, 12);
					continue;
				}

				// Button 13 matches button "PLUS" on wiimote
				else if (((b[2] & 0xFF) & 0x10) == 0x10
						&& !(this.lastevent instanceof ButtonPressedEvent)) {
					this.wiimote.fireButtonPressedEvent(13);
					this.lastevent = new ButtonPressedEvent(
							this.wiimote, 13);
					continue;
				}

				// Button 14 unknown
				// Button 15 unknown
				// Button 16 unknown

				// Button released
				else if ((input[2].equals("20") || input[2].equals("40") || input[2]
						.equals("60"))
						&& (input[3].equals("00") || input[3].equals("20")
								|| input[3].equals("40") || input[3]
								.equals("60"))
						&& !(this.lastevent instanceof ButtonReleasedEvent)) {
					this.wiimote.fireButtonReleasedEvent();
					this.lastevent = new ButtonReleasedEvent(
							this.wiimote);
					continue;
				}

			} // while(running)

		} catch (IOException e) {
			Log.write("Streamer: Connection to Wiimote lost.");
			this.running = false;
		}
	}

	private byte[] getRaw() throws IOException {
		byte[] b = new byte[19];
		this.receiveCon.receive(b);
		return b;
	}

	/**
	 * stops this thread.
	 */
	protected void stopThread() {
		this.running = false;
	}

	protected boolean isRunning() {
		return this.running;
	}

	private String[] byte2hex(byte[] b) {
		String[] out = new String[b.length];
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				out[n] = ("0" + stmp).toUpperCase();
			else
				out[n] = stmp.toUpperCase();
		}
		return out;
	}

}
