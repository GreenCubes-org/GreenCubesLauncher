package org.greencubes.swing;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class GJBoxPanel extends JPanel {
	
	public GJBoxPanel(int boxLayoutAxis, Color background) {
		if(background == null) {
			setOpaque(false);
		} else {
			setBackground(background);
		}
		setLayout(new BoxLayout(this, boxLayoutAxis));
	}
}
