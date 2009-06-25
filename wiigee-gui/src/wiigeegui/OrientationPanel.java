package wiigeegui;

import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;
import org.wiigee.event.AccelerationEvent;
import java.awt.Color;
import java.awt.Graphics;
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

    TransformGroup transgroup;

    public OrientationPanel() {
        super();

        System.out.println("loading canvas3d");

        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);
        canvas.setFocusable(true);
        canvas.requestFocus();
        SimpleUniverse universe = new SimpleUniverse(canvas);
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

        // fixed size at the moment...
        canvas.setSize(423, 230);
        add(canvas);

        this.repaint();
    }

    public void setRotation(RotationEvent e) {
        Transform3D rotationX = new Transform3D();
        rotationX.rotX(Math.toRadians(-e.getYaw()));

        Transform3D rotationY = new Transform3D();
        rotationY.rotY(Math.toRadians(e.getPitch()));

        Transform3D rotationZ = new Transform3D();
        rotationZ.rotZ(Math.toRadians(e.getRoll()));

        rotationX.mul(rotationY);
        rotationX.mul(rotationZ);

        this.transgroup.setTransform(rotationX);
    }

}
