package wiigeegui;

import org.wiigee.device.Wiimote;
import org.wiigee.event.ButtonPressedEvent;
import org.wiigee.event.ButtonReleasedEvent;
import java.awt.Graphics;
import java.util.EventObject;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author bepo
 */
public class WiimotePanel extends JPanel {

    ImageIcon current = new ImageIcon(getClass().getResource("/img/WiiMote_released.png"));
	ImageIcon released = new ImageIcon(getClass().getResource("/img/WiiMote_released.png"));
	ImageIcon pressed2 = new ImageIcon(getClass().getResource("/img/WiiMote_2_pressed.png"));
	ImageIcon pressedA = new ImageIcon(getClass().getResource("/img/WiiMote_A_pressed.png"));
	ImageIcon pressedB = new ImageIcon(getClass().getResource("/img/WiiMote_B_pressed.png"));
	ImageIcon pressedHOME = new ImageIcon(getClass().getResource("/img/WiiMote_HOME_pressed.png"));

    @Override
    public final void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.drawImage(this.current.getImage(),0,0,this);
    }

	public void changeState(EventObject event) {
		if(event instanceof ButtonPressedEvent) {
			if(((ButtonPressedEvent)event).getButton()==Wiimote.BUTTON_2) {
				// Button 2
				this.current = this.pressed2;
				this.repaint();
			} else if(((ButtonPressedEvent)event).getButton()==Wiimote.BUTTON_A) {
				// Button A
				this.current = this.pressedA;
				this.repaint();
			} else if(((ButtonPressedEvent)event).getButton()==Wiimote.BUTTON_B) {
				// Button B
				this.current = this.pressedB;
				this.repaint();
			} else if(((ButtonPressedEvent)event).getButton()==Wiimote.BUTTON_HOME) {
				// Button HOME
				this.current = this.pressedHOME;
				this.repaint();
			}
		} else if(event instanceof ButtonReleasedEvent) {
			current = this.released;
			this.repaint();
		}
	} // changeState

}
