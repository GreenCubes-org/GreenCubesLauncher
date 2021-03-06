package org.greencubes.launcher;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javafx.application.Platform;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.greencubes.client.Client;
import org.greencubes.launcher.LauncherOptions.OnStartAction;
import org.greencubes.main.Main;
import org.greencubes.swing.AbstractComponentListener;
import org.greencubes.swing.AbstractWindowListener;
import org.greencubes.swing.GAWTUtil;
import org.greencubes.swing.GJBoxPanel;
import org.greencubes.swing.GPopupMenu;
import org.greencubes.swing.JPanelBG;
import org.greencubes.swing.DropdownListener;
import org.greencubes.swing.TransparencyFixer;
import org.greencubes.swing.UIScheme;
import org.greencubes.swing.UndecoratedJFrame;
import org.greencubes.util.I18n;
import org.greencubes.util.MacOSX;
import org.greencubes.util.OperatingSystem;
import org.json.JSONException;
import org.json.JSONObject;

import sun.java2d.SunGraphicsEnvironment;

public class LauncherMain {
	
	final LauncherMain$Config config = new LauncherMain$Config(this);
	final LauncherMain$Play play = new LauncherMain$Play(this);
	
	private JPanelBG logoPanel;
	private JPanel innerPanel;
	
	UndecoratedJFrame frame;
	JPanel clientSelectPanel;
	JPanel mainPanel;
	JPanel clientPanel;
	JPanel configPanel;
	JLabel topGame;
	JLabel configLabel;
	
