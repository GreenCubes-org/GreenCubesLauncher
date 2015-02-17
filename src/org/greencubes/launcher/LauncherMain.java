package org.greencubes.launcher;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.greencubes.client.Client;
import org.greencubes.client.IClientStatus;
import org.greencubes.client.IClientStatus.Status;
import org.greencubes.client.Server;
import org.greencubes.main.Main;
import org.greencubes.swing.AbstractComponentListener;
import org.greencubes.swing.AbstractMouseListener;
import org.greencubes.swing.AbstractWindowListener;
import org.greencubes.swing.GAWTUtil;
import org.greencubes.swing.GJBoxPanel;
import org.greencubes.swing.GPopupMenu;
import org.greencubes.swing.JPanelBG;
import org.greencubes.swing.DropdownListener;
import org.greencubes.swing.RoundedCornerBorder;
import org.greencubes.swing.UIScheme;
import org.greencubes.swing.UndecoratedJFrame;
import org.greencubes.util.I18n;
import org.greencubes.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

public class LauncherMain {
	
	private UndecoratedJFrame frame;
	private JPanel mainPanel;
	private JPanel innerPanel;
	private JPanel clientPanel;
	
	private WebView browser;
	
	private Client currentClient;
	private JLabel clientButtonText;
	private JLabel clientStatusLine;
	private JComponent clientStatusPanel;
	private JComponent progressBarContainer;
	private JComponent progressBar;
	private JComponent serverSelect;
	private JPanel serverSelectPanel;
	private JLabel selectedServerName;
	private GPopupMenu serverListMenu;
	private List<Server> currentServerList = new ArrayList<Server>();
	private Server lastSelectedServer;
	private JPanelBG logoPanel;
	
	//@formatter:off
	/**
	 * Should not be invoked in AWT thread
	 */
	public LauncherMain(Window previousFrame) {
		frame = new UndecoratedJFrame(I18n.get("title"));
		Main.currentFrame = frame;
		frame.setIconImages(LauncherOptions.getIcons());
		frame.setMinimumSize(new Dimension(640, 320));
		frame.setMaximumSize(new Dimension(1440, 960));
		frame.add(innerPanel = new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.BACKGROUND) {{
			setPreferredSize(new Dimension(Main.getConfig().optInt("width", 900), Main.getConfig().optInt("height", 640)));
			// Top line
			add(new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.EMPTY) {{
				add(logoPanel = new JPanelBG("/res/main.logo.png") {{ // GreenCubes logo
					s(this, 96, 96);
					final GPopupMenu mainPopup = new GPopupMenu(false);
					addMouseListener(new DropdownListener(mainPopup, 0, 89, 200L, 0, 0));
					mainPopup.setMenuFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 18));
					mainPopup.setMenuColors(UIScheme.MAIN_MENU_BG, UIScheme.TITLE_COLOR, UIScheme.MAIN_MENU_BG_SEL, UIScheme.TITLE_COLOR_SEL);
					mainPopup.setMenuBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
					
					JMenuItem item = mainPopup.addItem(I18n.get("menu.settings"), "/res/menu.settings.png");
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							new LauncherConfig(frame);
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
							new Thread() {
								@Override
								public void run() {
									new LauncherLogin(null);
								}
							}.start();
						}
					});
					//item = mainPopup.addItem(I18n.get("menu.offline"), "/res/menu.offline.png");
					mainPopup.setOpaque(false);
					mainPopup.setBorder(GAWTUtil.popupBorder());
					mainPopup.validate();
				}});
				
