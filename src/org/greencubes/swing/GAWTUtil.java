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
		return new EmptyBorder(new Insets(23, 13, 13, 13)) {
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
				g.drawImage(icon, 0, 10, 13, 10 + 13, 1, 11, 14, 24, c);
				// Top-left padding
				g.drawImage(icon, 13, 10, 13 + 7, 10 + 13, 15, 11, 15 + 7, 11 + 13, c);
				g.drawImage(icon, 13 + 7, 10, 13 + 7 + 7, 10 + 13, 23, 11, 23 + 1, 11 + 13, c);
				// Pimpochka
				g.drawImage(icon, 13 + 7 + 7, 0, 13 + 7 + 7 + 38, 23, 25, 1, 25 + 38, 1 + 23, c);
				
				// Top-right
				g.drawImage(icon, width - 13, 10, width, 10 + 13, 74, 11, 74 + 13, 11 + 13, c);
				// Top-right padding
				g.drawImage(icon, width - 13 - 7, 10, width - 13, 10 + 13, 66, 11, 66 + 7, 11 + 13, c);
				// Top
				g.drawImage(icon, 13 + 7 + 7 + 38, 10, width - 13 - 7, 10 + 13, 64, 11, 64 + 1, 11 + 13, c);
				// Right-top padding
				g.drawImage(icon, width - 13, 23, width, 23 + 7, 74, 25, 74 + 13, 25 + 7, c);
				// Right
				g.drawImage(icon, width - 13, 23 + 7, width, height - 13 - 7, 74, 33, 74 + 13, 33 + 1, c);
				// Right-bottom padding
				g.drawImage(icon, width - 13, height - 13 - 7, width, height - 13, 74, 35, 74 + 13, 35 + 7, c);
				// Right-bottom
				g.drawImage(icon, width - 13, height - 13, width, height, 74, 43, 74 + 13, 43 + 13, c);
				// Bottom-right padding
				g.drawImage(icon, width - 13 - 7, height - 13, width - 13, height, 66, 43, 66 + 7, 43 + 13, c);
				// Bottom
				g.drawImage(icon, 13 + 7, height - 13, width - 13 - 7, height, 23, 43, 23 + 1, 43 + 13, c);
				// Bottom-left padding
				g.drawImage(icon, 13, height - 13, 13 + 7, height, 15, 43, 15 + 7, 43 + 13, c);
				// Bottom-left
				g.drawImage(icon, 0, height - 13, 13, height, 1, 43, 1 + 13, 43 + 13, c);
				// Left-bottom padding
				g.drawImage(icon, 0, height - 13 - 7, 13, height - 13, 1, 35, 1 + 13, 35 + 7, c);
				// Left
				g.drawImage(icon, 0, 10 + 13 + 7, 13, height - 13 - 7, 1, 33, 1 + 13, 33 + 1, c);
				// Left-top padding
				g.drawImage(icon, 0, 10 + 13, 13, 10 + 13 + 7, 1, 25, 1 + 13, 25 + 7, c);
			}

			@Override
			public boolean isBorderOpaque() {
				return false;
			}
		};
	}
}
