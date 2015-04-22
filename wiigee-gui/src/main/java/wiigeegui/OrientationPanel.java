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

import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import org.wiigee.event.RotationEvent;

/**
 *
 * @author Benjamin 'BePo' Poppinga
 */
public class OrientationPanel extends JPanel {

    Canvas3D canvas;
    TransformGroup transgroup;

    public OrientationPanel() {
        super();

        System.out.println("loading canvas3d");

        this.setLayout(new BorderLayout());

        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        this.canvas = new Canvas3D(config);
        this.canvas.setFocusable(true);
        this.canvas.requestFocus();
        SimpleUniverse universe = new SimpleUniverse(this.canvas);
        universe.getViewingPlatform().setNominalViewingTransform();

        //Add components
        BranchGroup objRoot = new BranchGroup();
        ColorCube colorCube = new ColorCube(0.4);
        
        this.transgroup = new TransformGroup();
        this.transgroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        this.transgroup.addChild(colorCube);

        objRoot.addChild(this.transgroup);
        objRoot.compile();
        
        universe.addBranchGraph(objRoot);

        add(this.canvas);
    }

    public void setRotation(RotationEvent e) {
        Transform3D rotationX = new Transform3D();
        rotationX.rotX(Math.toRadians(e.getPitch()));

        Transform3D rotationY = new Transform3D();
        rotationY.rotY(Math.toRadians(-e.getYaw()));

        Transform3D rotationZ = new Transform3D();
        rotationZ.rotZ(Math.toRadians(-e.getRoll()));

        rotationX.mul(rotationY);
        rotationX.mul(rotationZ);

        this.transgroup.setTransform(rotationX);
    }

}
