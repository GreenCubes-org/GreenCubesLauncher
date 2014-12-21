package org.greencubes.swing;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

public class UndecoratedJFrame extends JFrame {
	
	private Point initialClick;
	private ComponentResizer resizer;
	
	public UndecoratedJFrame(String title) {
		super(title);
		// Hack to use right maximized bound with udecorated window.
		// Otherwise it will overlap system panel.
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = env.getMaximumWindowBounds();
		setMaximizedBounds(bounds);
		
		setUndecorated(true);
		
		resizer = new ComponentResizer();
		resizer.setSnapSize(new Dimension(10, 10));
		resizer.registerComponent(this);
		
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.isConsumed() || !resizer.isEnabled())
					return;
				initialClick = e.getPoint();
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(e.isConsumed() || !resizer.isEnabled())
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
	
	@Override
	public void setResizable(boolean resizable) {
		resizer.setEnabled(resizable);
	}
	
	@Override
	public boolean isResizable() {
		return resizer.isEnabled();
	}
}
