/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wiigeegui;

/**
 *
 * @author bepo
 */
public class wiigeemain {

    static Frontend frontend;
    
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        // setup wiimote
        // manually connect
        try {            
            frontend = new Frontend();

            // show gui
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    frontend.setVisible(true);
                }
            });
        } catch (Exception e) {
            System.out.println("caught Exception.");
            e.printStackTrace();
            System.exit(1);
        }
    }

}
