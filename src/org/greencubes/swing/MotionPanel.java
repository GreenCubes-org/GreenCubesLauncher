package org.greencubes.swing;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MotionPanel extends JPanel {
	
	private Point initialClick;
	
	public MotionPanel(final JFrame parent) {
		
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				initialClick = e.getPoint();
				super.mousePressed(e);
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			
			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
				// get location of Window
				int thisX = parent.getLocation().x;
				int thisY = parent.getLocation().y;
				
				// Determine how much the mouse moved since the initial click
				int xMoved = (thisX + e.getX()) - (thisX + initialClick.x);
				int yMoved = (thisY + e.getY()) - (thisY + initialClick.y);
				
				// Move window to this position
				int X = thisX + xMoved;
				int Y = thisY + yMoved;
				parent.setLocation(X, Y);
			}
		});
	}
}