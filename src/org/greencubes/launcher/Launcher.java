package org.greencubes.launcher;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Launcher {
	
	public Downloader downloader = new Downloader("https://auth.greencubes.org/mc/");
	public MouseListener closeMouseListener;
	
	public Launcher() {
		downloader.addServer("https://auth1.greencubes.org/mc/");
		closeMouseListener = new MouseListener() {
			@Override
            public void mouseClicked(MouseEvent e) {
				Launcher.this.close();
            }
			@Override
            public void mousePressed(MouseEvent e) {
            }

			@Override
            public void mouseReleased(MouseEvent e) {
            }

			@Override
            public void mouseEntered(MouseEvent e) {
            }

			@Override
            public void mouseExited(MouseEvent e) {
            }
		};
		new LauncherSecond(this);
	}
	
	protected void close() {
		System.exit(0);
	}
}
