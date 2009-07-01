package wiigeegui;

import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import org.wiigee.event.AccelerationEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentListener;
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
