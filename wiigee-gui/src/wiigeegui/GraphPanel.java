/*
 * wiigee - accelerometerbased gesture recognition
 * Copyright (C) 2007, 2008, 2009, 2010 Benjamin Poppinga
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
package wiigeegui;

import org.wiigee.event.AccelerationEvent;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author Benjamin 'BePo' Poppinga
 */
public class GraphPanel extends JPanel {

    private int time;
    private int currX,  currY,  currZ;
    private int lastX,  lastY,  lastZ;
    private final int GMAX = 3;

    public void accelerate(AccelerationEvent e) {
        lastX = currX;
        lastY = currY;
        lastZ = currZ;
        currX = (int) (e.getX() / GMAX * (this.getHeight() / 2) - 1) + (this.getHeight() / 2) - 1;
        currY = (int) (e.getY() / GMAX * (this.getHeight() / 2) - 1) + (this.getHeight() / 2) - 1;
        currZ = (int) (e.getZ() / GMAX * (this.getHeight() / 2) - 1) + (this.getHeight() / 2) - 1;

        // update time
        time = (time<this.getWidth()) ? time+1 : 0;

        this.repaint();
    }

    @Override
    public final void paintComponent(Graphics graphics) {
        if(time==0) {
            // reset if time is at the beginning
            graphics.clearRect(0, 0, this.getWidth(), this.getHeight());
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, this.getWidth(), this.getHeight());
        }

        // axis
        graphics.setColor(Color.BLACK);
        graphics.drawLine(0, (this.getHeight() / 2) - 1, this.getWidth(), (this.getHeight() / 2) - 1);

        // y-axis labeling
        int spacer = (this.getHeight() / 2) / GMAX;
        for(int i=1; i<GMAX; i++) {
            graphics.drawString(i+"g", 2, (this.getHeight()/2)-i*spacer);
            graphics.drawString("-"+i+"g", 2, (this.getHeight()/2)+i*spacer);
        }

        // Legende anlegen
        graphics.setColor(Color.RED);
        graphics.drawString("X", 5, 13);
        graphics.setColor(Color.GREEN);
        graphics.drawString("Y", 100, 13);
        graphics.setColor(Color.BLUE);
        graphics.drawString("Z", 195, 13);

        // draw lines
        graphics.setColor(Color.RED);
        graphics.drawLine(time, lastX, time, currX);
        graphics.setColor(Color.GREEN);
        graphics.drawLine(time, lastY, time, currY);
        graphics.setColor(Color.BLUE);
        graphics.drawLine(time, lastZ, time, currZ);
    }
}
