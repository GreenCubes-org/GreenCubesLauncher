package org.greencubes.launcher;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import netscape.javascript.JSObject;

import org.greencubes.client.Client;
import org.greencubes.client.IClientStatus;
import org.greencubes.client.Server;
import org.greencubes.client.IClientStatus.Status;
import org.greencubes.main.Main;
import org.greencubes.swing.GAWTUtil;
import org.greencubes.swing.GJBoxPanel;
import org.greencubes.swing.GPopupMenu;
import org.greencubes.swing.JPanelBG;
import org.greencubes.swing.RoundedCornerBorder;
import org.greencubes.swing.UIScheme;
import org.greencubes.util.I18n;
import org.greencubes.util.URLHandler;
import org.greencubes.util.Util;

public class LauncherMain$Play {
	
	private final LauncherMain superClass;
	
	private WebView browser;
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
	private Map<Client, JPanel> clientButtons = new HashMap<Client, JPanel>();
	
	Client currentClient;
	
	LauncherMain$Play(LauncherMain superClass) {
		this.superClass = superClass;
	}
	
	//@formatter:off
	void displayPlayPanel() {
		if(superClass.clientPanel != null && superClass.clientPanel.getParent() != null)
			return;
		superClass.topGame.setForeground(UIScheme.TITLE_COLOR_SEL);
		if(superClass.configPanel != null) {
			superClass.configPanel.getParent().remove(superClass.configPanel);
			superClass.configPanel = null;
			superClass.configLabel.setVisible(false);
		}
		if(Main.enableOldClient || Main.ENABLE_TEST_CLIENT)
			superClass.mainPanel.add(superClass.clientSelectPanel = new JPanelBG("/res/main.right.shadow.png") {{
				//setOpaque(false);
				setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
				setBackground(new Color(38, 51, 51, 255));
				add(new JPanel() {{
					s(this, 1, 24);
					setBackground(new Color(0, 0, 0, 0));
				}});
				
				if(Main.enableOldClient) {
					add(new JPanel() {{ // Old client button
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
								setText("<html><div style=\"text-align: center; width: 100%;\">" + Client.OLD.localizedName.replace("\n", "<br>") + "</div></html>");
								setFont(new Font("ClearSans Light", Font.PLAIN, 12));
								disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
							}});
							GAWTUtil.removeMouseListeners(pane);
						}});
						final JPanel it = this;
						addMouseListener(new MouseAdapter() {
							@Override
							public void mouseEntered(MouseEvent e) {
								it.setBackground(new Color(109, 160, 160, 255));
								it.revalidate();
							}
							
							@Override
							public void mouseExited(MouseEvent e) {
								if(currentClient != Client.OLD){ 
									it.setBackground(new Color(38, 51, 51, 255));
								} else {
									it.setBackground(new Color(82, 123, 123, 255));
								}
								it.revalidate();
							}
							
							@Override
							public void mouseClicked(MouseEvent e) {
								displayClient(Client.OLD);
							}
						});
						clientButtons.put(Client.OLD, it);
					}});
				}
				
				add(new JPanel() {{ // New client button
					s(this, 96, 96);
					setBackground(new Color(38, 51, 51, 255));
					add(new JPanel() {{ // Inner
						s(this, 96, 96);
						setOpaque(false);
						add(new JPanelBG("/res/main.client.logo.png") {{
							setOpaque(false);
							s(this, 48, 48);
						}});
						JComponent pane;
						add(pane = new JLabel() {{
							setOpaque(false);
							setAlignmentX(JLabel.CENTER_ALIGNMENT);
							setBackground(new Color(0, 0, 0, 100));
							setForeground(new Color(176, 230, 238, 255));
							setText("<html><div style=\"text-align: center; width: 100%;\">" + Client.MAIN.localizedName.replace("\n", "<br>") + "</div></html>");
							setFont(new Font("ClearSans Light", Font.PLAIN, 12));
							disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
						}});
						GAWTUtil.removeMouseListeners(pane);
					}});
					final JPanel it = this;
					addMouseListener(new MouseAdapter() {
						@Override
						public void mouseEntered(MouseEvent e) {
							it.setBackground(new Color(109, 160, 160, 255));
							it.revalidate();
						}
						
						@Override
						public void mouseExited(MouseEvent e) {
							if(currentClient != Client.MAIN) {
								it.setBackground(new Color(38, 51, 51, 255));
							} else {
								it.setBackground(new Color(82, 123, 123, 255));
							}
							it.revalidate();
						}
						
						@Override
						public void mouseClicked(MouseEvent e) {
							displayClient(Client.MAIN);
						}
					});
					clientButtons.put(Client.MAIN, it);
				}});
				
				if(Main.ENABLE_TEST_CLIENT) {
					add(new JPanel() {{ // Test client button
						s(this, 96, 96);
						setBackground(new Color(38, 51, 51, 255));
						add(new JPanel() {{ // Inner
							s(this, 96, 96);
							setOpaque(false);
							add(new JPanelBG("/res/main.testclient.logo.png") {{
								setOpaque(false);
								s(this, 48, 48);
							}});
							JComponent pane;
							add(pane = new JLabel() {{
								setOpaque(false);
								setAlignmentX(JLabel.CENTER_ALIGNMENT);
								setBackground(new Color(0, 0, 0, 100));
								setForeground(new Color(176, 230, 238, 255));
								setText("<html><div style=\"text-align: center; width: 100%;\">" + Client.TEST.localizedName.replace("\n", "<br>") + "</div></html>");
								setFont(new Font("ClearSans Light", Font.PLAIN, 12));
								disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
							}});
							GAWTUtil.removeMouseListeners(pane);
						}});
						final JPanel it = this;
						addMouseListener(new MouseAdapter() {
							@Override
							public void mouseEntered(MouseEvent e) {
								it.setBackground(new Color(109, 160, 160, 255));
								it.revalidate();
							}
							
							@Override
							public void mouseExited(MouseEvent e) {
								if(currentClient != Client.TEST){ 
									it.setBackground(new Color(38, 51, 51, 255));
								} else {
									it.setBackground(new Color(82, 123, 123, 255));
								}
								it.revalidate();
							}
							
							@Override
							public void mouseClicked(MouseEvent e) {
								displayClient(Client.TEST);
							}
						});
						clientButtons.put(Client.TEST, it);
					}});
				}
				
				add(Box.createVerticalGlue());
			}});
		if(superClass.clientPanel != null) {
			superClass.mainPanel.add(superClass.clientPanel);
			superClass.frame.revalidate();
			updateButtons();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					superClass.frame.repaint();
				}
			});
			return;
		}
		superClass.mainPanel.add(superClass.clientPanel = new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
			setBackground(UIScheme.BACKGROUND);
		}});
		superClass.frame.revalidate();
		final Client toDisplay = currentClient == null ? (Main.enableOldClient ? Client.OLD : Client.MAIN) : currentClient;
		currentClient = null;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				displayClient(toDisplay);
			}
		});
	}
	//@formatter:on
	
	private void updateButtons() {
		if(clientButtons.size() > 0) {
			Iterator<Entry<Client, JPanel>> iterator = clientButtons.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<Client, JPanel> e = iterator.next();
				JPanel clientButton = e.getValue();
				if(e.getKey() == currentClient) {
					clientButton.setBackground(new Color(82, 123, 123, 255));
					clientButton.revalidate();
				} else {
					clientButton.setBackground(new Color(38, 51, 51, 255));
					clientButton.revalidate();
				}
			}
		}
	}
	
	//@formatter:off
	private void displayClient(Client client) {
		if(client == currentClient)
			return;
		synchronized(client) {
			superClass.clientPanel.removeAll();
			currentClient = client;
			updateButtons();
			superClass.clientPanel.add(new JPanel() {{
				setOpaque(false);
				setLayout(new GridBagLayout());
				final JFXPanel browserPanel = new JFXPanel();
				browserPanel.setOpaque(false);
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
			superClass.clientPanel.add(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
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
						setFont(new Font(UIScheme.LONG_TEXT_FONG, Font.PLAIN, 14));
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
						addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								pressTheBigButton();
							}
							@Override
							public void mouseEntered(MouseEvent e) {
								setBackground(UIScheme.BIG_BUTTON_ACTIVE);
							}
							@Override
							public void mouseExited(MouseEvent e) {
								setBackground(UIScheme.BIG_BUTTON);
							}
						});
					}});
				}});	
			}});
			superClass.frame.revalidate();
			Platform.runLater(new Runnable() { // We should wait while browser loads
	            @Override 
	            public void run() {
	            	currentClient.openBrowserPage(browser.getEngine());
	    			currentClient.load(superClass);
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
	
	private void openClientBrowser(final Client client, final JFXPanel panel) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				StackPane sp = new StackPane();
				browser = new WebView();
				final WebEngine engine = browser.getEngine();
				
				engine.getLoadWorker().stateProperty().addListener(
			        new ChangeListener<State>() {
						@Override
						public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
							if(newValue == State.SUCCEEDED) {
								JSObject obj = (JSObject) engine.executeScript("window");
								obj.setMember("urlHandler", new URLHandler());
								//engine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
								engine.executeScript("updateLinks();");
								browser.setVisible(true);
							} else {
								browser.setVisible(false);
							}
						}
			        });
				browser.setVisible(false);
				browser.setFontSmoothingType(FontSmoothingType.GRAY);
				sp.getChildren().add(new Text(I18n.get("browser.loading")) {{
					setFont(new javafx.scene.text.Font(UIScheme.TITLE_FONT, 36));
					setFill(UIScheme.toPaint(UIScheme.TITLE_COLOR));
				}});
				sp.getChildren().add(browser);
				final Scene sc = new Scene(sp);
				sc.setFill(null);
				sc.getStylesheets().add(LauncherMain.class.getResource("/res/scrollbar.css").toExternalForm());
				panel.setScene(sc);
				client.openBrowserPage(engine);
			}
		});
	}
	
	public void clientStatusUpdate(Client client) {
		if(client == currentClient && superClass.clientPanel != null) {
			IClientStatus clientStatus = client.getStatus();
			Status s = clientStatus.getStatus();
			clientButtonText.setText(I18n.get(s.statusActionName == null ? s.statusName : s.statusActionName).toUpperCase());
			clientStatusLine.setText("<html><div style=\"text-align: right; width: 100%;\">" + client.getStatus().getStatusTitle().replace("\n", "<br>") + "</div></html>");
			if(clientStatus.getStatusProgress() < 0 && progressBarContainer != null) {
				clientStatusPanel.remove(progressBarContainer);
				clientStatusPanel.revalidate();
				superClass.frame.repaint();
				progressBarContainer = null;
			} else if(clientStatus.getStatusProgress() >= 0) {
				if(progressBarContainer == null) {
					clientStatusPanel.add(progressBarContainer = new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.PROGRESSBAR_BG) {
						{
							Insets isc = clientStatusPanel.getBorder().getBorderInsets(clientStatusPanel);
							s(this, clientStatusPanel.getWidth() - isc.left - isc.right, 22);
							setBorder(new RoundedCornerBorder(UIScheme.BACKGROUND, UIScheme.PROGRESSBAR_BORDER, 4) {
								@Override
								public Insets getBorderInsets(Component c) {
									return new Insets(1, 2, 1, 2);
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
				s(progressBar, (int) ((progressBarContainer.getWidth() - 2) * clientStatus.getStatusProgress()), 18);
				clientStatusPanel.revalidate();
			}
			if(clientStatus.getStatus() == Status.READY && currentClient.getServers().size() > 0) {
				if(serverSelect == null) {
					serverSelectPanel.add(serverSelect = new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.MENU_BG) {
						{
							setBorder(BorderFactory.createLineBorder(UIScheme.MENU_BORDER, 1));
							s(this, 272, 24);
							serverListMenu = new GPopupMenu(true);
							serverListMenu.setIconTextGap(0);
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
							add(new JPanelBG("/res/menu.arrow.up.png") {
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
		selectedServerName.setText(currentClient.getSelectedServer().name);
		final List<Server> servers = currentClient.getServers();
		Server current = currentClient.getSelectedServer();
		if(current != lastSelectedServer || !servers.containsAll(currentServerList) || !currentServerList.containsAll(servers)) {
			lastSelectedServer = current;
			serverListMenu.removeAll();
			for(int i = 0; i < servers.size(); ++i) {
				JMenuItem item = serverListMenu.addItem(servers.get(i).name, servers.get(i) == current ? "/res/menu.check.png" : "/res/menu.empty.png");
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
	
	static void s(Component c, int width, int height) {
		c.setSize(width, height);
		Dimension d = new Dimension(width, height);
		c.setPreferredSize(d);
		c.setMaximumSize(d);
		c.setMinimumSize(d);
	}
}
