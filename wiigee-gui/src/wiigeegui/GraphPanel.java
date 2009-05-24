package wiigeegui;

import event.AccelerationEvent;
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

    public void accelerate(AccelerationEvent e) {
        lastX = currX;
        lastY = currY;
        lastZ = currZ;
        currX = (int) (e.getX() / 5 * (this.getHeight() / 2) - 1) + (this.getHeight() / 2) - 1;
        currY = (int) (e.getY() / 5 * (this.getHeight() / 2) - 1) + (this.getHeight() / 2) - 1;
        currZ = (int) (e.getZ() / 5 * (this.getHeight() / 2) - 1) + (this.getHeight() / 2) - 1;

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
