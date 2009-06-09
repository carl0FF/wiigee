/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wiigee.logic;

import java.util.Vector;
import junit.framework.TestCase;
import org.wiigee.device.Device;
import org.wiigee.event.AccelerationEvent;

/**
 *
 * @author bepo
 */
public class ClassifierTest extends TestCase {
    
    public ClassifierTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of classifyGesture method, of class Classifier.
     */
    public void testClassifyGesture() {

        // create a pseudo-device
        Device d = new Device();

        // create 3 gestures
        Gesture g0 = new Gesture();
        Gesture g1 = new Gesture();
        Gesture g2 = new Gesture();

        g0.add(new AccelerationEvent(d, 0.0, 0.0, 0.0, 0.0));
        g0.add(new AccelerationEvent(d, 1.0, 1.0, 1.0, 0.0));
        g0.add(new AccelerationEvent(d, 2.0, 2.0, 2.0, 0.0));
        g0.add(new AccelerationEvent(d, 1.0, 1.0, 1.0, 0.0));

        g1.add(new AccelerationEvent(d, -1.0, 1.0, -1.0, 0.0));
        g1.add(new AccelerationEvent(d, -1.0, 3.0, -1.0, 0.0));
        g1.add(new AccelerationEvent(d, -1.0, 1.0, -1.0, 0.0));
        g1.add(new AccelerationEvent(d, -1.0, 3.0, -1.0, 0.0));

        g2.add(new AccelerationEvent(d, -2.0, -2.0, -2.0, -2.0));
        g2.add(new AccelerationEvent(d, -2.0, -2.0, -2.0, -2.0));
        g2.add(new AccelerationEvent(d, -2.0, -2.0, -2.0, -2.0));
        g2.add(new AccelerationEvent(d, -2.0, -2.0, -2.0, -2.0));

        // create 3 gesturesets with 1 gesture each
        Vector<Gesture> gs0 = new Vector<Gesture>();
        Vector<Gesture> gs1 = new Vector<Gesture>();
        Vector<Gesture> gs2 = new Vector<Gesture>();
        gs0.add(g0);
        gs1.add(g1);
        gs2.add(g2);

        // create three gesturemodels and train these
        GestureModel gm0 = new GestureModel();
        GestureModel gm1 = new GestureModel();
        GestureModel gm2 = new GestureModel();
        gm0.train(gs0);
        gm1.train(gs1);
        gm2.train(gs2);

        // create a classifier and add gesturemodels
        Classifier classifier = new Classifier();
        classifier.addGestureModel(gm0);
        classifier.addGestureModel(gm1);
        classifier.addGestureModel(gm2);

        // classify gesture
        int result0 = classifier.classifyGesture(g0);
        int result1 = classifier.classifyGesture(g1);
        int result2 = classifier.classifyGesture(g2);

        if((result0 != 0) ||
           (result1 != 1) ||
           (result2 != 2)) {
            fail("Wrong gesture classified.");
        }

    }



    /**
     * Test of addGestureModel method, of class Classifier.
     */
    public void testAddGestureModel() {
    }

    /**
     * Test of getGestureModel method, of class Classifier.
     */
    public void testGetGestureModel() {
    }

    /**
     * Test of getGestureModels method, of class Classifier.
     */
    public void testGetGestureModels() {
    }

    /**
     * Test of getCountOfGestures method, of class Classifier.
     */
    public void testGetCountOfGestures() {
    }

    /**
     * Test of clear method, of class Classifier.
     */
    public void testClear() {
    }

}
