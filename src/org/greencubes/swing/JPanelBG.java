package org.greencubes.swing;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * JPanel with image background
 * @author Rena
 *
 */
public class JPanelBG extends JPanel {

	private static final long serialVersionUID = -8003041052208046109L;
	
	private Image bg;
	
	public JPanelBG(String bg) {
		try {
			this.bg = ImageIO.read(JPanelBG.class.getResource(bg));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(bg, 0, 0, this);
	}
	
}
