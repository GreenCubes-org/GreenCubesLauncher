package org.greencubes.swing;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EventListener;

import javax.swing.JComponent;

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
}