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
package wiigeegui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Robot;
import java.awt.Toolkit;
import org.wiigee.event.InfraredEvent;
import org.wiigee.util.Log;

/**
 *
 * @author Benjamin 'BePo' Poppinga
 */
public class InfraredPanel extends JPanel {

    // Constants
    public static final double DISTANCE_BETWEEN_SPOTS = 0.335; // meter

    private boolean robotMouseEnabled;
    private int[] middle;
    private int[] pointer;
    private int[][] coordinates;
    private boolean[] valid;
    private double distance;
    private int lastdeltaX;
    private int lastdeltaY;


    public InfraredPanel() {
        super();
        this.robotMouseEnabled = false;
        this.distance = 0.0;
        this.lastdeltaX = 0;
        this.lastdeltaY= 0;
        this.middle = new int[] { 0, 0 };
        this.pointer = new int[] { 0, 0 };
    }

    public void setRobotMouseEnabled(boolean enabled) {
        this.robotMouseEnabled = enabled;
    }

    public void setInfrared(InfraredEvent event) {
        this.coordinates = event.getCoordinates();
        this.valid = event.getValids();

        int x1 = this.coordinates[0][0];
        int y1 = this.coordinates[0][1];
        int x2 = this.coordinates[1][0];
        int y2 = this.coordinates[1][1];
        

        // calculate pointing direction
        if(x1<1023 && x2<1023) {
            // middle in view, used for pointer calculation
            int dx = x2-x1;
            int dy = y2-y1;
            this.middle[0] = x1+(dx/2);
            this.middle[1] = y1+(dy/2);
            this.pointer[0] = 1024-this.middle[0];
            this.pointer[1] = 768-this.middle[1];

            this.lastdeltaX = dx;
            this.lastdeltaY = dy;
        } else if(x1<1023 && x2>=1023) {
            // middle not in view, P1 in view
            this.pointer[0] = 1024-x1-(int)(this.lastdeltaX*0.5);
            this.pointer[1] = 768-y1-(int)(this.lastdeltaY*0.5);
        } else if(x1>=1023 && x2<1023) {
            // middle not in view, P2 in view
            this.pointer[0] = 1024-x2+(int)(this.lastdeltaX*0.5);
            this.pointer[1] = 768-y2+(int)(this.lastdeltaY*0.5);
        }

        if(this.robotMouseEnabled) {
            this.updateRobotMouse();
        }
        this.repaint();
    }

    @Override
    public final void paintComponent(Graphics graphics) {
        // reset
        graphics.clearRect(0, 0, this.getWidth(), this.getHeight());
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, this.getWidth(), this.getHeight());

        if (this.coordinates != null && this.valid != null) {
            if (this.valid[0]) {
                graphics.setColor(Color.RED);
                graphics.fillOval(coordinates[0][0] * this.getWidth() / 1024, (768-coordinates[0][1]) * this.getHeight() / 768, 20, 20);
            }

            if (this.valid[1]) {
                graphics.setColor(Color.GREEN);
                graphics.fillOval(coordinates[1][0] * this.getWidth() / 1024, (768-coordinates[1][1]) * this.getHeight() / 768, 20, 20);
            }

            if (this.valid[2]) {
                graphics.setColor(Color.YELLOW);
                graphics.fillOval(coordinates[2][0] * this.getWidth() / 1024, (768-coordinates[2][1]) * this.getHeight() / 768, 20, 20);
            }

            if (this.valid[3]) {
                graphics.setColor(Color.BLUE);
                graphics.fillOval(coordinates[3][0] * this.getWidth() / 1024, (768-coordinates[3][1]) * this.getHeight() / 768, 20, 20);
            }

            // draw middle
            graphics.setColor(Color.BLACK);
            graphics.fillOval(middle[0] * this.getWidth() / 1024, (768-middle[1]) * this.getHeight() / 768, 10, 10);

            // draw pointer
            graphics.setColor(Color.PINK);
            graphics.fillOval(pointer[0] * this.getWidth() / 1024, (768-pointer[1]) * this.getHeight() / 768, 10, 10);

        }
    }

    private void updateRobotMouse() {
        try {
            Dimension display = (Toolkit.getDefaultToolkit()).getScreenSize();
            new Robot().mouseMove(pointer[0]*display.width/1024, (768-pointer[1])*display.height/768);
        } catch (Exception ex) {
            Log.write("Error while setting robot mouse coordinates:");
            ex.printStackTrace();
        }
    }

    private double getDistanceToSensorbar(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2-x1);
        int dy = Math.abs(y2-y1);
        double pixDist = Math.sqrt(dx*dx+dy*dy);

        double angle = 41.0*pixDist/1024;
        //System.out.println("view angle="+angle);

        double realDist = (DISTANCE_BETWEEN_SPOTS/2)/(Math.tan(Math.toRadians(angle/2)));

        return realDist;
    }

    private double getRotation(int x1, int y1, int x2, int y2) {
        int dx = x2-x1;
        int dy = y2-y1;
        return Math.toDegrees(Math.atan2(dy, dx));
    }
}
