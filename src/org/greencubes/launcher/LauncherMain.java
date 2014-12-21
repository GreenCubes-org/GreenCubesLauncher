package org.greencubes.launcher;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicMenuItemUI;

import org.greencubes.client.Client;
import org.greencubes.client.IClientStatus;
import org.greencubes.client.IClientStatus.Status;
import org.greencubes.main.Main;
import org.greencubes.swing.AbstractComponentListener;
import org.greencubes.swing.AbstractMouseListener;
import org.greencubes.swing.AbstractWindowListener;
import org.greencubes.swing.GAWTUtil;
import org.greencubes.swing.JPanelBG;
import org.greencubes.swing.DropdownListener;
import org.greencubes.swing.RoundedCornerBorder;
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
	
	//@formatter:off
	/**
	 * Should not be invoked in AWT thread
	 */
	public LauncherMain(Window previousFrame) {
		frame = new UndecoratedJFrame(I18n.get("title"));
		frame.setIconImages(LauncherOptions.getIcons());
		frame.setMinimumSize(new Dimension(640, 320));
		frame.setMaximumSize(new Dimension(1440, 960));
		frame.add(innerPanel = new JPanel() {{
			Dimension d = new Dimension(Main.getConfig().optInt("width", 900), Main.getConfig().optInt("height", 640));
			setPreferredSize(d);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBackground(new Color(23, 30, 30, 255));
			setBorder(BorderFactory.createLineBorder(new Color(11, 33, 31, 255), 1));
			// Top line
			add(new JPanel() {{
				setBackground(new Color(71, 87, 85, 255));
				setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
				add(new JPanelBG("/res/main.logo.png") {{ // GreenCubes logo
					setBackground(new Color(0, 0, 0, 0));
					s(this, 98, 95);
					paddingTop = 15;
					paddingLeft = 16;
					final JPopupMenu mainPopup = new JPopupMenu();
					addMouseListener(new DropdownListener(mainPopup, 0, 95));
					mainPopup.add(new JMenuItem("Test item 1") {{
						setBackground(new Color(115, 146, 146, 255));
						setForeground(new Color(192, 228, 232, 255));
						setFont(new Font("ClearSans Light", Font.PLAIN, 18));
						setUI(new BasicMenuItemUI() {{
							selectionBackground = new Color(155, 193, 193, 255);
							selectionForeground = new Color(236, 255, 255, 255);
						}});
						setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
					}});
					mainPopup.add(new JMenuItem("Test item 2") {{
						setBackground(new Color(115, 146, 146, 255));
						setForeground(new Color(192, 228, 232, 255));
						setFont(new Font("ClearSans Light", Font.PLAIN, 18));
						setUI(new BasicMenuItemUI() {{
							selectionBackground = new Color(155, 193, 193, 255);
							selectionForeground = new Color(236, 255, 255, 255);
						}});
						setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
					}});
					mainPopup.add(new JMenuItem("Test item 3") {{
						setBackground(new Color(115, 146, 146, 255));
						setForeground(new Color(192, 228, 232, 255));
						setFont(new Font("ClearSans Light", Font.PLAIN, 18));
						setUI(new BasicMenuItemUI() {{
							selectionBackground = new Color(155, 193, 193, 255);
							selectionForeground = new Color(236, 255, 255, 255);
						}});
						setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
					}});
					
					//mainPopup.setBackground(new Color(0, 0, 0, 0));
					
					mainPopup.setBorder(GAWTUtil.popupBorder());
				}});
				
				add(new JPanel() {{ // Everything else on top
					setMaximumSize(new Dimension(9999, 95));
					setOpaque(false);
					setBackground(new Color(0, 0, 0, 0));
					setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
					add(new JPanel() {{ // Window buttons
						setBackground(new Color(0, 0, 0, 0));
						setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
						add(Box.createHorizontalGlue());
						add(new JPanel() {{ // Minimize button
							s(this, 25, 25);
							setBackground(new Color(0, 0, 0, 0));
							add(new JPanelBG("/res/minimize.png") {{
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
						add(new JPanel() {{ // Maximize button
							s(this, 25, 25);
							setBackground(new Color(0, 0, 0, 0));
							add(new JPanelBG("/res/expand.png") {{
								s(this, 14, 14);
								setBackground(new Color(0, 0, 0, 0));
							}});
							addMouseListener(new AbstractMouseListener() {
								// TODO : Can add cross animation
								@Override
								public void mouseClicked(MouseEvent e) {
									int state = frame.getExtendedState();
									if((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
										frame.setExtendedState(Frame.NORMAL);
										frame.setResizable(true);
									} else {
										frame.maximize();
										frame.setResizable(false);
									}
								}
							});
						}});
						add(new JPanel() {{ // Close button
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
					
					add(Box.createVerticalGlue());
					
					add(new JPanel() {{
						setBackground(new Color(0, 0, 0, 0));
						setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
						add(new JLabel() {{
							//setOpaque(false);
							setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));
							setBackground(Util.debugColor());
							setForeground(new Color(126, 209, 218, 255));
							setText(I18n.get("main.title.game"));
							setFont(new Font("Lato", Font.PLAIN, 24));
							disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
						}});
						add(new JLabel() {{
							//setOpaque(false);
							setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));
							setBackground(Util.debugColor());
							setForeground(new Color(126, 209, 218, 255));
							setText(I18n.get("main.title.shop"));
							setFont(new Font("Lato", Font.PLAIN, 24));
							disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
						}});
						add(new JLabel() {{
							//setOpaque(false);
							setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));
							setBackground(Util.debugColor());
							setForeground(new Color(126, 209, 218, 255));
							setText(I18n.get("main.title.updates"));
							setFont(new Font("Lato", Font.PLAIN, 24));
							disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
						}});
						add(new JLabel() {{
							//setOpaque(false);
							setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));
							setBackground(Util.debugColor());
							setForeground(new Color(126, 209, 218, 255));
							setText((LauncherOptions.userInfo != null ? LauncherOptions.userInfo.optString("username") : LauncherOptions.sessionUser).toUpperCase());
							setFont(new Font("Lato", Font.PLAIN, 24));
							disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
						}});
						add(Box.createHorizontalGlue());
					}});
				}});
			}});
			add(mainPanel = new JPanel() {{
				setOpaque(false);
				setBackground(new Color(0, 0, 0, 0));
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
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));
		mainPanel.add(new JPanel() {{
			//setOpaque(false);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBackground(new Color(46, 54, 54, 255));
			add(new JPanel() {{
				s(this, 1, 1);
				setBackground(new Color(0, 0, 0, 0));
			}});
			add(new JPanel() {{ // New client button
				//setOpaque(false);
				s(this, 95, 95);
				setBackground(new Color(0, 0, 0, 0));
				add(new JPanel() {{ // Inner
					s(this, 95, 95);
					setOpaque(false);
					setBackground(new Color(0, 0, 0, 0));
					add(new JPanelBG("/res/main.oldclient.logo.png") {{
						s(this, 48, 48);
					}});
					JComponent pane;
					add(pane = new JLabel() {{
						setOpaque(false);
						setAlignmentX(JLabel.CENTER_ALIGNMENT);
						setBackground(new Color(0, 0, 0, 100));
						setForeground(new Color(126, 209, 218, 255));
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
						it.setBackground(new Color(96, 122, 122, 255));
						it.revalidate();
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						it.setBackground(new Color(46, 54, 54, 255));
						it.revalidate();
					}
				});
			}});
			
			add(Box.createVerticalGlue());
		}});
		mainPanel.add(clientPanel = new JPanel() {{
			setOpaque(false);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBackground(new Color(23, 30, 30, 255));
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
				JFXPanel browserPanel = new JFXPanel();
				add(browserPanel, new GridBagConstraints() {{
					weightx = 1;
					weighty = 1;
					fill = GridBagConstraints.BOTH;
				}});
				openClientBrowser(currentClient, browserPanel);
			}});
			clientPanel.add(new JPanel() {{
				setOpaque(false);
				setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
				setBorder(BorderFactory.createEmptyBorder(22, 64, 22, 64));
				add(new JPanel() {{
					setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
					setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
					s(this, 0, 40);
					setMaximumSize(new Dimension(9999, 9999));
					setOpaque(false);
					add(clientStatusPanel = new JPanel() {{
						setOpaque(false);
						setMaximumSize(new Dimension(9999, 9999));
						setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
						setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
					}});
					add(new JPanel() {{
						s(this, 272, 22);
						setOpaque(false);
						// TODO : Server selection
					}});
				}});
				add(new JPanel() {{
					setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
					add(Box.createHorizontalGlue());
					setOpaque(false);
					add(clientStatusLine = new JLabel() {{
						setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 24));
						setOpaque(false);
						setBackground(Util.debugColor());
						setAlignmentX(JLabel.RIGHT_ALIGNMENT);
						setAlignmentY(JLabel.CENTER_ALIGNMENT);
						setForeground(new Color(192, 229, 237, 255));
						setText(currentClient.getStatus().getStatusTitle());
						setFont(new Font("ClearSans Light", Font.PLAIN, 14));
						disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
						setMinimumSize(new Dimension(0, 66));
					}});
					add(new JPanel() {{
						s(this, 272, 66);
						setBorder(new RoundedCornerBorder(new Color(23, 30, 30, 255), null, 4));
						setBackground(new Color(116, 147, 147, 255));
						setLayout(new GridBagLayout());
						add(clientButtonText = new JLabel() {{
							setOpaque(false);
							setAlignmentX(JLabel.CENTER_ALIGNMENT);
							setForeground(new Color(236, 255, 255, 255));
							Status s = currentClient.getStatus().getStatus();
							setText(I18n.get(s.statusActionName == null ? s.statusName : s.statusActionName).toUpperCase());
							setFont(new Font("Lato", Font.BOLD, 36));
							//disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
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
			clientStatusLine.setText(client.getStatus().getStatusTitle());
			if(clientStatus.getStatusProgress() < 0 && progressBarContainer != null) {
				clientStatusPanel.remove(progressBarContainer);
				clientStatusPanel.revalidate();
				progressBarContainer = null;
			} else if(clientStatus.getStatusProgress() >= 0) {
				if(progressBarContainer == null) {
					clientStatusPanel.add(progressBarContainer = new JPanel() {{
						setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
						Insets isc = clientStatusPanel.getBorder().getBorderInsets(clientStatusPanel);
						s(this, clientStatusPanel.getWidth() - isc.left - isc.right, 22);
						setBackground(new Color(68, 84, 83, 255));
						setBorder(new RoundedCornerBorder(new Color(23, 30, 30, 255), new Color(111, 134, 138, 255), 4));
						add(new JPanel() {{
							s(this, 1, 1);
							setBackground(new Color(0, 0, 0, 0));
						}});
						add(progressBar = new JPanel() {{
							setBackground(new Color(192, 229, 237, 255));
							s(this, 18, 18);
							setBorder(new RoundedCornerBorder(new Color(68, 84, 83, 255), null, 4));
						}});
						
					}});
					clientStatusPanel.getLayout().layoutContainer(clientStatusPanel);
				}
				s(progressBar, (int) ((progressBarContainer.getWidth() - 4) * clientStatus.getStatusProgress()), 18);
				clientStatusPanel.revalidate();
			}
		}
	}
	
	private void openClientBrowser(final Client client, final JFXPanel panel) {
		 Platform.runLater(new Runnable() {
            @Override 
            public void run() {
                browser = new WebView();
                WebEngine engine = browser.getEngine();
                panel.setScene(new Scene(browser));
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
