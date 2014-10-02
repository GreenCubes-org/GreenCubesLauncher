package org.greencubes.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.greencubes.swing.JPanelBG;

public class LauncherSecond {
	
	public JFrame mainFrame;
	
	@SuppressWarnings("serial")
	public LauncherSecond(final Launcher launcher) {
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();
		
		// Main frame
		mainFrame = new JFrame("GreenCubes Launcher") {{
			setUndecorated(true);
			setAlwaysOnTop(false);
			setBackground(new Color(1.0F, 1.0F, 1.0F, 0.0F));
			setPreferredSize(new Dimension(1000, 640));
			try {
				ArrayList<BufferedImage> icons = new ArrayList<BufferedImage>(5);
				icons.add(ImageIO.read(LauncherFirst.class.getResource("/gcico32x32.png")));
				icons.add(ImageIO.read(LauncherFirst.class.getResource("/gcico48x48.png")));
				icons.add(ImageIO.read(LauncherFirst.class.getResource("/gcico64x64.png")));
				icons.add(ImageIO.read(LauncherFirst.class.getResource("/gcico128x128.png")));
				icons.add(ImageIO.read(LauncherFirst.class.getResource("/gcico256x256.png")));
				setIconImages(icons);
			} catch(IOException e1) {
				e1.printStackTrace();
			}
			
			// Background
			add(new JPanelBG("/bgbig.png") {{
					setLayout(layout);
					
				}}, BorderLayout.CENTER);
			
			// Show
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}};
		
		JPanel cross = new JPanelBG("/cross.png") {{;
			setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
			setPreferredSize(new Dimension(26, 26));
			addMouseListener(launcher.closeMouseListener);
		}};
	}
	
}
