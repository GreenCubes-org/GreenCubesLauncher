package org.greencubes.swing;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.Timer;

public class DropdownListener extends MouseAdapter {
	
	private final JPopupMenu popup;
	private int offsetX;
	private int offsetY;
	private boolean useCustomOffset;
	private long animation = -1;
	private int startX;
	private int startY;
	
	public DropdownListener(JPopupMenu popupMenu) {
		popup = popupMenu;
	}
	
	public DropdownListener(JPopupMenu popupMenu, int offsetX, int offsetY) {
		popup = popupMenu;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.useCustomOffset = true;
	}
	
	public DropdownListener(JPopupMenu popupMenu, int offsetX, int offsetY, long animation, int startX, int startY) {
		this(popupMenu, offsetX, offsetY);
		this.animation = animation;
		this.startX = startX;
		this.startY = startY;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.isConsumed())
			return;
		if(e.getButton() == MouseEvent.BUTTON1) {
			int targetX;
			int targetY;
			if(useCustomOffset) {
				targetX = offsetX;
				targetY = offsetY;
			} else {
				targetX = e.getX();
				targetY = e.getY();
			}
			if(animation > 0) {
				popup.show(e.getComponent(), startX, startY);
				final long steps = animation / 5;
				final float stepX = (targetX - startX) / (float) steps;
				final float stepY = (targetY - startY) / (float) steps;
				final Timer t = new Timer(5, null);
				t.addActionListener(new ActionListener() {
					
					int i = 0;
					float mvX;
					float mvY;
	
					@Override
					public void actionPerformed(ActionEvent e) {
						mvX += stepX;
						mvY += stepY;
						int mx = (int) mvX;
						int my = (int) mvY;
						mvX -= mx;
						mvY -= my;
						Point pos = popup.getLocationOnScreen();
						popup.setLocation(pos.x + mx, pos.y + my);
						if(++i == steps)
							t.stop();
					}
				});
				t.start();
			} else {
				popup.show(e.getComponent(), targetX, targetY);
			}
		}
	}
}