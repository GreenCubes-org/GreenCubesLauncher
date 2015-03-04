package org.greencubes.swing;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * JPanel with image background
 * @author Rena
 */
public class JPanelBG extends JPanel {

	private static final long serialVersionUID = -8003041052208046109L;
	
	public int paddingTop = 0;
	public int paddingLeft = 0;
	/**
	 * Resize bg image to fit in panel boundaries
	 */
	public boolean fill = false;
	public Image bg;
	public Image activeBg;
	public Image inactiveBg;
	
	public JPanelBG(String bg) {
		try {
			this.bg = ImageIO.read(JPanelBG.class.getResource(bg));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public JPanelBG(String defaultBg, String activeBg) {
		this(defaultBg);
		this.inactiveBg = this.bg;
		try {
			this.activeBg = ImageIO.read(JPanelBG.class.getResource(activeBg));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public MouseListener getActiveMouseListener() {
		return new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				bg = activeBg;
				repaint();
			}

			@Override
		    public void mouseExited(MouseEvent e) {
		    	bg = inactiveBg;
		    	repaint();
		    }
		};
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawBg(g);		
	}
	
	protected void drawBg(Graphics g) {
		if(this.fill)
			g.drawImage(this.bg, this.paddingLeft, this.paddingTop, g.getClipBounds().width, g.getClipBounds().height, this);
		else
			g.drawImage(this.bg, this.paddingLeft, this.paddingTop, this);
	}
	
}
