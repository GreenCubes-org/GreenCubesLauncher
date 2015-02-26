package org.greencubes.swing;

import com.apple.eawt.Application;

import java.awt.Image;
import java.util.List;

public class MacOSX {
	
	public static void setIcons(List<? extends Image> icons) {
		Application application = Application.getApplication();
		application.setDockIconImage(icons.get(icons.size() - 1));
	}
	
}
