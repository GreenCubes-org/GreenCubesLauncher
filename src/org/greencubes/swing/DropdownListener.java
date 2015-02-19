package org.greencubes.swing;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class DropdownListener extends MouseAdapter {
	
	private final JPopupMenu popup;
	private int offsetX;
	private int offsetY;
	private boolean useCustomOffset;
	private long animation = -1;
	private int startX;
	private int startY;
	private List<ActionListener> listeners = new ArrayList<ActionListener>();
	
	public DropdownListener(JPopupMenu popupMenu) {
		this.popup = popupMenu;
	}
	
	public DropdownListener(JPopupMenu popupMenu, int offsetX, int offsetY) {
		this.popup = popupMenu;
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
	
	public void addActionListener(ActionListener listener) {
		listeners.add(listener);
	}
	
	public void show(Component component, int x, int y) {
		int targetX;
		int targetY;
		if(this.useCustomOffset) {
			targetX = this.offsetX;
			targetY = this.offsetY;
		} else {
			targetX = x;
			targetY = y;
		}
		if(this.animation > 0) {
			this.popup.show(component, this.startX, this.startY);
			final long steps = this.animation / 5;
			final float stepX = (targetX - this.startX) / (float) steps;
			final float stepY = (targetY - this.startY) / (float) steps;
			final Timer t = new Timer(5, null);
			t.addActionListener(new ActionListener() {
				int i = 0;
				float mvX;
				float mvY;
				@Override
				public void actionPerformed(ActionEvent e) {
					this.mvX += stepX;
					this.mvY += stepY;
					int mx = (int) this.mvX;
					int my = (int) this.mvY;
					this.mvX -= mx;
					this.mvY -= my;
					Point pos = DropdownListener.this.popup.getLocationOnScreen();
					DropdownListener.this.popup.setLocation(pos.x + mx, pos.y + my);
					if(++this.i == steps) {
						t.stop();
						if(listeners.size() > 0)
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									for(int i = 0; i < listeners.size(); ++i)
										listeners.get(i).actionPerformed(new ActionEvent(DropdownListener.this.popup, ActionEvent.ACTION_FIRST, "shown"));
								}
							});
					}
				}
			});
			t.start();
		} else {
			this.popup.show(component, targetX, targetY);
			for(int i = 0; i < listeners.size(); ++i)
				listeners.get(i).actionPerformed(new ActionEvent(DropdownListener.this.popup, ActionEvent.ACTION_FIRST, "shown"));
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.isConsumed())
			return;
		if(e.getButton() == MouseEvent.BUTTON1)
			show(e.getComponent(), e.getX(), e.getY());
	}
}