package org.greencubes.launcher;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.greencubes.client.Client;
import org.greencubes.client.IClientStatus;
import org.greencubes.client.IClientStatus.Status;
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
	
	private JFrame frame;
	private JPanel mainPanel;
	private JPanel innerPanel;
	private JPanel clientPanel;
	
	private WebView browser;
	
	private Client currentClient;
	private JTextPane clientButtonText;
	private JComponent clientButton;
	private JTextPane clientStatusLine;
	private JComponent clientStatusPanel;
	private JComponent progressBarContainer;
	private JComponent progressBar;
	
	//@formatter:off
	/**
	 * Should not be invoked in AWT thread
	 */
	public LauncherMain(Window previousFrame) {
		frame = new JFrame(I18n.get("title"));
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
					add(new JPanel() {{ // Minimize button
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
								add(new JMenuItem("Log Out"){{
									addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											Client.MAIN.openBrowserPage(browser.getEngine());
										}
									});
								}});
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
			config.put("posx", b.x);
			config.put("posy", b.y);
			config.put("width", b2.width);
			config.put("height", b2.height);
		} catch(JSONException e) {
		}
	}
	
	//@formatter:off
	private void displayPlayPanel() {
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
						setText(I18n.get("client.main.name"));
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
				s(browserPanel, 100, 100);
				openClientBrowser(currentClient, browserPanel);
			}});
			clientPanel.add(new JPanel() {{
				//s(this, -1, 100);
				setOpaque(false);
				setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
				add(new JPanel() {{
					s(this, 10, 100);
					setBackground(new Color(0, 0, 0, 0));
				}});
				add(new JPanel() {{
					setLayout(new GridBagLayout());
					s(this, 170, 100);
					setBackground(new Color(0, 0, 0, 0));
					add(clientButton = new JPanel() {{
						s(this, 150, 80);
						setBackground(new Color(52, 53, 50, 255));
						setLayout(new GridBagLayout());
						add(clientButtonText = new JTextPane() {{
							setOpaque(false);
							StyledDocument doc = getStyledDocument();
							SimpleAttributeSet center = new SimpleAttributeSet();
							StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
							doc.setParagraphAttributes(0, doc.getLength(), center, false);
							setBackground(new Color(0, 0, 0, 0));
							setForeground(new Color(25, 97, 14, 255));
							setEditable(false);
							Status s = currentClient.getStatus().getStatus();
							setText(I18n.get(s.statusActionName == null ? s.statusName : s.statusActionName));
							setFont(new Font("ClearSans", Font.BOLD, 25));
							disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
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
					}}, new GridBagConstraints() {{
						weightx = 1;
						weighty = 1;
					}});
					
				}});
				add(new JPanel() {{
					setOpaque(false);
					setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
					add(new JPanel() {{
						setBackground(new Color(0, 0, 0, 0));
						s(this, 10, 10);
					}});
					add(new JPanel() {{
						setOpaque(false);
						setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
						add(clientStatusPanel = new JPanel() {{
							setBackground(new Color(0, 0, 0, 0));
							setMaximumSize(new Dimension(800, 9999));
							setLayout(new BorderLayout());
						}});
						setMaximumSize(new Dimension(9999, 9999));
					}});
					add(clientStatusLine = new JTextPane() {{
						setMaximumSize(new Dimension(9999, 25));
						setOpaque(false);
						StyledDocument doc = getStyledDocument();
						SimpleAttributeSet center = new SimpleAttributeSet();
						StyleConstants.setAlignment(center, StyleConstants.ALIGN_LEFT);
						doc.setParagraphAttributes(0, doc.getLength(), center, false);
						setBackground(Util.debugColor());
						setForeground(new Color(0, 0, 0, 255));
						setEditable(false);
						setText(currentClient.getStatus().getStatusTitle());
						setFont(new Font("ClearSans", Font.PLAIN, 14));
						disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
					}});
					add(new JPanel() {{
						setBackground(new Color(0, 0, 0, 0));
						s(this, 10, 10);
					}});
				}});
			}});
			frame.revalidate();
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
			clientButtonText.setText(I18n.get(s.statusActionName == null ? s.statusName : s.statusActionName));
			clientStatusLine.setText(client.getStatus().getStatusTitle());
			if(clientStatus.getStatusProgress() < 0 && progressBarContainer != null) {
				clientStatusPanel.remove(progressBarContainer);
				clientStatusPanel.revalidate();
				progressBarContainer = null;
			} else if(clientStatus.getStatusProgress() >= 0) {
				if(progressBarContainer == null) {
					clientStatusPanel.add(progressBarContainer = new JPanel() {{
						setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
						setMinimumSize(new Dimension(20, 20));
						setMaximumSize(new Dimension(9999, 20));
						setBackground(new Color(46, 94, 44, 255));
						add(new JPanel() {{
							setOpaque(true);
							s(this, 2, 2);
							setBackground(new Color(46, 94, 44, 255));
						}});
						add(new JPanel() {{
							setBackground(new Color(46, 94, 44, 255));
							setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
							add(new JPanel() {{
								s(this, 2, 2);
								setBackground(new Color(46, 94, 44, 255));
							}});
							add(progressBar = new JPanel() {{
								setBackground(new Color(94, 94, 94, 255));
								s(this, 16, 16);
							}});
							add(new JPanel() {{
								s(this, 2, 2);
								setBackground(new Color(46, 94, 44, 255));
							}});
						}});
						
					}}, BorderLayout.PAGE_END);
					clientStatusPanel.revalidate();
				}
				s(progressBar, (int) ((progressBarContainer.getWidth() - 4) * clientStatus.getStatusProgress()), 16);
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
