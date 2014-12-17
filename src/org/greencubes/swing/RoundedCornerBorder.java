package org.greencubes.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.AbstractBorder;

public class RoundedCornerBorder extends AbstractBorder {
	
	public Color bgColor;
	public Color borderColor;
	public int inset;
	
	public RoundedCornerBorder(Color bgColor, Color borderColor, int inset) {
		this.bgColor = bgColor;
		this.borderColor = borderColor;
		this.inset = inset * 2;
	}
	
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		RoundRectangle2D round = new RoundRectangle2D.Float(x, y, width, height, inset, inset);
		Container parent = c.getParent();
		if(parent != null) {
			g2.setColor(bgColor);
			Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
			corner.subtract(new Area(round));
			g2.fill(corner);
		}
		if(borderColor != null) {
			RoundRectangle2D round2 = new RoundRectangle2D.Float(x, y, width - 1, height - 1, inset, inset);
			g2.setColor(borderColor);
			g2.draw(round2);
		}
		g2.dispose();
	}
	
	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(1, 1, 1, 1);
	}
	
	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		insets.left = insets.right = insets.top = insets.bottom = 1;
		return insets;
	}
}