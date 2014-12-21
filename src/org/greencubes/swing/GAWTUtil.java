package org.greencubes.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EventListener;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class GAWTUtil {
	
	public static void removeMouseListeners(JComponent c) {
		EventListener handlers[] = c.getListeners(MouseListener.class);
		for(int i = 0; i < handlers.length; i++)
			c.removeMouseListener((MouseListener) handlers[i]);
		handlers = c.getListeners(MouseMotionListener.class);
		for(int i = 0; i < handlers.length; i++)
			c.removeMouseMotionListener((MouseMotionListener) handlers[i]);
	}
	
	public static String toURL(String str) {
		try {
			return new URL(str).toExternalForm();
		} catch(MalformedURLException exception) {
			return null;
		}
	}
	
	public static Border popupBorder() {
		return new EmptyBorder(new Insets(16, 11, 11, 11)) {
			Image icon;
			{
				try {
					icon = ImageIO.read(GAWTUtil.class.getResource("/res/border.popup.png"));
				} catch(IOException e) {
					throw new AssertionError(e);
				}
			}
			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				g.translate(x, y);
				
				// Top-left
				g.drawImage(icon, 0, 0, 11, 16, 0, 0, 11, 16, c);
				// Top-left padding
				g.drawImage(icon, 11, 0, 11 + 16, 16, 37, 16, 38, 27, c);
				
				g.drawImage(icon, 11 + 16, 0, 11 + 16 + 39, 16, 11, 0, 50, 16, c);
				
				g.drawImage(icon, 11 + 16 + 39, 0, width - 11, 16, 37, 16, 38, 27, c);
				
				// Top-right
				g.drawImage(icon, width - 11, 0, width, 16, 0, 33, 11, 44, c);
				// Right
				g.drawImage(icon, width - 11, 16, width, height - 16 - 11, 39, 17, 50, 18, c);
			}

			@Override
			public boolean isBorderOpaque() {
				return false;
			}
		};
	}
}
