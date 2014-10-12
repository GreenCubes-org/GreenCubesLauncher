package org.greencubes.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.greencubes.main.Main;
import org.greencubes.swing.AbstractMouseListener;
import org.greencubes.swing.JPanelBG;
import org.greencubes.util.I18n;

public class LauncherLogin {
	
	private JFrame frame;
	
	//@formatter:off
	public LauncherLogin() {
		frame = new JFrame(I18n.get("title"));
		frame.setIconImages(LauncherOptions.getIcons());
		frame.setUndecorated(!Main.TEST);
		frame.add(new JPanelBG("/res/login.bg.png") {{
			setPreferredSize(new Dimension(560, 384));
			setLayout(new GridBagLayout());
			setBackground(new Color(0, 1, 1, 0.5f));
			// Top line
			add(new JPanel() {{
				setPreferredSize(new Dimension(128, 25));
				setBackground(new Color(0, 1, 0, 0.5f));
			}}, gbc(1, 1, 0, 0));
			add(new JPanel() {{
				setPreferredSize(new Dimension(407, 25));
				setBackground(new Color(1, 0, 0, 0.5f));
			}}, gbc(1, 1, 1, 0));
			add(new JPanel() {{
				setPreferredSize(new Dimension(25, 25));
				setBackground(new Color(0, 0, 1, 0.5f));
				add(new JPanelBG("/res/cross.png") {{
					setPreferredSize(new Dimension(14, 14));
					setBackground(new Color(0, 0, 1, 0.5f));
				}});
				addMouseListener(new AbstractMouseListener() {
					// TODO : Can add cross animation
					@Override
					public void mouseClicked(MouseEvent e) {
						Main.close();
					}
				});
			}}, gbc(1, 1, 2, 0));
			
			add(new JPanel() {{
				setPreferredSize(new Dimension(128, 359));
				setBackground(new Color(1, 1, 0, 0.5f));
			}}, gbc(1, 1, 0, 1));
		}}, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		if(LauncherOptions.autoLogin) {
			LauncherOptions.loadSession();
			if(LauncherOptions.sessionUserId > 0) {
				try {
					LauncherOptions.authSession();
					LauncherOptions.userInfo = LauncherUtil.sessionRequest("action=info");
					if(Main.TEST)
						System.out.println(LauncherOptions.userInfo);
					launcherMain();
					return;
					// Continue launcher loading
				} catch(Exception e) {
				}
			}
		}
	}
	//@formatter:on
	
	public void launcherMain() {
		
	}
	
	private static GridBagConstraints gbc(int width, int height, int x, int y) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = width;
		c.gridheight = height;
		c.gridx = x;
		c.gridy = y;
		return c;
	}
	
}
