package org.greencubes.launcher;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.greencubes.swing.GJBoxPanel;
import org.greencubes.swing.UIScheme;

public class LauncherConfig {
	
	public LauncherConfig(final JFrame frame) {
		JDialog dialog = new JDialog(frame, Dialog.ModalityType.APPLICATION_MODAL);
		s(dialog, 200, 500);
		dialog.setLocationRelativeTo(frame);
		//dialog.setUndecorated(true);
		dialog.add(new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.BACKGROUND) {{
			s(this, 200, 500);
		}});
		
		dialog.setVisible(true);
	}
	
	private static void s(Component c, int width, int height) {
		c.setSize(width, height);
		Dimension d = new Dimension(width, height);
		c.setPreferredSize(d);
		c.setMaximumSize(d);
		c.setMinimumSize(d);
	}
	
}
