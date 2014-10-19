package org.greencubes.launcher;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.greencubes.client.Client;
import org.greencubes.main.Main;
import org.greencubes.swing.AbstractComponentListener;
import org.greencubes.swing.AbstractMouseListener;
import org.greencubes.swing.AbstractWindowListener;
import org.greencubes.swing.GAWTUtil;
import org.greencubes.swing.JPanelBG;
import org.greencubes.util.I18n;
import org.greencubes.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

public class LauncherMain {
	
	private CefClient cefClient;
	
	private JFrame frame;
	private JPanel mainPanel;
	private JPanel innerPanel;
	private JPanel clientPanel;
	private Client currentClient;
	
	//@formatter:off
	public LauncherMain(Window previousFrame) {
		frame = new JFrame(I18n.get("title")) { // We use not jframe as we need to render canvas
			@Override
			public void paint(Graphics g) {
				// Hack to make maximum size work
				// TODO : Find better solution
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
		frame.add(innerPanel = new JPanelBG("/res/main.bg.png") {{
			Dimension d = new Dimension(Main.getConfig().optInt("width", 900), Main.getConfig().optInt("height", 640));
			setPreferredSize(d);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBackground(new Color(0, 0, 0, 0));
			// Top line
			add(new JPanel() {{
				setOpaque(false);
				setBackground(new Color(0, 0, 0, 0));
				setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
				add(new JPanel() {{
					setBackground(new Color(0, 0, 0, 0));
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
							addMouseListener(new AbstractMouseListener() {
								// TODO : Can add cross animation
								@Override
								public void mouseClicked(MouseEvent e) {
									frame.dispose();
									Main.close();
								}
							});
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
					setOpaque(false);
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
					
					add(new JPanel() {{
						setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
						setOpaque(false);
						setBackground(Util.debugColor());
						add(new JPanel() {{
							setOpaque(false);
							add(new JTextPane() {{
								setOpaque(false);
								StyledDocument doc = getStyledDocument();
								SimpleAttributeSet center = new SimpleAttributeSet();
								StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
								doc.setParagraphAttributes(0, doc.getLength(), center, false);
								setBackground(Util.debugColor());
								setForeground(new Color(0, 0, 0, 255));
								setEditable(false);
								setText(LauncherOptions.userInfo != null ? LauncherOptions.userInfo.optString("username") : LauncherOptions.sessionUser);
								setFont(new Font("ClearSans", Font.PLAIN, 14));
								disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
							}});
						}});
						
						add(new JMenuBar() {{
							add(new JMenu("Online") {{
								add(new JMenuItem("Cats"));
								add(new JMenuItem("Cats 2"));
								add(new JMenuItem("Cats 3"));
							}});
						}});
							
					}});
					add(new JPanel() {{
						s(this, 20, 0);
					}});
				}});
				add(new JPanel() {{
					s(this, 5, 8);
					setBackground(new Color(0, 0, 0, 0));
				}});
			}});
			add(mainPanel = new JPanel() {{
				setOpaque(false);
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
				saveFrameInfo();
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				saveFrameInfo();
			}
		});
		frame.addComponentListener(new AbstractComponentListener() {
			@Override
			public void componentMoved(ComponentEvent e) {
				saveFrameInfo();
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
	
	private void saveFrameInfo() {
		JSONObject config = Main.getConfig();
		Rectangle b = frame.getBounds();
		Rectangle b2 = innerPanel.getBounds();
		try {
			config.put("posx", b.x);
			config.put("posy", b.y);
			config.put("width", b2.width);
			config.put("height", b2.height);
		} catch(JSONException e) {
		}
	}
	
	//@formatter:off
	private void displayPlayPanel() {
		CefClient cefClient = getCefClient();
		final CefBrowser browser = cefClient.createBrowser("https://greencubes.org/", false, false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));
		mainPanel.add(new JPanel() {{
			setOpaque(false);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBackground(new Color(0, 0, 0, 0));
			add(new JPanel() {{
				s(this, 1, 1);
				setBackground(new Color(0, 0, 0, 0));
			}});
			add(new JPanel() {{ // New client button
				setOpaque(false);
				s(this, 101, 101);
				setBackground(new Color(0, 0, 0, 0));
				final JPanel inner;
				add(inner = new JPanel() {{ // Inner
					s(this, 95, 95);
					setOpaque(false);
					setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
					setBackground(new Color(0, 0, 0, 0));
					add(new JPanelBG("/res/main.newclient.logo.png") {{
						s(this, 48, 48);
					}});
					JTextPane pane;
					add(pane = new JTextPane() {{
						setOpaque(false);
						StyledDocument doc = getStyledDocument();
						SimpleAttributeSet center = new SimpleAttributeSet();
						StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
						doc.setParagraphAttributes(0, doc.getLength(), center, false);
						setBackground(new Color(0, 0, 0, 100));
						setForeground(new Color(93, 43, 94, 255));
						setEditable(false);
						setText(I18n.get("client.new.name"));
						setFont(new Font("ClearSans", Font.PLAIN, 14));
						disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
					}});
					GAWTUtil.removeMouseListeners(pane);
				}});
				addMouseListener(new AbstractMouseListener() {
					@Override
					public void mouseEntered(MouseEvent e) {
						inner.setBorder(BorderFactory.createLineBorder(new Color(21, 54, 20, 255), 1));
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						inner.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
					}
				});
			}});
			
			add(new JPanel() {{ // Old client button
				setOpaque(false);
				s(this, 101, 101);
				setBackground(new Color(0, 0, 0, 0));
				final JPanel inner;
				add(inner = new JPanel() {{ // Inner
					s(this, 95, 95);
					setOpaque(false);
					setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
					setBackground(new Color(0, 0, 0, 0));
					add(new JPanelBG("/res/main.oldclient.logo.png") {{
						s(this, 48, 48);
					}});
					JTextPane pane;
					add(pane = new JTextPane() {{
						setOpaque(false);
						StyledDocument doc = getStyledDocument();
						SimpleAttributeSet center = new SimpleAttributeSet();
						StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
						doc.setParagraphAttributes(0, doc.getLength(), center, false);
						setBackground(new Color(0, 0, 0, 100));
						setForeground(new Color(63, 76, 63, 255));
						setEditable(false);
						setText("Старый\nклиент");
						setFont(new Font("ClearSans", Font.PLAIN, 14));
						disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
					}});
					GAWTUtil.removeMouseListeners(pane);
				}});
				addMouseListener(new AbstractMouseListener() {
					@Override
					public void mouseEntered(MouseEvent e) {
						inner.setBorder(BorderFactory.createLineBorder(new Color(21, 54, 20, 255), 1));
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						inner.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
					}
				});
			}});
			
			add(new JPanel() {{ // Test client button
				setOpaque(false);
				s(this, 101, 101);
				setBackground(new Color(0, 0, 0, 0));
				final JPanel inner;
				add(inner = new JPanel() {{ // Inner
					s(this, 95, 95);
					setOpaque(false);
					setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
					setBackground(new Color(0, 0, 0, 0));
					add(new JPanelBG("/res/main.testclient.logo.png") {{
						s(this, 48, 48);
					}});
					JTextPane pane;
					add(pane = new JTextPane() {{
						setOpaque(false);
						StyledDocument doc = getStyledDocument();
						SimpleAttributeSet center = new SimpleAttributeSet();
						StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
						doc.setParagraphAttributes(0, doc.getLength(), center, false);
						setBackground(new Color(0, 0, 0, 100));
						setForeground(new Color(139, 83, 0, 255));
						setEditable(false);
						setText("Тестовый\nклиент");
						setFont(new Font("ClearSans", Font.PLAIN, 14));
						disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
					}});
					GAWTUtil.removeMouseListeners(pane);
				}});
				addMouseListener(new AbstractMouseListener() {
					@Override
					public void mouseEntered(MouseEvent e) {
						inner.setBorder(BorderFactory.createLineBorder(new Color(21, 54, 20, 255), 1));
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						inner.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
					}
				});
			}});
			
			add(Box.createVerticalGlue());
		}});
		mainPanel.add(clientPanel = new JPanel() {{
			setOpaque(false);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBackground(new Color(0, 0, 0, 0));
			add(new JPanel() {{
				setOpaque(false);
				setLayout(new GridBagLayout());
				Component c = browser.getUIComponent();
				add(c, new GridBagConstraints() {{
					weightx = 1;
					weighty = 1;
					fill = GridBagConstraints.BOTH;
				}});
				s(c, 100, 100);
			}});
			add(new JPanel() {{
				setOpaque(false);
				setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
				s(this, 10, 150);
				setBackground(new Color(0, 0, 0, 0));
			}});
		}});
		frame.revalidate();
		currentClient = null;
		// TODO : Display client
	}
	//@formatter:on
	
	private CefClient getCefClient() {
		synchronized(this) {
			if(cefClient == null) {
				CefApp cefApp = CefApp.getInstance();
				cefClient = cefApp.createClient();
			}
			return cefClient;
		}
	}
	
	private static void s(Component c, int width, int height) {
		c.setSize(width, height);
		Dimension d = new Dimension(width, height);
		c.setPreferredSize(d);
		c.setMaximumSize(d);
		c.setMinimumSize(d);
	}
}
