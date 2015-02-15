package org.greencubes.swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPasswordField;
import javax.swing.text.Document;

public class PlaceholderPasswordField extends JPasswordField {
	
	private String placeholder;
	
	public PlaceholderPasswordField() {
	}
	
	public PlaceholderPasswordField(final Document pDoc, final String pText, final int pColumns) {
		super(pDoc, pText, pColumns);
	}
	
	public PlaceholderPasswordField(final int pColumns) {
		super(pColumns);
	}
	
	public PlaceholderPasswordField(final String pText) {
		super(pText);
	}
	
	public PlaceholderPasswordField(final String pText, final int pColumns) {
		super(pText, pColumns);
	}
	
	public String getPlaceholder() {
		return placeholder;
	}
	
	@Override
	protected void paintComponent(final Graphics pG) {
		super.paintComponent(pG);
		
		if(placeholder.length() == 0 || getPassword().length > 0) {
			return;
		}
		
		final Graphics2D g = (Graphics2D) pG;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(getDisabledTextColor());
		g.drawString(placeholder, getInsets().left, pG.getFontMetrics().getMaxAscent() + getInsets().top);
	}
	
	public void setPlaceholder(final String s) {
		placeholder = s;
	}
	
}