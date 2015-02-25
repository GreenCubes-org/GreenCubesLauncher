package org.greencubes.swing;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

import sun.java2d.SunGraphicsEnvironment;

@SuppressWarnings("restriction")
public class UndecoratedJFrame extends JFrame {
	
	protected Point initialClick;
	protected ComponentResizer resizer;
	
	public UndecoratedJFrame(String title) {
		super(title, GAWTUtil.getBestConfiguration());
		// Hack to use right maximized bound with udecorated window.
		// Otherwise it will overlap system panel.
		GraphicsConfiguration config = getGraphicsConfiguration();
		Rectangle usableBounds = SunGraphicsEnvironment.getUsableBounds(config.getDevice());
		setMaximizedBounds(new Rectangle(0, 0, usableBounds.width, usableBounds.height));
		
		setUndecorated(true);
		
		resizer = new ComponentResizer();
		resizer.setSnapSize(new Dimension(10, 10));
		resizer.registerComponent(this);
		
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.isConsumed()) {
					initialClick = null;
					return;
				}
				initialClick = e.getPoint();
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(e.isConsumed() || initialClick == null)
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
	
	/**
	 * For right result on multimonitor system you should use this function, not
	 * {@code setExtendedState(Frame.MAXIMIZED_BOTH)}
	 */
	public void maximize() {
		// We reset the bounds in case launcher run on
		// multi-monitor system and has ben moved since startup
		GraphicsConfiguration config = getGraphicsConfiguration();
		Rectangle usableBounds = SunGraphicsEnvironment.getUsableBounds(config.getDevice());
		setMaximizedBounds(new Rectangle(0, 0, usableBounds.width, usableBounds.height));
		setExtendedState(Frame.MAXIMIZED_BOTH);
	}
	
	@Override
	public void setResizable(boolean resizable) {
		resizer.setEnabled(resizable);
		super.setResizable(resizable);
	}
	
	@Override
	public boolean isResizable() {
		return resizer.isEnabled();
	}
}
