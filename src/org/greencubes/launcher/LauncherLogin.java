package org.greencubes.launcher;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.greencubes.main.Main;
import org.greencubes.swing.AbstractMouseListener;
import org.greencubes.swing.AbstractWindowListener;
import org.greencubes.swing.GAWTUtil;
import org.greencubes.swing.GJBoxPanel;
import org.greencubes.swing.JPanelBG;
import org.greencubes.swing.PlaceholderPasswordField;
import org.greencubes.swing.PlaceholderTextField;
import org.greencubes.swing.RoundedCornerBorder;
import org.greencubes.swing.UIScheme;
import org.greencubes.swing.UndecoratedJFrame;
import org.greencubes.util.I18n;

public class LauncherLogin {
	
	private JFrame frame;
	private JPanel centerPanel;
	private JTextField userField;
	private JPasswordField passwordField;
	private JCheckBox autoLoginCheckBox;
	private JTextPane errorPane;
	private boolean isLoginDisplayed = false;
	
	//@formatter:off
	/**
	 * Should not be invoked in AWT thread
	 */
	public LauncherLogin(JFrame previousFrame) {
		frame = new UndecoratedJFrame(I18n.get("title"));
		Main.currentFrame = frame;
		frame.setIconImages(LauncherOptions.getIcons());
		frame.setUndecorated(true);
		frame.setResizable(false);
		frame.add(new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.BACKGROUND) {{
			setPreferredSize(new Dimension(400, 300));
			// Top line
			add(new JPanelBG("/res/login.top.png") {{ // Window buttons
				setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
				s(this, 400, 32);
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
			add(Box.createVerticalStrut(20));
			add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
				max(this, null, 26);
				add(Box.createHorizontalGlue());
				add(new JLabel(I18n.get("login.title").toUpperCase()) {{
					setForeground(UIScheme.TEXT_COLOR);
					setFont(new Font(UIScheme.TITLE_FONT, Font.PLAIN, 24));
				}});
				add(Box.createHorizontalGlue());
			}});
			add(Box.createVerticalStrut(15));
			// Center panel
			add(centerPanel = new GJBoxPanel(BoxLayout.PAGE_AXIS, null));
			add(Box.createVerticalGlue());
		}}, BorderLayout.CENTER);
		frame.addWindowListener(new AbstractWindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.dispose();
				Main.close();
			}
		});
		frame.pack();
		frame.setLocationRelativeTo(null);
		if(previousFrame != null)
			previousFrame.dispose();
		frame.setVisible(true);
		//displayLogin();
		if(LauncherOptions.autoLogin) {
			LauncherOptions.loadSession();
			if(LauncherOptions.sessionUserId > 0) {
				displayProgress(I18n.get("login.authorization"));
				try {
					LauncherOptions.authSession();
					LauncherOptions.userInfo = LauncherUtil.sessionRequest("action=info");
					if(Main.TEST)
						System.out.println(LauncherOptions.userInfo);
					displayProgress(I18n.get("login.loading"));
					launcherMain();
					return;
				} catch(IOException e) {
					if(Main.TEST)
						e.printStackTrace();
				} catch(Exception e) {
					if(Main.TEST)
						e.printStackTrace();
				}
			}
		}
		displayLogin();
	}
	//@formatter:on
	
	/**
	 * Should not be invoked in AWT thread
	 */
	private void launcherMain() {
		new LauncherMain(frame); // Send current frame so next window can destroy it when ready
	}
	
	private void joinOffline() {
		if(userField != null) {
			if(userField.getText().length() > 0) {
				// joinOffline() runs in window thread, so we need to start new thread to allow progress update
				new Thread() {
					@Override
					public void run() {
						displayProgress(I18n.get("login.loading"));
						if(LauncherOptions.sessionUser != null && !LauncherOptions.sessionUser.equals(userField.getText()))
							LauncherOptions.logOff();
						LauncherOptions.sessionUser = userField.getText();
						launcherMain();
					}
				}.start();				
			} else {
				GAWTUtil.showDialog(I18n.get("login.error.title"), I18n.get("login.nousername"), new Object[] {"OK"}, JOptionPane.ERROR_MESSAGE, 300);
				displayLogin();
			}
		}
	}
	
	private void doLogin() {
		if(userField != null && passwordField != null) {
			if(userField.getText().length() > 0 && passwordField.getPassword().length > 0) {
				// goLogin() runs in window thread, so we need to start new thread to allow progress update
				new Thread() {
					@Override
					public void run() {
						displayProgress(I18n.get("login.authorization"));
						try {
							LauncherOptions.auth(userField.getText(), passwordField.getPassword());
							LauncherOptions.userInfo = LauncherUtil.sessionRequest("action=info");
							if(Main.TEST)
								System.out.println(LauncherOptions.userInfo);
							displayProgress(I18n.get("login.loading"));
							launcherMain();
							return;
						} catch(IOException e) {
							int select = GAWTUtil.showDialog(I18n.get("login.error.title"), I18n.get("login.exception", e.getLocalizedMessage()), new Object[] {I18n.get("login.error.repeat"), I18n.get("login.error.offline")}, JOptionPane.ERROR_MESSAGE, 300);
							if(select == 1) {
								joinOffline();
								return;
							}
						} catch(AuthError e) {
							GAWTUtil.showDialog(I18n.get("login.error.title"), I18n.get("login.exception", I18n.get("login.error." + e.errorCode)), new Object[] {"OK"}, JOptionPane.ERROR_MESSAGE, 300);
						}
						displayLogin();
					}
				}.start();
			} else if(userField.getText().length() == 0 && passwordField.getPassword().length == 0) {
				GAWTUtil.showDialog(I18n.get("login.error.title"), I18n.get("login.nousernamepassword"), new Object[] {"OK"}, JOptionPane.ERROR_MESSAGE, 300);
				if(errorPane != null)
					errorPane.setText(I18n.get("login.nousernamepassword"));
			} else if(userField.getText().length() == 0) {
				GAWTUtil.showDialog(I18n.get("login.error.title"), I18n.get("login.nousername"), new Object[] {"OK"}, JOptionPane.ERROR_MESSAGE, 300);
			} else if(passwordField.getPassword().length == 0) {
				GAWTUtil.showDialog(I18n.get("login.error.title"), I18n.get("login.nopassword"), new Object[] {"OK"}, JOptionPane.ERROR_MESSAGE, 300);
			}
		}
	}
	
	//@formatter:off
	private void displayProgress(final String progressString) {
		isLoginDisplayed = false;
		centerPanel.removeAll();
		centerPanel.invalidate();
		centerPanel.add(new JPanel() {{
			setOpaque(false);
			setLayout(new GridBagLayout());
			add(new JLabel() {{
				setOpaque(false);
				setAlignmentX(JLabel.CENTER_ALIGNMENT);
				setForeground(UIScheme.TITLE_COLOR);
				setText(progressString);
				setFont(new Font(UIScheme.TITLE_FONT, Font.BOLD, 24));
			}}, new GridBagConstraints() {{
				weighty = 1.0d;
			}});
		}});
		centerPanel.revalidate();
		frame.repaint();
	}
	//@formatter:on
	
	//@formatter:off
	private void displayLogin() {
		if(isLoginDisplayed)
			return;
		isLoginDisplayed = true;
		centerPanel.removeAll();
		final String savedUser = userField != null ? userField.getText() : LauncherOptions.sessionUser;
		centerPanel.add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
			max(this, null, 24);
			add(Box.createHorizontalGlue());
			add(userField = new PlaceholderTextField() {{
				s(this, 200, 24);
				setForeground(UIScheme.TEXT_COLOR);
				setBackground(UIScheme.INPUT_BG);
				setCaretColor(UIScheme.TEXT_COLOR);
				setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 18));
				setBorder(new RoundedCornerBorder(UIScheme.BACKGROUND, UIScheme.INPUT_BORDER, 4));
				if(savedUser != null)
					setText(savedUser);
				setDisabledTextColor(UIScheme.TEXT_COLOR_DISABLED);
				setPlaceholder(I18n.get("login.login"));
			}});
			add(Box.createHorizontalGlue());
		}});
		
		centerPanel.add(Box.createVerticalStrut(12));
		
		centerPanel.add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
			max(this, null, 24);
			add(Box.createHorizontalGlue());
			add(passwordField = new PlaceholderPasswordField() {{
				s(this, 200, 24);
				setForeground(UIScheme.TEXT_COLOR);
				setBackground(UIScheme.INPUT_BG);
				setCaretColor(UIScheme.TEXT_COLOR);
				setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 18));
				setBorder(new RoundedCornerBorder(UIScheme.BACKGROUND, UIScheme.INPUT_BORDER, 4));
				setDisabledTextColor(UIScheme.TEXT_COLOR_DISABLED);
				setDisabledTextColor(UIScheme.TEXT_COLOR_DISABLED);
				setPlaceholder(I18n.get("login.password"));
			}});
			add(Box.createHorizontalGlue());
		}});
		
		centerPanel.add(Box.createVerticalStrut(4));
		
		final ImageIcon iconUnchecked = new ImageIcon(JPanelBG.class.getResource("/res/checkbox.png"));
		final ImageIcon iconChecked = new ImageIcon(JPanelBG.class.getResource("/res/checkbox.checked.png"));
		
		centerPanel.add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
			max(this, null, 20);
			add(Box.createHorizontalGlue());
			add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
				boolean checked = LauncherOptions.autoLogin;
				add(autoLoginCheckBox = new JCheckBox(I18n.get("login.autologin"), checked ? iconChecked : iconUnchecked, checked) {{
					setOpaque(false);
					setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 18));
					setForeground(UIScheme.TITLE_COLOR);
					addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							if(autoLoginCheckBox.isSelected())
								setIcon(iconChecked);
							else
								setIcon(iconUnchecked);
						}
					});
					setMargin(new Insets(2, 0, 2, 0));
					setToolTipText(I18n.get("login.autologin.tip"));
				}});
			}});
			add(Box.createHorizontalGlue());
		}});
		
		centerPanel.add(Box.createVerticalStrut(4));
		
		centerPanel.add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
			max(this, null, 36);
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
					setText(I18n.get("login.dologin").toUpperCase());
					setFont(new Font(UIScheme.TITLE_FONT, Font.BOLD, 24));
				}}, new GridBagConstraints() {{
					weightx = 1;
					weighty = 1;
				}});
				addMouseListener(new AbstractMouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {
						doLogin();
					}
				});
			}});
			add(Box.createHorizontalGlue());
		}});
		
		centerPanel.add(Box.createVerticalStrut(4));
		
		centerPanel.add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
			max(this, null, 24);
			add(Box.createHorizontalGlue());
			add(new JLabel(I18n.get("login.dologinoffline")) {{
				setForeground(UIScheme.TITLE_COLOR);
				setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 18));
				//underline(this);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				addMouseListener(new AbstractMouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {
						joinOffline();
					}
				});
			}});
			add(Box.createHorizontalGlue());
		}});
		
		centerPanel.add(Box.createVerticalStrut(4));
		
		centerPanel.add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
			max(this, null, 24);
			add(Box.createHorizontalStrut(25));
			add(new JLabel(I18n.get("login.register")) {{
				setForeground(UIScheme.TITLE_COLOR);
				setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 18));
				//underline(this);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				addMouseListener(new AbstractMouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {
						LauncherUtil.onenURLInBrowser(Main.REGISTRATION_URL);
					}
				});
			}});
			add(Box.createHorizontalGlue());
			add(new JLabel(I18n.get("login.forgot")) {{
				setForeground(UIScheme.TITLE_COLOR);
				setFont(new Font(UIScheme.TEXT_FONT, Font.PLAIN, 18));
				//underline(this);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				addMouseListener(new AbstractMouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {
						LauncherUtil.onenURLInBrowser(Main.PASSWORD_RECOVER_URL);
					}
				});
			}});
			add(Box.createHorizontalStrut(25));
		}});
		
		
		centerPanel.add(Box.createVerticalGlue());
		centerPanel.revalidate();
		frame.repaint();
		
		passwordField.setActionCommand("OK");
		passwordField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if(ae.getActionCommand().equals("OK"))
					doLogin();
			}
		});
		userField.setActionCommand("OK");
		userField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if(ae.getActionCommand().equals("OK"))
					doLogin();
			}
		});
		
		if(LauncherOptions.sessionUser != null)
			passwordField.requestFocusInWindow();
		else
			userField.requestFocusInWindow();
	}
	//@formatter:on
	
	private static void max(Component c, Integer width, Integer height) {
		c.setMaximumSize(new Dimension(width != null ? width.intValue() : Short.MAX_VALUE, height != null ? height.intValue() : Short.MAX_VALUE));
	}
	
	private static void s(Component c, int width, int height) {
		c.setSize(width, height);
		Dimension d = new Dimension(width, height);
		c.setPreferredSize(d);
		c.setMaximumSize(d);
		c.setMinimumSize(d);
	}
	
	/*@SuppressWarnings({"unchecked", "rawtypes"})
	private static void underline(JLabel label) {
		Font font = label.getFont();
		Map attributes = font.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		label.setFont(font.deriveFont(attributes));
	}*/
}
