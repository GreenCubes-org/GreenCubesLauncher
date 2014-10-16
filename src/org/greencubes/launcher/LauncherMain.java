package org.greencubes.launcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.greencubes.main.Main;
import org.greencubes.swing.AbstractComponentListener;
import org.greencubes.swing.AbstractMouseListener;
import org.greencubes.swing.AbstractWindowListener;
import org.greencubes.swing.JPanelBG;
import org.greencubes.util.I18n;
import org.json.JSONException;
import org.json.JSONObject;

public class LauncherMain {
	
	private CefClient cefClient;
	
	private Frame frame;
	private JPanel mainPanel;
	
	//@formatter:on
	public LauncherMain(Window previousFrame) {
		frame = new Frame(I18n.get("title")) { // We use not jframe as we need to render canvas
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
		JPanel innerPanel;
		frame.add(innerPanel = new JPanelBG("/res/main.bg.png") {{
			Dimension d = new Dimension(Main.getConfig().optInt("width", 900), Main.getConfig().optInt("height", 640));
			setPreferredSize(d);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBackground(new Color(0, 0, 0, 0));
			
			// Top line
			add(new JPanel() {{
				//setOpaque(false);
				setBackground(new Color(0, 0, 0, 0));
				setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
				add(new JPanel() {{
					setBackground(new Color(0, 0, 0, 0));
					setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
					add(Box.createHorizontalGlue());
					add(new JPanel() {{
						s(this, 25, 25);
						setBackground(new Color(0, 0, 0, 0));
						add(new JPanel() {{ // TODO : Minimize button
							s(this, 14, 14);
							setBackground(new Color(0, 0, 0, 0));
						}});
						addMouseListener(new AbstractMouseListener() {
							// TODO : Can add cross animation
							@Override
							public void mouseClicked(MouseEvent e) {
								frame.setState(Frame.ICONIFIED);
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
								frame.dispose();
								Main.close();
							}
						});
					}});
				}});
				add(new JPanel() {{
					setBackground(new Color(0, 0, 0, 0));
					setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
					add(new JPanelBG("/res/main.logo.png") {{ // GreenCubes logo
						setBackground(new Color(0, 0, 0, 0));
						s(this, 100, 50);
						paddingTop = 1;
						// TODO : Add popout panel
					}});
					
					add(new JPanelBG("/res/main.play.ruRU.png") {{
						s(this, 110, 50);
						setBackground(new Color(0, 0, 0, 0));
						paddingLeft = 15;
						paddingTop = 15;
					}});
					add(new JPanelBG("/res/main.shop.ruRU.png") {{
						s(this, 120, 50);
						setBackground(new Color(0, 0, 0, 0));
						paddingLeft = 10;
						paddingTop = 15;
					}});
					add(new JPanelBG("/res/main.news.ruRU.png") {{
						s(this, 130, 50);
						setBackground(new Color(0, 0, 0, 0));
						paddingLeft = 15;
						paddingTop = 15;
					}});
					
					add(Box.createHorizontalGlue());
				}});
				add(new JPanel() {{
					s(this, 5, 5);
					setBackground(new Color(0, 0, 0, 0));
				}});
			}});
			add(mainPanel = new JPanel() {{
				setBackground(new Color(0, 0, 0, 0));
			}});
		}});
		
		frame.pack();
		// Load position from config
		if(Main.getConfig().has("posx") && Main.getConfig().has("posy")) {
			frame.setLocation(Main.getConfig().optInt("posx", 0), Main.getConfig().optInt("posy", 0));
		} else
			frame.setLocationRelativeTo(null);
		// Resize and close listeners to save position and size betwen launcher starts
		innerPanel.addComponentListener(new AbstractComponentListener() {
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
				frame.dispose();
				Main.close();
			}
		});
		
		if(previousFrame != null)
			previousFrame.dispose();
		frame.setVisible(true);
		displayPlayPanel();
	}
	//@formatter:on

	//@formatter:off
	private void displayPlayPanel() {
		CefClient cefClient = getCefClient();
		CefBrowser browser = cefClient.createBrowser("https://greencubes.org/", false, true);
		Component c = browser.getUIComponent();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.add(c, new GridBagConstraints() {{
			gridx = 1;
			gridy = 1;
			weightx = 1;
			weighty = 1;
			fill = GridBagConstraints.BOTH;
		}});
		frame.revalidate();
	}
	
	private CefClient getCefClient() {
		synchronized(this) {
			if(cefClient == null) {
				CefApp cefApp = CefApp.getInstance();
				cefClient = cefApp.createClient();
			}
			return cefClient;
		}
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
