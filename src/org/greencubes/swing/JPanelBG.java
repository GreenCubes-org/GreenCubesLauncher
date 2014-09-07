package org.greencubes.swing;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class JPanelBG extends JPanel {

	private static final long serialVersionUID = -8003041052208046109L;
	
	private Image bg;
	
	public JPanelBG(Image bg) {
		this.bg = bg;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(bg, 0, 0, this);
	}
	
}
