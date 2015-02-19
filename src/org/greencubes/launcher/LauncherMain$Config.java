package org.greencubes.launcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.greencubes.client.Client;
import org.greencubes.client.IClientStatus.Status;
import org.greencubes.launcher.LauncherOptions.OnStartAction;
import org.greencubes.main.Main;
import org.greencubes.swing.AbstractMouseListener;
import org.greencubes.swing.GAWTUtil;
import org.greencubes.swing.GJBoxPanel;
import org.greencubes.swing.GPopupMenu;
import org.greencubes.swing.JPanelBG;
import org.greencubes.swing.RoundedCornerBorder;
import org.greencubes.swing.UIScheme;
import org.greencubes.util.I18n;
import org.greencubes.yaml.YamlException;
import org.greencubes.yaml.YamlFile;
import org.json.JSONException;

public class LauncherMain$Config {
	
	private final LauncherMain superClass;
	
	private JPanel configPage;
	private int configPageId = -1;
	private JLabel launcherConfig;
	private JLabel clientConfig;
	private String newLanguage = I18n.currentLanguage;
	private OnStartAction newOnStartAction = LauncherOptions.onClientStart;
	private ScreenSize newScreenSize ;
	private List<ScreenSize> screenSizes = new ArrayList<ScreenSize>();
	private YamlFile newClientConfig;
	private List<String> supportedClientLanguages = new ArrayList<String>();
	private String textureQuality = Main.IS_64_BIT_JAVA ? "high" : "medium";
	
	LauncherMain$Config(LauncherMain superClass) {
		this.superClass = superClass;
	}
	
