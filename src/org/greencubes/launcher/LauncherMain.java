package org.greencubes.launcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.greencubes.main.Main;
import org.greencubes.swing.AbstractComponentListener;
import org.greencubes.swing.AbstractMouseListener;
import org.greencubes.swing.AbstractWindowListener;
import org.greencubes.swing.JPanelBG;
import org.greencubes.util.I18n;
import org.greencubes.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

public class LauncherMain {
	
	private JFrame frame;
	
	//@formatter:off
	public LauncherMain(JFrame previousFrame) {
		frame = new JFrame(I18n.get("title")) {
			@Override
            public void paint(Graphics g) {
				// Hack to make maximum size work
				// TODO : Finde better solution
                Dimension d = getSize();
                Dimension m1 = getMaximumSize();
                boolean resize = d.width > m1.width || d.height > m1.height;
                d.width = Math.min(m1.width, d.width);
                d.height = Math.min(m1.height, d.height);
                if(resize) {
                    Point p = getLocation();
                    setVisible(false);
                    setSize(d);
                    setLocation(p);
                    setVisible(true);
                }
                super.paint(g);
            }
		};
		frame.setIconImages(LauncherOptions.getIcons());
		frame.setUndecorated(!Main.TEST);
		frame.setMinimumSize(new Dimension(640, 320));
		frame.setMaximumSize(new Dimension(1440, 960));
		frame.add(new JPanelBG("/res/main.bg.png") {{
			Dimension d = new Dimension(Main.getConfig().optInt("width", 900), Main.getConfig().optInt("height", 640));
			setPreferredSize(d);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBackground(new Color(0, 1, 1, 0));
			setMaximumSize(new Dimension(1440, 960));
			
			// Top line
			add(new JPanel() {{
				setBackground(Util.debugColor());
				setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
				add(Box.createHorizontalGlue());
				add(new JPanel() {{
					s(this, 25, 25);
					setBackground(new Color(0, 0, 0, 0));
					add(new JPanelBG("/res/cross.png") {{ // TODO : Minimize button
						s(this, 14, 14);
						setBackground(new Color(0, 0, 0, 0));
					}});
					addMouseListener(new AbstractMouseListener() {
						// TODO : Can add cross animation
						@Override
						public void mouseClicked(MouseEvent e) {
							frame.setState(JFrame.ICONIFIED);
						}
					});
				}});
				add(new JPanel() {{
					s(this, 25, 25);
					setBackground(new Color(0, 0, 0, 0));
					add(new JPanelBG("/res/cross.png") {{
						s(this, 14, 14);
						setBackground(new Color(0, 0, 0, 0));
					}});
					addMouseListener(new AbstractMouseListener() {
						// TODO : Can add cross animation
						@Override
						public void mouseClicked(MouseEvent e) {
							Main.close();
						}
					});
				}});
			}});
			
			add(new JPanel() {{
				setBackground(Util.debugColor());
				setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
				add(new JPanelBG("/res/main.logo.png") {{ // GreenCubes logo
					setBackground(new Color(0, 0, 0, 0));
					s(this, 100, 50);
					paddingTop = 1;
					// TODO : Add popout panel
				}});
				
				add(new JPanel() {{
					s(this, 120, 50);
					setBackground(Util.debugColor());
					add(new JTextPane() {{
						setOpaque(false);
						StyledDocument doc = getStyledDocument();
						SimpleAttributeSet center = new SimpleAttributeSet();
						StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
						doc.setParagraphAttributes(0, doc.getLength(), center, false);
						setBackground(new Color(0, 0, 0, 0));
						setForeground(new Color(25, 97, 14, 255));
						setEditable(false);
						setText("ИГРАТЬ");
						setFont(new Font("ClearSans", Font.BOLD, 30));
					}});
				}});
				
				add(Box.createHorizontalGlue());
			}});

			add(Box.createVerticalGlue());
		}});
		
		frame.pack();
		// Load position from config
		if(Main.getConfig().has("posx") && Main.getConfig().has("posy")) {
			frame.setLocation(Main.getConfig().optInt("posx", 0), Main.getConfig().optInt("posy", 0));
		} else
			frame.setLocationRelativeTo(null);
		// Resize and close listeners to save position and size betwen launcher starts
		frame.addComponentListener(new AbstractComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				save();
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				save();
			}
			
			protected void save() {
				JSONObject config = Main.getConfig();
				Rectangle b = frame.getBounds();
				try {
					config.put("posx", b.x);
					config.put("posy", b.y);
					config.put("width", b.width);
					config.put("height", b.height);
				} catch(JSONException e) {}
			}
		});
		frame.addWindowListener(new AbstractWindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				Main.close();
			}
		});
		
		if(previousFrame != null)
			previousFrame.dispose();
		frame.setVisible(true);
	}
	//@formatter:on

	private static void s(Component c, int width, int height) {
		c.setSize(width, height);
		Dimension d = new Dimension(width, height);
		c.setPreferredSize(d);
		c.setMaximumSize(d);
		c.setMinimumSize(d);
	}
	
	
}