	//@formatter:off
	/**
	 * Should not be invoked in AWT thread
	 */
	public LauncherMain(Window previousFrame) {
		Platform.setImplicitExit(false);
		frame = new UndecoratedJFrame(I18n.get("title"));
		if(OperatingSystem.getCurrentPlatform() == OperatingSystem.OSX)
			MacOSX.setTitle(I18n.get("title"));
		Main.currentFrame = frame;
		if(LauncherOptions.keepTrayIcon)
			LauncherOptions.systemTrayInit();
		frame.setIconImages(LauncherOptions.getIcons());
		frame.setMinimumSize(new Dimension(640, 320));
		frame.setMaximumSize(new Dimension(1440, 960));
		final Rectangle usableBounds = SunGraphicsEnvironment.getUsableBounds(frame.getGraphicsConfiguration().getDevice());
		frame.add(innerPanel = new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.BACKGROUND) {{
			setPreferredSize(new Dimension(Math.min(Main.getConfig().optInt("width", 900), usableBounds.width), Math.min(Main.getConfig().optInt("height", 640), usableBounds.height)));
			// Top line
			add(new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.TOP_PANEL_BG_LOGO) {{
				final JPanel topPanel = this;
				add(logoPanel = new JPanelBG("/res/main.logo.png") {
					// GreenCubes logo
					Image highlightBg;
					Image activeBg;
					Image highlighedActiveBg;
					Image defaultBg = bg;
					boolean mouseActive = false;
					{ 
						try {
							this.highlightBg = ImageIO.read(JPanelBG.class.getResource("/res/main.logo.highlighted.png"));
							this.activeBg = ImageIO.read(JPanelBG.class.getResource("/res/main.logo.active.png"));
							this.highlighedActiveBg = ImageIO.read(JPanelBG.class.getResource("/res/main.logo.active.highlighted.png"));
						} catch(IOException e) {
							throw new RuntimeException(e);
						}
						setBackground(UIScheme.TOP_PANEL_BG_LOGO);
						s(this, 96, 96);
						final GPopupMenu mainPopup = new GPopupMenu(false);
						DropdownListener ddl = new DropdownListener(mainPopup, 0, 89, 200L, 0, 0);
						addMouseListener(new MouseAdapter() {
							@Override
							public void mousePressed(MouseEvent e) {
								if(e.isConsumed())
									return;
								mainPopup.show(e.getComponent(), false);
							}
						});
						mainPopup.setMenuFont(new Font(UIScheme.TITLE_FONT, Font.PLAIN, 18));
						mainPopup.setMenuColors(UIScheme.MAIN_MENU_BG, UIScheme.TITLE_COLOR, UIScheme.MAIN_MENU_BG_SEL, UIScheme.TITLE_COLOR_SEL);
						mainPopup.setMenuBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
						
						JMenuItem item = mainPopup.addItem(I18n.get("menu.settings"), "/res/menu.settings.png");
						item.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								config.displayConfig();
							}
						});
						//item = mainPopup.addItem(I18n.get("menu.help"), "/res/menu.help.png");
						item = mainPopup.addItem(I18n.get("menu.support"), "/res/menu.support.png");
						item.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								LauncherUtil.onenURLInBrowser(Main.SUPPORT_SYSTEM_URL);
							}
						});
						item = mainPopup.addItem(I18n.get("menu.relogin"), "/res/menu.relogin.png");
						item.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								LauncherOptions.logOff();
								frame.dispose();
								if(Main.trayIcon != null) {
									SystemTray.getSystemTray().remove(Main.trayIcon);
									Main.trayIcon = null;
								}
								new Thread() {
									@Override
									public void run() {
										new LauncherLogin(null);
									}
								}.start();
							}
						});
						//item = mainPopup.addItem(I18n.get("menu.offline"), "/res/menu.offline.png");
						item = mainPopup.addItem(I18n.get("menu.exit"), "/res/menu.exit.png");
						item.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Main.performWindowAction(OnStartAction.CLOSE);
							}
						});
						mainPopup.setOpaque(false);
						mainPopup.setBorder(GAWTUtil.safePopupBorder());
						mainPopup.validate();
						mainPopup.addPopupMenuListener(new PopupMenuListener() {
							@Override
							public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
								bg = mouseActive ? highlighedActiveBg : activeBg;
								topPanel.repaint();
							}
							@Override
							public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
								bg = mouseActive ? highlightBg : defaultBg;
								topPanel.repaint();
							}
							@Override
							public void popupMenuCanceled(PopupMenuEvent e) {
							}
						});
						TransparencyFixer.add(mainPopup);
						ddl.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								bg = getMousePosition() != null ? highlighedActiveBg : activeBg;
								topPanel.repaint();
							}
						});
						addMouseListener(new MouseAdapter() {
							@Override
							public void mouseEntered(MouseEvent e) {
								mouseActive = true;
								bg = mainPopup.isVisible() ? highlighedActiveBg : highlightBg;
								topPanel.repaint();
							}
							@Override
						    public void mouseExited(MouseEvent e) {
								mouseActive = false;
						    	bg = mainPopup.isVisible() ? activeBg : defaultBg;
						    	topPanel.repaint();
						    }
						});
				}});
				
				add(new JPanelBG("/res/main.top.png") { // Everything else on top
					@Override
					protected void drawBg(Graphics g) {
						g.drawImage(this.bg, g.getClipBounds().width - this.bg.getWidth(this), 0, this);
					}
					{
						setMaximumSize(new Dimension(9999, 96));
						setBackground(UIScheme.TOP_PANEL_BG_LOGO);
						setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
						add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{ // Window buttons
							add(Box.createHorizontalGlue());
							add(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{ // Minimize button
								s(this, 30, 30);
								setAlignmentX(JComponent.CENTER_ALIGNMENT);
								add(Box.createVerticalGlue());
								JPanelBG panel;
								add(panel = new JPanelBG("/res/minimize.png", "/res/minimize.active.png") {{
									s(this, 14, 14);
									setOpaque(false);
								}});
								add(Box.createVerticalGlue());
								addMouseListener(new MouseAdapter() {
									@Override
									public void mousePressed(MouseEvent e) {
										Main.performWindowAction(LauncherOptions.onLauncherMinimize);
									}
								});
								addMouseListener(panel.getActiveMouseListener());
							}});
							if(OperatingSystem.getCurrentPlatform() != OperatingSystem.OSX) // OS X shittly supports window expanding
								add(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{ // Maximize button
									s(this, 30, 30);
									add(Box.createVerticalGlue());
									JPanelBG panel;
									add(panel = new JPanelBG("/res/expand.png", "/res/expand.active.png") {{
										s(this, 14, 14);
										setOpaque(false);
									}});
									add(Box.createVerticalGlue());
									addMouseListener(GAWTUtil.createMaximizeListener(frame));
									addMouseListener(panel.getActiveMouseListener());
								}});
							add(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{ // Close button
								s(this, 30, 30);
								add(Box.createVerticalGlue());
								JPanelBG panel;
								add(panel = new JPanelBG("/res/cross.png", "/res/cross.active.png") {{
									s(this, 14, 14);
									setOpaque(false);
								}});
								add(Box.createVerticalGlue());
								addMouseListener(new MouseAdapter() {
									@Override
									public void mousePressed(MouseEvent e) {
										Main.performWindowAction(LauncherOptions.onLauncherClose);
									}
								});
								addMouseListener(panel.getActiveMouseListener());
							}});
						}});
						
						add(Box.createVerticalGlue());
						
						add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
							add(topGame = new JLabel() {{
								setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));
								setForeground(UIScheme.TITLE_COLOR);
								setText(I18n.get("main.title.game"));
								setFont(new Font(UIScheme.TITLE_FONT, Font.PLAIN, 24));
								addMouseListener(new MouseAdapter() {
									@Override
									public void mouseEntered(MouseEvent e) {
										setForeground(UIScheme.TITLE_COLOR_SEL);
									}
									@Override
									public void mouseExited(MouseEvent e) {
										setForeground((clientPanel != null && clientPanel.getParent() != null) ? UIScheme.TITLE_COLOR_SEL : UIScheme.TITLE_COLOR);
									}
									@Override
									public void mousePressed(MouseEvent e) {
										play.displayPlayPanel();
									}
								});
							}});
							add(new JLabel() {{
								setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));
								setForeground(UIScheme.TITLE_COLOR);
								setText((LauncherOptions.userInfo != null ? LauncherOptions.userInfo.optString("username") : LauncherOptions.sessionUser).toUpperCase());
								setFont(new Font(UIScheme.TITLE_FONT, Font.PLAIN, 24));
								disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
								addMouseListener(new MouseAdapter() {
									@Override
									public void mouseEntered(MouseEvent e) {
										setForeground(UIScheme.TITLE_COLOR_SEL);
									}
									@Override
									public void mouseExited(MouseEvent e) {
										setForeground(UIScheme.TITLE_COLOR);
									}
									@Override
									public void mousePressed(MouseEvent e) {
										LauncherUtil.onenURLInBrowser(Main.USER_CONTROL_PANEL_URL);
									}
								});
							}});
							add(configLabel = new JLabel() {{
								setVisible(false);
								setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));
								setForeground(UIScheme.TITLE_COLOR_SEL);
								setText(I18n.get("settings.title").toUpperCase());
								setFont(new Font(UIScheme.TITLE_FONT, Font.PLAIN, 24));
								disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
							}});
							add(Box.createHorizontalGlue());
						}});
					}
				});
			}});
			add(mainPanel = new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
				setMaximumSize(new Dimension(9999, 9999));
			}});
		}});
		
		frame.pack();
		// Load position from config
		if(Main.getConfig().has("posx") && Main.getConfig().has("posy")) {
			frame.setLocation(Math.max(Main.getConfig().optInt("posx", 0), usableBounds.x), Math.max(Main.getConfig().optInt("posy", 0), usableBounds.y));
			Dimension d = innerPanel.getPreferredSize();
			if(frame.getLocation().x - usableBounds.x + d.width > usableBounds.width || frame.getLocation().y - usableBounds.y + d.height > usableBounds.height)
				frame.setLocationRelativeTo(null);
		} else
			frame.setLocationRelativeTo(null);
		if(Main.getConfig().optBoolean("maximized")) {
			frame.maximize();
			frame.setResizable(false);
		}
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
		java.awt.EventQueue.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		    	frame.toFront();
		    	frame.repaint();
		    }
		});
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				play.displayPlayPanel();
			}
		});
		if(!Main.getConfig().optBoolean("tipshown")) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					final JDialog dialog = new JDialog(frame, true);
					dialog.setUndecorated(true);
					GAWTUtil.safeTransparentBackground(dialog, UIScheme.EMPTY);
					dialog.add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
						setBorder(GAWTUtil.safePopupBorder());
						add(new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.MAIN_MENU_BG) {{
							setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
							JTextPane pane = GAWTUtil.getNiceTextPane(I18n.get("menu.tip"), 300);
							SimpleAttributeSet attribs = new SimpleAttributeSet();
							StyleConstants.setFontFamily(attribs, UIScheme.TITLE_FONT);
							StyleConstants.setFontSize(attribs, 18);
							StyleConstants.setForeground(attribs, UIScheme.TEXT_COLOR);
							StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_CENTER);

							pane.setParagraphAttributes(attribs, true);
							GAWTUtil.fixtTextPaneWidth(pane, 300);
							add(pane);
							
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
								setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
								add(Box.createHorizontalGlue());
								add(new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.MAIN_MENU_BG_SEL) {{
									add(Box.createHorizontalGlue());
									s(this, 50, 28);
									add(new JLabel() {{
										setOpaque(false);
										setAlignmentX(JLabel.CENTER_ALIGNMENT);
										setForeground(UIScheme.TITLE_COLOR_SEL);
										setText(I18n.get("menu.tip.ok"));
										setFont(new Font(UIScheme.TITLE_FONT, Font.BOLD, 20));
									}}, new GridBagConstraints() {{
										weightx = 1;
										weighty = 1;
									}});
									add(Box.createHorizontalGlue());
									addMouseListener(new MouseAdapter() {
										@Override
										public void mousePressed(MouseEvent e) {
											try {
												Main.getConfig().put("tipshown", true);
											} catch(JSONException e1) {}
											dialog.setVisible(false);
											dialog.dispose();
										}
									});
								}});
							}});
							
						}});						
					}});
					dialog.revalidate();
					dialog.pack();
					Point p = logoPanel.getLocationOnScreen();
					dialog.setLocation(p.x, p.y + logoPanel.getHeight());
					dialog.setVisible(true);
					/*SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							dialog.pack();
							dialog.revalidate();
							dialog.repaint();
						}
					});*/
				}
			});
		}
	}
	//@formatter:on
	
	private void saveFrameInfo() {
		JSONObject config = Main.getConfig();
		Rectangle b = frame.getBounds();
		Rectangle b2 = innerPanel.getBounds();
		try {
			boolean maximized = (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
			if(!maximized) { // Update window info only if not maximized
				config.put("posx", b.x);
				config.put("posy", b.y);
				config.put("width", b2.width);
				config.put("height", b2.height);
			}
			config.put("maximized", maximized);
		} catch(JSONException e) {
		}
	}
	
	public void onHide() {
		// TODO : Call and implement
	}
	
	public void onUnhide() {
		// TODO : Call and implement
	}
	
	/**
	 * Must be invoked from AWT Event thread
	 * @param client
	 */
	public void clientStatusUpdate(Client client) {
		play.clientStatusUpdate(client);
	}
	
	static void s(Component c, int width, int height) {
		c.setSize(width, height);
		Dimension d = new Dimension(width, height);
		c.setPreferredSize(d);
		c.setMaximumSize(d);
		c.setMinimumSize(d);
	}
}
