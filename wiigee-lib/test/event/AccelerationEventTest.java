/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package event;

import org.wiigee.event.AccelerationEvent;
import org.wiigee.device.Device;
import junit.framework.TestCase;

/**
 *
 * @author bepo
 */
public class AccelerationEventTest extends TestCase {
    
    public AccelerationEventTest(String testName) {
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
     * Test of getSource method, of class AccelerationEvent.
     */
    public void testGetSource() {
        System.out.println("getSource");
        AccelerationEvent instance = null;
        Device expResult = null;
        Device result = instance.getSource();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getX method, of class AccelerationEvent.
     */
    public void testGetX() {
        System.out.println("getX");
        AccelerationEvent instance = null;
        double expResult = 0.0;
        double result = instance.getX();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getY method, of class AccelerationEvent.
     */
    public void testGetY() {
        System.out.println("getY");
        AccelerationEvent instance = null;
        double expResult = 0.0;
        double result = instance.getY();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZ method, of class AccelerationEvent.
     */
    public void testGetZ() {
        System.out.println("getZ");
        AccelerationEvent instance = null;
        double expResult = 0.0;
        double result = instance.getZ();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAbsValue method, of class AccelerationEvent.
     */
    public void testGetAbsValue() {
        System.out.println("getAbsValue");
        AccelerationEvent instance = null;
        double expResult = 0.0;
        double result = instance.getAbsValue();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
