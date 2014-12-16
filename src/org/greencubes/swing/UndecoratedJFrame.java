package org.greencubes.swing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

public class UndecoratedJFrame extends JFrame {
	
	private Point initialClick;
	
	public UndecoratedJFrame(String title) {
		super(title);
		
		setUndecorated(true);
		
		ComponentResizer cr = new ComponentResizer();
		cr.setSnapSize(new Dimension(10, 10));
		cr.registerComponent(this);
		
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.isConsumed())
					return;
				initialClick = e.getPoint();
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(e.isConsumed())
					return;
				int thisX = UndecoratedJFrame.this.getLocation().x;
				int thisY = UndecoratedJFrame.this.getLocation().y;
				int xMoved = (thisX + e.getX()) - (thisX + initialClick.x);
				int yMoved = (thisY + e.getY()) - (thisY + initialClick.y);
				int X = thisX + xMoved;
				int Y = thisY + yMoved;
				UndecoratedJFrame.this.setLocation(X, Y);
			}
		});
	}
}
