package org.greencubes.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

public class DropdownListener extends MouseAdapter {
	
	private final JPopupMenu popup;
	private int offsetX;
	private int offsetY;
	private boolean useCustomOffset;
	
	public DropdownListener(JPopupMenu popupMenu) {
		popup = popupMenu;
	}
	
	public DropdownListener(JPopupMenu popupMenu, int offsetX, int offsetY) {
		popup = popupMenu;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.useCustomOffset = true;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.isConsumed())
			return;
		if(e.getButton() == MouseEvent.BUTTON1) {
			if(useCustomOffset)
				popup.show(e.getComponent(), offsetX, offsetY);
			else
				popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}