	protected void displayConfig() {
		if(superClass.configPanel != null)
			return;
		configPageId = -1;
		if(superClass.clientPanel != null) {
			superClass.clientPanel.getParent().remove(superClass.clientPanel);
			superClass.clientPanel = null;
		}
		superClass.topGame.setForeground(UIScheme.TITLE_COLOR);
		superClass.mainPanel.add(superClass.configPanel = new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
			setBackground(UIScheme.BACKGROUND);
			add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
				add(new JLabel("НАСТРОЙКИ".toUpperCase()) {{
					setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
					setForeground(UIScheme.TEXT_COLOR);
					setFont(new Font(UIScheme.TITLE_FONT, Font.PLAIN, 24));
				}});
				add(Box.createHorizontalGlue());
			}});
			add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
				add(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
					setMaximumSize(new Dimension(200, Short.MAX_VALUE));
					add(launcherConfig = new JLabel("Настройки лаунчера") {{
						setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
						setFont(new Font(UIScheme.TITLE_FONT, Font.PLAIN, 16));
						setForeground(UIScheme.TITLE_COLOR);
					}});
					add(clientConfig = new JLabel("Настройки клиента") {{
						setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
						setFont(new Font(UIScheme.TITLE_FONT, Font.PLAIN, 16));
						setForeground(UIScheme.TITLE_COLOR);
					}});
					launcherConfig.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {
							displayConfigPage(0);
						}
						@Override
					    public void mouseEntered(MouseEvent e) {
							if(configPageId != 0) {
								launcherConfig.setForeground(UIScheme.TITLE_COLOR_SEL);
							}
						}
						@Override
					    public void mouseExited(MouseEvent e) {
							if(configPageId != 0) {
								launcherConfig.setForeground(UIScheme.TITLE_COLOR);
							}
						}
					});
					clientConfig.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {
							displayConfigPage(1);
						}
						@Override
					    public void mouseEntered(MouseEvent e) {
							if(configPageId != 1) {
								clientConfig.setForeground(UIScheme.TITLE_COLOR_SEL);
							}
						}
						@Override
					    public void mouseExited(MouseEvent e) {
							if(configPageId != 1) {
								clientConfig.setForeground(UIScheme.TITLE_COLOR);
							}
						}
					});
					add(Box.createVerticalGlue());
				}});
				add(new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.EMPTY) {{
					setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5), BorderFactory.createMatteBorder(0, 1, 0, 0, UIScheme.TITLE_COLOR)));
					add(Box.createVerticalGlue());
				}});
				add(new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.BACKGROUND) {{
					setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
					add(configPage = new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
						
					}});
					add(new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.BACKGROUND) {{
						setMaximumSize(new Dimension(Short.MAX_VALUE, 52));
						setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
						add(Box.createHorizontalGlue());
						add(new JPanel() {{
							s(this, 200, 36);
							setBorder(new RoundedCornerBorder(UIScheme.BACKGROUND, null, 4));
							setBackground(UIScheme.BIG_BUTTON);
							setLayout(new GridBagLayout());
							add(new JLabel() {{
								setOpaque(false);
								setAlignmentX(JLabel.CENTER_ALIGNMENT);
								setForeground(UIScheme.TEXT_COLOR);
								setText("Сохранить".toUpperCase());
								setFont(new Font(UIScheme.TITLE_FONT, Font.BOLD, 24));
							}}, new GridBagConstraints() {{
								weightx = 1;
								weighty = 1;
							}});
							addMouseListener(new AbstractMouseListener() {
								@Override
								public void mousePressed(MouseEvent e) {
									boolean alert = !I18n.currentLanguage.equals(newLanguage);
									try {
										Main.getConfig().put("lang", newLanguage);
										Main.getConfig().put("onstart", newOnStartAction.ordinal());
										LauncherOptions.onClientStart = newOnStartAction;
									} catch(JSONException e1) {
										// Is it even possible?
									}
									try {
										newClientConfig.save(new FileOutputStream(new File(Client.MAIN.getWorkingDirectory(), "config.yml")));
									} catch(Throwable t) {}
									if(alert) {
										GAWTUtil.showDialog(I18n.get("settings.launcher.restart.title"), I18n.get("settings.launcher.restart"), new String[] {
											//I18n.get("settings.launcher.restart.do"), // TODO : Add support of restarting
											I18n.get("settings.launcher.restart.skip"),
										}, JOptionPane.QUESTION_MESSAGE, 300);
									}
									superClass.play.displayPlayPanel();
									
								}
							});
						}});
					}});
				}});
			}});
		}});
		displayConfigPage(0);
	}

	private void displayConfigPage(int pageId) {
		if(configPageId == pageId)
			return;
		configPageId = pageId;
		switch(pageId) {
			case 0:
				launcherConfig.setForeground(UIScheme.TITLE_COLOR_SEL);
				clientConfig.setForeground(UIScheme.TITLE_COLOR);
				configPage.removeAll();
				configPage.add(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
					JScrollPane scrollPane;
					add(scrollPane = new JScrollPane(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
						add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
							add(new JLabel(I18n.get("settings.launcher.language")) {{
								setForeground(UIScheme.TEXT_COLOR);
								setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
							}});
							add(Box.createHorizontalGlue());
						}});
						add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
							setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.MENU_BG) {
								{
									final JLabel selectedLanguage;
									setBorder(BorderFactory.createLineBorder(UIScheme.MENU_BORDER, 1));
									s(this, 272, 24);
									final GPopupMenu langSelect = new GPopupMenu(false);
									langSelect.setIconTextGap(0);
									langSelect.setMenuColors(UIScheme.MENU_DD_BG, UIScheme.TITLE_COLOR, UIScheme.MENU_DD_BG_SEL, UIScheme.TITLE_COLOR_SEL);
									addMouseListener(new MouseAdapter() {
										@Override
										public void mousePressed(MouseEvent e) {
											if(e.isConsumed())
												return;
											langSelect.show(e.getComponent(), false);
										}
									});
									langSelect.setBorder(BorderFactory.createEmptyBorder());
									langSelect.setOpaque(false);
									langSelect.setMenuSize(new Dimension(272, 24));
									langSelect.setMenuFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
									add(new JPanel() {
										{
											s(this, 20, 0);
											setBackground(UIScheme.EMPTY);
										}
									});
									add(selectedLanguage = new JLabel(I18n.get("lang." + newLanguage)) {
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
									final ImageIcon empty = new ImageIcon(GPopupMenu.class.getResource("/res/menu.empty.png"));
									final ImageIcon checked = new ImageIcon(GPopupMenu.class.getResource("/res/menu.check.png"));
									for(final String lang : I18n.supportedLanguages) {
										final JMenuItem item = langSelect.addItem(I18n.get("lang." + lang), lang.equals(newLanguage) ? "/res/menu.check.png" : "/res/menu.empty.png");
										item.addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent e) {
												selectedLanguage.setText(I18n.get("lang." + lang));
												for(JMenuItem item1 : langSelect.getItems()) {
													if(item1 != item)
														item1.setIcon(empty);
												}
												item.setIcon(checked);
												newLanguage = lang;
											}
										});
									}
								}
							});
							
							add(Box.createHorizontalGlue());
						}});
						add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
							add(new JLabel(I18n.get("settings.launcher.onstart")) {{
								setForeground(UIScheme.TEXT_COLOR);
								setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
							}});
							add(Box.createHorizontalGlue());
						}});
						add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
							setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.MENU_BG) {
								{
									final JLabel selectedOnStart;;
									setBorder(BorderFactory.createLineBorder(UIScheme.MENU_BORDER, 1));
									s(this, 272, 24);
									final GPopupMenu onStartSelect = new GPopupMenu(false);
									onStartSelect.setIconTextGap(0);
									onStartSelect.setMenuColors(UIScheme.MENU_DD_BG, UIScheme.TITLE_COLOR, UIScheme.MENU_DD_BG_SEL, UIScheme.TITLE_COLOR_SEL);
									addMouseListener(new MouseAdapter() {
										@Override
										public void mousePressed(MouseEvent e) {
											if(e.isConsumed())
												return;
											onStartSelect.show(e.getComponent(), false);
										}
									});
									onStartSelect.setBorder(BorderFactory.createEmptyBorder());
									onStartSelect.setOpaque(false);
									onStartSelect.setMenuSize(new Dimension(272, 24));
									onStartSelect.setMenuFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
									add(new JPanel() {
										{
											s(this, 20, 0);
											setBackground(UIScheme.EMPTY);
										}
									});
									add(selectedOnStart = new JLabel(I18n.get(newOnStartAction.langKey)) {
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
									final ImageIcon empty = new ImageIcon(GPopupMenu.class.getResource("/res/menu.empty.png"));
									final ImageIcon checked = new ImageIcon(GPopupMenu.class.getResource("/res/menu.check.png"));
									for(final OnStartAction action : OnStartAction.values()) {
										final JMenuItem item = onStartSelect.addItem(I18n.get(action.langKey), newOnStartAction == action ? "/res/menu.check.png" : "/res/menu.empty.png");
										item.addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent e) {
												selectedOnStart.setText(I18n.get(action.langKey));
												for(JMenuItem item1 : onStartSelect.getItems()) {
													if(item1 != item)
														item1.setIcon(empty);
												}
												item.setIcon(checked);
												newOnStartAction = action;
											}
										});
									}
								}
							});
							
							add(Box.createHorizontalGlue());
						}});
					}}, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {{
						setOpaque(false);
						setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
					}});
					scrollPane.getViewport().setOpaque(false);
					scrollPane.getVerticalScrollBar().setUI(GAWTUtil.customScrollBarUI(UIScheme.BACKGROUND, UIScheme.TOP_PANEL_BG));
					scrollPane.getVerticalScrollBar().setOpaque(false);
				}});
				break;
			case 1:
				clientConfig.setForeground(UIScheme.TITLE_COLOR_SEL);
				launcherConfig.setForeground(UIScheme.TITLE_COLOR);
				configPage.removeAll();
				if(Client.MAIN.getStatus().getStatus() != Status.READY && Client.MAIN.getStatus().getStatus() != Status.OFFLINE) {
					configPage.add(new JTextPane() {{
						setText(I18n.get("settings.client.needupdate"));
						setForeground(UIScheme.TEXT_COLOR);
						setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
						setEditable(false);
						setOpaque(false);
						setHighlighter(null);
					}});
				} else {
					final List<String> textureQualities = new ArrayList<String>();
					textureQualities.add("high");
					textureQualities.add("medium");
					textureQualities.add("low");
					if(newClientConfig == null) {
						File wd = Client.MAIN.getWorkingDirectory();
						newClientConfig = new YamlFile();
						try {
							newClientConfig.load(new FileInputStream(new File(wd, "config.yml")));
						} catch(FileNotFoundException e) {
						} catch(YamlException e) {
							e.printStackTrace();
						}
						YamlFile langs = new YamlFile();
						try {
							langs.load(new FileInputStream(new File(wd, "resources/lang/languages.yml")));
						} catch(FileNotFoundException e) {
						} catch(YamlException e) {
							e.printStackTrace();
						}
						supportedClientLanguages.addAll(langs.getKeys(null));
						GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
						GraphicsDevice display = ge.getDefaultScreenDevice();
						DisplayMode[] availableModes = display.getDisplayModes();
						      
						for(DisplayMode mode : availableModes) {
							ScreenSize ss = new ScreenSize(mode.getWidth(), mode.getHeight());
							if(ss.width > 1000 && !screenSizes.contains(ss))
								screenSizes.add(ss);
							if(display.getDisplayMode().equals(mode)) {
								newScreenSize = ss;
							}
						}
						if(newScreenSize == null) {
							newScreenSize = new ScreenSize(1, 1);
						}
						List<Integer> resolution = newClientConfig.getIntList("graphics.resolution", new ArrayList<Integer>(2));
						if(resolution.size() == 2 && resolution.get(0) > 0 && resolution.get(1) > 0) {
							newScreenSize = new ScreenSize(resolution.get(0), resolution.get(1));
							if(!screenSizes.contains(newScreenSize))
								screenSizes.add(newScreenSize);
						}
						Collections.sort(screenSizes, new Comparator<ScreenSize>() {
							@Override
							public int compare(ScreenSize o1, ScreenSize o2) {
								int i = Integer.compare(o2.width, o1.width);
								if(i == 0)
									return Integer.compare(o2.height, o1.height);
								return i;
							}
						});
						textureQuality = newClientConfig.getString("graphics.items-quality", textureQuality);
					}
					configPage.add(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
						JScrollPane scrollPane;
						add(scrollPane = new JScrollPane(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
							final ImageIcon iconUnchecked = new ImageIcon(JPanelBG.class.getResource("/res/checkbox.png"));
							final ImageIcon iconChecked = new ImageIcon(JPanelBG.class.getResource("/res/checkbox.checked.png"));
							
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
								add(new JLabel(I18n.get("settings.client.generic")) {{
									setForeground(UIScheme.TEXT_COLOR);
									setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 20));
								}});
								add(Box.createHorizontalGlue());
							}});
							
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
								add(new JLabel(I18n.get("settings.client.language")) {{
									setForeground(UIScheme.TEXT_COLOR);
									setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
								}});
								add(Box.createHorizontalGlue());
							}});
							
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
								setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));
								add(new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.MENU_BG) {
									{
										final JLabel selectedLang;
										setBorder(BorderFactory.createLineBorder(UIScheme.MENU_BORDER, 1));
										s(this, 272, 24);
										final GPopupMenu langSelect = new GPopupMenu(false);
										langSelect.setIconTextGap(0);
										langSelect.setMenuColors(UIScheme.MENU_DD_BG, UIScheme.TITLE_COLOR, UIScheme.MENU_DD_BG_SEL, UIScheme.TITLE_COLOR_SEL);
										addMouseListener(new MouseAdapter() {
											@Override
											public void mousePressed(MouseEvent e) {
												if(e.isConsumed())
													return;
												langSelect.show(e.getComponent(), false);
											}
										});
										langSelect.setBorder(BorderFactory.createEmptyBorder());
										langSelect.setOpaque(false);
										langSelect.setMenuSize(new Dimension(272, 24));
										langSelect.setMenuFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
										add(new JPanel() {
											{
												s(this, 20, 0);
												setBackground(UIScheme.EMPTY);
											}
										});
										String currenLang = newClientConfig.getString("lang");
										if(currenLang == null) {
											if(supportedClientLanguages.contains(I18n.currentLanguage)) {
												// Set language from launcher
												currenLang = I18n.currentLanguage;
												newClientConfig.setProperty("lang", currenLang);
											}
											currenLang = "unselected";
										}
										add(selectedLang = new JLabel(I18n.get("lang." + currenLang)) {
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
										final ImageIcon empty = new ImageIcon(GPopupMenu.class.getResource("/res/menu.empty.png"));
										final ImageIcon checked = new ImageIcon(GPopupMenu.class.getResource("/res/menu.check.png"));
										for(final String lang : supportedClientLanguages) {
											final JMenuItem item = langSelect.addItem(I18n.get("lang." + lang), lang.equals(currenLang) ? "/res/menu.check.png" : "/res/menu.empty.png");
											item.addActionListener(new ActionListener() {
												@Override
												public void actionPerformed(ActionEvent e) {
													selectedLang.setText(I18n.get("lang." + lang));
													for(JMenuItem item1 : langSelect.getItems()) {
														if(item1 != item)
															item1.setIcon(empty);
													}
													item.setIcon(checked);
													newClientConfig.setProperty("lang", lang);
												}
											});
										}
									}
								});
								
								add(Box.createHorizontalGlue());
							}});
							
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
								add(new JLabel(I18n.get("settings.client.graphics")) {{
									setForeground(UIScheme.TEXT_COLOR);
									setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 20));
								}});
								add(Box.createHorizontalGlue());
							}});
							
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
								add(new JLabel(I18n.get("settings.client.resolution")) {{
									setForeground(UIScheme.TEXT_COLOR);
									setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
								}});
								add(Box.createHorizontalGlue());
							}});
							
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
								setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));
								add(new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.MENU_BG) {
									{
										final JLabel selectedScreenSize;
										setBorder(BorderFactory.createLineBorder(UIScheme.MENU_BORDER, 1));
										s(this, 272, 24);
										final GPopupMenu screenSizeSelect = new GPopupMenu(false);
										screenSizeSelect.setIconTextGap(0);
										screenSizeSelect.setMenuColors(UIScheme.MENU_DD_BG, UIScheme.TITLE_COLOR, UIScheme.MENU_DD_BG_SEL, UIScheme.TITLE_COLOR_SEL);
										addMouseListener(new MouseAdapter() {
											@Override
											public void mousePressed(MouseEvent e) {
												if(e.isConsumed())
													return;
												screenSizeSelect.show(e.getComponent(), false);
											}
										});
										screenSizeSelect.setBorder(BorderFactory.createEmptyBorder());
										screenSizeSelect.setOpaque(false);
										screenSizeSelect.setMenuSize(new Dimension(272, 24));
										screenSizeSelect.setMenuFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
										add(new JPanel() {
											{
												s(this, 20, 0);
												setBackground(UIScheme.EMPTY);
											}
										});
										add(selectedScreenSize = new JLabel(newScreenSize.name) {
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
										final ImageIcon empty = new ImageIcon(GPopupMenu.class.getResource("/res/menu.empty.png"));
										final ImageIcon checked = new ImageIcon(GPopupMenu.class.getResource("/res/menu.check.png"));
										for(final ScreenSize ss : screenSizes) {
											final JMenuItem item = screenSizeSelect.addItem(ss.name, newScreenSize.equals(ss) ? "/res/menu.check.png" : "/res/menu.empty.png");
											item.addActionListener(new ActionListener() {
												@Override
												public void actionPerformed(ActionEvent e) {
													selectedScreenSize.setText(ss.name);
													for(JMenuItem item1 : screenSizeSelect.getItems()) {
														if(item1 != item)
															item1.setIcon(empty);
													}
													item.setIcon(checked);
													newScreenSize = ss;
													newClientConfig.setProperty("graphics.resolution", new ArrayList<Integer>() {{
														add(newScreenSize.width);
														add(newScreenSize.height);
													}});
												}
											});
										}
									}
								});
								
								add(Box.createHorizontalGlue());
							}});
							
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
								setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));
								add(new JCheckBox(I18n.get("settings.client.fullscreen"), newClientConfig.getBoolean("graphics.fullscreen", true) ?  iconChecked : iconUnchecked) {{
									setOpaque(false);
									setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
									setForeground(UIScheme.TITLE_COLOR);
									addItemListener(new ItemListener() {
										@Override
										public void itemStateChanged(ItemEvent e) {
											if(isSelected())
												setIcon(iconChecked);
											else
												setIcon(iconUnchecked);
											newClientConfig.setProperty("graphics.fullscreen", isSelected());
										}
									});
									addMouseListener(new MouseAdapter() {
										@Override
										public void mouseEntered(MouseEvent e) {
											setForeground(UIScheme.TITLE_COLOR_SEL);
										}
										@Override
									    public void mouseExited(MouseEvent e) {
											setForeground(UIScheme.TITLE_COLOR);
									    }
									});
									setMargin(new Insets(2, 0, 2, 0));
								}});
								add(Box.createHorizontalGlue());
							}});
							
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
								add(new JLabel(I18n.get("settings.client.textures")) {{
									setForeground(UIScheme.TEXT_COLOR);
									setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
								}});
								add(Box.createHorizontalGlue());
							}});
							
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
								setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));
								add(new GJBoxPanel(BoxLayout.LINE_AXIS, UIScheme.MENU_BG) {
									{
										final JLabel selectedQuality;
										setBorder(BorderFactory.createLineBorder(UIScheme.MENU_BORDER, 1));
										s(this, 272, 24);
										final GPopupMenu qualitySelect = new GPopupMenu(false);
										qualitySelect.setIconTextGap(0);
										qualitySelect.setMenuColors(UIScheme.MENU_DD_BG, UIScheme.TITLE_COLOR, UIScheme.MENU_DD_BG_SEL, UIScheme.TITLE_COLOR_SEL);
										addMouseListener(new MouseAdapter() {
											@Override
											public void mousePressed(MouseEvent e) {
												if(e.isConsumed())
													return;
												qualitySelect.show(e.getComponent(), false);
											}
										});
										qualitySelect.setBorder(BorderFactory.createEmptyBorder());
										qualitySelect.setOpaque(false);
										qualitySelect.setMenuSize(new Dimension(272, 24));
										qualitySelect.setMenuFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
										add(new JPanel() {
											{
												s(this, 20, 0);
												setBackground(UIScheme.EMPTY);
											}
										});
										add(selectedQuality = new JLabel(I18n.get("quality." + textureQuality)) {
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
										final ImageIcon empty = new ImageIcon(GPopupMenu.class.getResource("/res/menu.empty.png"));
										final ImageIcon checked = new ImageIcon(GPopupMenu.class.getResource("/res/menu.check.png"));
										for(final String q : textureQualities) {
											final JMenuItem item = qualitySelect.addItem(I18n.get("quality." + q), q.equals(textureQuality) ? "/res/menu.check.png" : "/res/menu.empty.png");
											item.addActionListener(new ActionListener() {
												@Override
												public void actionPerformed(ActionEvent e) {
													selectedQuality.setText(I18n.get("quality." + q));
													for(JMenuItem item1 : qualitySelect.getItems()) {
														if(item1 != item)
															item1.setIcon(empty);
													}
													item.setIcon(checked);
													textureQuality = q;
													newClientConfig.setProperty("graphics.items-quality", q);
												}
											});
										}
									}
								});
								
								add(Box.createHorizontalGlue());
							}});
							
							add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
								setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));
								add(new JCheckBox(I18n.get("settings.client.shadows"), newClientConfig.getBoolean("graphics.shadows", false) ?  iconChecked : iconUnchecked) {{
									setOpaque(false);
									setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
									setForeground(UIScheme.TITLE_COLOR);
									addItemListener(new ItemListener() {
										@Override
										public void itemStateChanged(ItemEvent e) {
											if(isSelected())
												setIcon(iconChecked);
											else
												setIcon(iconUnchecked);
											newClientConfig.setProperty("graphics.shadows", isSelected());
										}
									});
									addMouseListener(new MouseAdapter() {
										@Override
										public void mouseEntered(MouseEvent e) {
											setForeground(UIScheme.TITLE_COLOR_SEL);
										}
										@Override
									    public void mouseExited(MouseEvent e) {
											setForeground(UIScheme.TITLE_COLOR);
									    }
									});
									setMargin(new Insets(2, 0, 2, 0));
								}});
								add(Box.createHorizontalGlue());
							}});
						}}, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {{
							setOpaque(false);
							setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
						}});
						scrollPane.getViewport().setOpaque(false);
						scrollPane.getVerticalScrollBar().setUI(GAWTUtil.customScrollBarUI(UIScheme.BACKGROUND, UIScheme.TOP_PANEL_BG));
						scrollPane.getVerticalScrollBar().setOpaque(false);
						if(!Main.IS_64_BIT_JAVA)
							add(new JTextPane() {{
								setText(I18n.get("32alert"));
								setForeground(new Color(255, 0, 0, 255));
								setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 16));
								setEditable(false);
								setOpaque(false);
								setHighlighter(null);
							}});
					}});
				}
				break;
		}
		superClass.frame.pack();
		superClass.frame.revalidate();
		superClass.frame.repaint();
	}
	
	static void s(Component c, int width, int height) {
		c.setSize(width, height);
		Dimension d = new Dimension(width, height);
		c.setPreferredSize(d);
		c.setMaximumSize(d);
		c.setMinimumSize(d);
	}
	
	private static class ScreenSize {
		
		final int width;
		final int height;
		final String name;
		
		public ScreenSize(int width, int height) {
			this.width = width;
			this.height = height;
			this.name = width + "x" + height;
		}
		
		@Override
		public int hashCode() {
			return width * 31 + height;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj.getClass() != getClass())
				return false;
			ScreenSize ss = (ScreenSize) obj;
			return ss.width == width && ss.height == height;
		}
	}
}
