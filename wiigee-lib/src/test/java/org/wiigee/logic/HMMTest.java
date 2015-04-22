/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wiigee.logic;

import java.util.Vector;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 *
 * @author bepo
 */
public class HMMTest extends TestCase {
    
    public HMMTest(String testName) {
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
     * Probabilistic test of the train method, of class HMM.
     */
    public void testTrain() {
        System.out.println("train");

        HMM instance = new HMM(8, 14);

        // setup a trainsequence
        Vector<int[]> trainsequence = new Vector<int[]>();
        trainsequence.add(new int[] { 0, 1, 2, 1, 6, 4, 3, 2});
        trainsequence.add(new int[] { 0, 1, 2, 2, 6, 3, 3, 1});
        trainsequence.add(new int[] { 0, 1, 2, 2, 6, 3, 2, 2});

        // setup a failing trainsequence
        int[] fail = new int[] { 5, 5, 5, 5, 5, 5, 5, 5};

        // train the hmm
        instance.train(trainsequence);

        double probA = instance.getProbability(trainsequence.elementAt(0));
        double probB = instance.getProbability(trainsequence.elementAt(1));
        double probC = instance.getProbability(trainsequence.elementAt(2));
        double probFAIL = instance.getProbability(fail);

        System.out.println("probA = "+probA);
        System.out.println("probB = "+probB);
        System.out.println("probC = "+probC);
        System.out.println("probFAIL = "+probFAIL);

        if((probA <= 1.0E-10) ||
           (probB <= 1.0E-10) ||
           (probC <= 1.0E-10)) {
            fail("Probabilities to low to be accurate.");
        }

        if(probFAIL > 0.0) {
            fail("Fake probability to high.");
        }
    }

}
