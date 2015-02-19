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
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.greencubes.client.Client;
import org.greencubes.client.IClientStatus;
import org.greencubes.client.Server;
import org.greencubes.client.IClientStatus.Status;
import org.greencubes.swing.AbstractMouseListener;
import org.greencubes.swing.GAWTUtil;
import org.greencubes.swing.GJBoxPanel;
import org.greencubes.swing.GPopupMenu;
import org.greencubes.swing.JPanelBG;
import org.greencubes.swing.RoundedCornerBorder;
import org.greencubes.swing.UIScheme;
import org.greencubes.util.I18n;
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
	
	Client currentClient;
	
	LauncherMain$Play(LauncherMain superClass) {
		this.superClass = superClass;
	}
	
	//@formatter:off
	void displayPlayPanel() {
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
		if(superClass.clientPanel != null)
			return;
		superClass.topGame.setForeground(UIScheme.TITLE_COLOR_SEL);
		if(superClass.configPanel != null) {
			superClass.configPanel.getParent().remove(superClass.configPanel);
			superClass.configPanel = null;
		}
		superClass.mainPanel.add(superClass.clientPanel = new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
			setBackground(UIScheme.BACKGROUND);
		}});
		superClass.frame.revalidate();
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
			superClass.clientPanel.add(new JPanel() {{
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
				browser = new WebView();
				WebEngine engine = browser.getEngine();
				Scene sc = new Scene(browser);
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