				add(new JPanelBG("/res/main.top.png") {{ // Everything else on top
					setMaximumSize(new Dimension(9999, 96));
					setBackground(UIScheme.TOP_PANEL_BG);
					setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
					add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{ // Window buttons
						add(Box.createHorizontalGlue());
						add(new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.EMPTY) {{ // Minimize button
							s(this, 30, 30);
							setAlignmentX(JComponent.CENTER_ALIGNMENT);
							add(Box.createVerticalGlue());
							add(new JPanelBG("/res/minimize.png") {{
								s(this, 14, 14);
								setBackground(UIScheme.EMPTY);
							}});
							add(Box.createVerticalGlue());
							addMouseListener(GAWTUtil.createMinimizeListener(frame));
						}});
						add(new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.EMPTY) {{ // Maximize button
							s(this, 30, 30);
							add(Box.createVerticalGlue());
							add(new JPanelBG("/res/expand.png") {{
								s(this, 14, 14);
								setBackground(UIScheme.EMPTY);
							}});
							add(Box.createVerticalGlue());
							addMouseListener(GAWTUtil.createMaximizeListener(frame));
						}});
						add(new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.EMPTY) {{ // Close button
							s(this, 30, 30);
							add(Box.createVerticalGlue());
							add(new JPanelBG("/res/cross.png") {{
								s(this, 14, 14);
								setBackground(UIScheme.EMPTY);
								addMouseListener(GAWTUtil.createCloseListener(frame));
							}});
							add(Box.createVerticalGlue());
							addMouseListener(GAWTUtil.createCloseListener(frame));
						}});
					}});
					
					add(Box.createVerticalGlue());
					
					add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
						add(new JLabel() {{
							setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));
							setForeground(UIScheme.TITLE_COLOR);
							setText(I18n.get("main.title.game"));
							setFont(new Font(UIScheme.TITLE_FONT, Font.PLAIN, 24));
							disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
						}});
						add(new JLabel() {{
							setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));
							setForeground(UIScheme.TITLE_COLOR);
							setText((LauncherOptions.userInfo != null ? LauncherOptions.userInfo.optString("username") : LauncherOptions.sessionUser).toUpperCase());
							setFont(new Font(UIScheme.TITLE_FONT, Font.PLAIN, 24));
							disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
						}});
						add(Box.createHorizontalGlue());
					}});
				}});
			}});
			add(mainPanel = new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
				setMaximumSize(new Dimension(9999, 9999));
			}});
		}});
		
		frame.pack();
		// Load position from config
		if(Main.getConfig().has("posx") && Main.getConfig().has("posy")) {
			frame.setLocation(Main.getConfig().optInt("posx", 0), Main.getConfig().optInt("posy", 0));
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
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				displayPlayPanel();
			}
		});
		if(!Main.getConfig().optBoolean("tipshown") || Main.TEST) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					final JDialog dialog = new JDialog(frame, true);
					dialog.setUndecorated(true);
					dialog.setBackground(UIScheme.EMPTY);
					dialog.add(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
						setBorder(GAWTUtil.popupBorder());
						add(new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.MAIN_MENU_BG) {{
							setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
							JTextPane pane = GAWTUtil.fixedWidthTextPane(I18n.get("menu.tip"), 300);
							pane.setForeground(new Color(176, 230, 238, 255));
							pane.setFont(new Font(UIScheme.TITLE_FONT, Font.PLAIN, 14));
							GAWTUtil.fixtTextPaneWidth(pane, 300);
							add(pane);
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
								setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
								add(Box.createHorizontalGlue());
								add(new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.BACKGROUND) {{
									add(Box.createHorizontalGlue());
									s(this, 50, 28);
									add(new JLabel() {{
										setOpaque(false);
										setAlignmentX(JLabel.CENTER_ALIGNMENT);
										setForeground(UIScheme.TEXT_COLOR);
										setText(I18n.get("menu.tip.ok"));
										setFont(new Font(UIScheme.TITLE_FONT, Font.BOLD, 20));
									}}, new GridBagConstraints() {{
										weightx = 1;
										weighty = 1;
									}});
									add(Box.createHorizontalGlue());
									addMouseListener(new AbstractMouseListener() {
										@Override
										public void mouseClicked(MouseEvent e) {
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
					//dialog.revalidate();
					dialog.pack();
					Point p = logoPanel.getLocationOnScreen();
					dialog.setLocation(p.x, p.y + logoPanel.getHeight());
					dialog.setVisible(true);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							dialog.pack();
							dialog.revalidate();
							
							dialog.repaint();
						}
					});
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
	
	//@formatter:off
	private void displayPlayPanel() {
		/*mainPanel.add(new JPanelBG("/res/main.right.shadow.png") {{
			//setOpaque(false);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBackground(new Color(38, 51, 51, 255));
			add(new JPanel() {{
				s(this, 1, 24);
				setBackground(new Color(0, 0, 0, 0));
			}});
			add(new JPanel() {{ // New client button
				s(this, 96, 96);
				setBackground(new Color(38, 51, 51, 255));
				add(new JPanel() {{ // Inner
					s(this, 96, 96);
					setOpaque(false);
					add(new JPanelBG("/res/main.oldclient.logo.png") {{
						s(this, 48, 48);
					}});
					JComponent pane;
					add(pane = new JLabel() {{
						setOpaque(false);
						setAlignmentX(JLabel.CENTER_ALIGNMENT);
						setBackground(new Color(0, 0, 0, 100));
						setForeground(new Color(176, 230, 238, 255));
						setText(I18n.get("client.main.name"));
						setFont(new Font("ClearSans Light", Font.PLAIN, 14));
						disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
					}});
					GAWTUtil.removeMouseListeners(pane);
				}});
				final JPanel it = this;
				addMouseListener(new AbstractMouseListener() {
					@Override
					public void mouseEntered(MouseEvent e) {
						it.setBackground(new Color(82, 123, 123, 255));
						it.revalidate();
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						it.setBackground(new Color(38, 51, 51, 255));
						it.revalidate();
					}
				});
			}});
			
			add(Box.createVerticalGlue());
		}});*/
		mainPanel.add(clientPanel = new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
			setBackground(UIScheme.BACKGROUND);
		}});
		frame.revalidate();
		currentClient = null;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				displayClient(Client.MAIN);
			}
		});
	}
	//@formatter:on
	
	//@formatter:off
	private void displayClient(Client client) {
		if(client == currentClient)
			return;
		synchronized(client) {
			currentClient = client;
			clientPanel.add(new JPanel() {{
				setOpaque(false);
				setLayout(new GridBagLayout());
				final JFXPanel browserPanel = new JFXPanel();
				add(browserPanel, new GridBagConstraints() {{
					gridx = 0;
					gridy = 0;
					weightx = 1;
					weighty = 1;
					fill = GridBagConstraints.BOTH;
				}});
				add(new JPanel() {{
					s(this, 4, 4);
					setBackground(UIScheme.BACKGROUND);
				}}, new GridBagConstraints() {{
					gridx = 1;
					gridy = 0;
				}});
				openClientBrowser(currentClient, browserPanel);
			}});
			clientPanel.add(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
				setBorder(BorderFactory.createEmptyBorder(22, 64, 22, 64));
				add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
					setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
					s(this, 0, 40);
					setMaximumSize(new Dimension(9999, 9999));
					add(clientStatusPanel = new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
						setMaximumSize(new Dimension(9999, 9999));
						setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
					}});
					add(serverSelectPanel = new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
						s(this, 272, 24);
					}});
				}});
				add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
					add(Box.createHorizontalGlue());
					add(clientStatusLine = new JLabel() {{
						setOpaque(false);
						setBackground(Util.debugColor());
						setHorizontalAlignment(SwingConstants.RIGHT);
						setVerticalAlignment(SwingConstants.CENTER);
						setForeground(new Color(176, 230, 238, 255));
						setText(currentClient.getStatus().getStatusTitle());
						setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 14));
						disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
						setMinimumSize(new Dimension(0, 66));
					}});
					add(Box.createHorizontalStrut(24));
					add(new JPanel() {{
						s(this, 272, 66);
						setBorder(new RoundedCornerBorder(UIScheme.BACKGROUND, null, 4));
						setBackground(UIScheme.BIG_BUTTON);
						setLayout(new GridBagLayout());
						add(clientButtonText = new JLabel() {{
							setOpaque(false);
							setAlignmentX(JLabel.CENTER_ALIGNMENT);
							setForeground(new Color(229, 255, 255, 255));
							Status s = currentClient.getStatus().getStatus();
							setText(I18n.get(s.statusActionName == null ? s.statusName : s.statusActionName).toUpperCase());
							setFont(new Font(UIScheme.TITLE_FONT, Font.BOLD, 36));
						}}, new GridBagConstraints() {{
							weightx = 1;
							weighty = 1;
						}});
						GAWTUtil.removeMouseListeners(clientButtonText);
						addMouseListener(new AbstractMouseListener() {
							@Override
							public void mouseClicked(MouseEvent e) {
								pressTheBigButton();
							}
						});
					}});
				}});	
			}});
			frame.revalidate();
			Platform.runLater(new Runnable() { // We should wait while browser loads
	            @Override 
	            public void run() {
	            	currentClient.openBrowserPage(browser.getEngine());
	    			currentClient.load(LauncherMain.this);
	            }
	        });
		}
	}
	//@formatter:on
	
	/**
	 * Press "Play" or other big button on clinet page.
	 */
	private void pressTheBigButton() {
		if(currentClient != null)
			currentClient.doJob();
	}
	
	/**
	 * Must be invoked from AWT Event thread
	 * @param client
	 */
	public void clientStatusUpdate(Client client) {
		if(client == currentClient && clientPanel != null) {
			IClientStatus clientStatus = client.getStatus();
			Status s = clientStatus.getStatus();
			clientButtonText.setText(I18n.get(s.statusActionName == null ? s.statusName : s.statusActionName).toUpperCase());
			clientStatusLine.setText("<html><div style=\"text-align: right; width: 100%;\">" + client.getStatus().getStatusTitle().replace("\n", "<br>") + "</div></html>");
			if(clientStatus.getStatusProgress() < 0 && progressBarContainer != null) {
				clientStatusPanel.remove(progressBarContainer);
				clientStatusPanel.revalidate();
				progressBarContainer = null;
			} else if(clientStatus.getStatusProgress() >= 0) {
				if(progressBarContainer == null) {
					clientStatusPanel.add(progressBarContainer = new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.PROGRESSBAR_BG) {
						{
							Insets isc = clientStatusPanel.getBorder().getBorderInsets(clientStatusPanel);
							s(this, clientStatusPanel.getWidth() - isc.left - isc.right, 22);
							setBorder(new RoundedCornerBorder(UIScheme.BACKGROUND, UIScheme.PROGRESSBAR_BORDER, 4));
							add(new JPanel() {
								{
									s(this, 1, 1);
									setBackground(UIScheme.EMPTY);
								}
							});
							add(progressBar = new JPanel() {
								{
									setBackground(UIScheme.PROGRESSBAR_BAR);
									s(this, 18, 18);
									setBorder(new RoundedCornerBorder(UIScheme.PROGRESSBAR_BG, null, 4));
								}
							});
							
						}
					});
					clientStatusPanel.getLayout().layoutContainer(clientStatusPanel);
				}
				s(progressBar, (int) ((progressBarContainer.getWidth() - 4) * clientStatus.getStatusProgress()), 18);
				clientStatusPanel.revalidate();
			}
			if(clientStatus.getStatus() == Status.READY && currentClient.getServers().size() > 0) {
				if(serverSelect == null) {
					serverSelectPanel.add(serverSelect = new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.MENU_BG) {
						{
							setBorder(BorderFactory.createLineBorder(UIScheme.MENU_BORDER, 1));
							s(this, 272, 24);
							serverListMenu = new GPopupMenu(true);
							serverListMenu.setMenuColors(UIScheme.MENU_DD_BG, UIScheme.TITLE_COLOR, UIScheme.MENU_DD_BG_SEL, UIScheme.TITLE_COLOR_SEL);
							addMouseListener(new MouseAdapter() {
								@Override
								public void mousePressed(MouseEvent e) {
									if(e.isConsumed())
										return;
									if(e.getButton() == MouseEvent.BUTTON1) {
										if(serverListMenu.isVisible()) {
											serverListMenu.setVisible(false);
										} else {
											serverListMenu.show(serverSelect, false);
										}
									}
								}
							});
							serverListMenu.setBorder(BorderFactory.createEmptyBorder());
							serverListMenu.setOpaque(false);
							serverListMenu.setMenuSize(new Dimension(272, 24));
							serverListMenu.setMenuFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
							add(new JPanel() {
								{
									s(this, 20, 0);
									setBackground(UIScheme.EMPTY);
								}
							});
							add(selectedServerName = new JLabel("") {
								{
									setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
									setForeground(UIScheme.TITLE_COLOR);
								}
							});
							add(Box.createHorizontalGlue());
							add(new JPanelBG("/res/menu.arrow.png") {
								{
									s(this, 22, 22);
									setBackground(UIScheme.EMPTY);
								}
							});
						}
					});
				}
				updateServerList();
			} else if(serverSelect != null) {
				serverSelect.getParent().remove(serverSelect);
				serverSelect = null;
			}
		}
	}
	
	private void updateServerList() {
		selectedServerName.setText(LauncherMain.this.currentClient.getSelectedServer().name);
		final List<Server> servers = currentClient.getServers();
		Server current = currentClient.getSelectedServer();
		if(current != lastSelectedServer || !servers.containsAll(currentServerList) || !currentServerList.containsAll(servers)) {
			lastSelectedServer = current;
			serverListMenu.removeAll();
			for(int i = 0; i < servers.size(); ++i) {
				JMenuItem item = serverListMenu.addItem(servers.get(i).name, servers.get(i) == current ? "/res/menu.check.png" : "/res/menu.empty.png");
				item.setIconTextGap(0);
				final int i1 = i;
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						currentClient.selectServer(servers.get(i1));
						updateServerList();
					}
				});
			}
			currentServerList.clear();
			currentServerList.addAll(servers);
		}
		serverListMenu.validate();
	}
	
	private void openClientBrowser(final Client client, final JFXPanel panel) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				browser = new WebView();
				WebEngine engine = browser.getEngine();
				Scene sc = new Scene(browser);
				sc.getStylesheets().add(LauncherMain.class.getResource("/res/scrollbar.css").toExternalForm());
				panel.setScene(sc);
				client.openBrowserPage(engine);
			}
		});
	}
	
	private static void s(Component c, int width, int height) {
		c.setSize(width, height);
		Dimension d = new Dimension(width, height);
		c.setPreferredSize(d);
		c.setMaximumSize(d);
		c.setMinimumSize(d);
	}
}
