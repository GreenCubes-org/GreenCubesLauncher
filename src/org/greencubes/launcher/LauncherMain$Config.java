package org.greencubes.launcher;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
import org.json.JSONException;

public class LauncherMain$Config {
	
	private final LauncherMain superClass;
	
	private JPanel configPage;
	private int configPageId = -1;
	private JLabel launcherConfig;
	private JLabel clientConfig;
	private String newLanguage = I18n.currentLanguage;
	private OnStartAction newOnStartAction = LauncherOptions.onClientStart;
	
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
									boolean alert = I18n.currentLanguage != newLanguage;
									try {
										Main.getConfig().put("lang", newLanguage);
										Main.getConfig().put("onstart", newOnStartAction.ordinal());
										LauncherOptions.onClientStart = newOnStartAction;
									} catch(JSONException e1) {
										// Is it even possible?
									}
									if(alert) {
										// TODO: Display saving alert
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
							add(new JLabel("Язык лаунчера:") {{
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
							add(new JLabel("При запуске игры:") {{
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
				configPage.add(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
					JScrollPane scrollPane;
					add(scrollPane = new JScrollPane(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
						add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
							add(new JLabel("Язык лаунчера:") {{
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
							add(new JLabel("При запуске игры:") {{
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
	
}
