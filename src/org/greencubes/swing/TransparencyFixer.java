package org.greencubes.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class TransparencyFixer implements PropertyChangeListener {
	
	private final Component component;
	
	public TransparencyFixer(Component component) {
		this.component = component;
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if(evt.getNewValue() == Boolean.TRUE) {
			// Retrieving popup menu window (we won't find it if it is inside of parent frame)
			final Window ancestor = getWindowAncestor(component);
			if(ancestor != null && ancestor.getClass().getCanonicalName().endsWith("HeavyWeightWindow")) {
				// Making popup menu window non-opaque
				GAWTUtil.safeTransparentBackground(ancestor, new Color(0, 0, 0, 0));//ancestor.setBackground(new Color(0, 0, 0, 0));
			}
		}
	}
	
	private Window getWindowAncestor(Component component) {
		if(component == null)
			return null;
		if(component instanceof Window)
			return (Window) component;
		for(Container p = component.getParent(); p != null; p = p.getParent())
			if(p instanceof Window)
				return (Window) p;
		return null;
	}
	
	public static void add(Component component) {
		TransparencyFixer fixer = new TransparencyFixer(component);
		component.addPropertyChangeListener("visible", fixer);
	}
	
}
