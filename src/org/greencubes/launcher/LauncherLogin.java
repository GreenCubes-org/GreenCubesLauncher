package org.greencubes.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.greencubes.main.Main;
import org.greencubes.swing.AbstractMouseListener;
import org.greencubes.swing.AbstractWindowListener;
import org.greencubes.swing.GAWTUtil;
import org.greencubes.swing.JPanelBG;
import org.greencubes.util.I18n;
import org.greencubes.util.Util;

public class LauncherLogin {
	
	private JFrame frame;
	private JPanel centerPanel;
	private JTextField userField;
	private JPasswordField passwordField;
	private JCheckBox autoLoginCheckBox;
	private String errorString;
	private JTextPane errorPane;
	
	//@formatter:off
	/**
	 * Should not be invoked in AWT thread
	 */
	public LauncherLogin(JFrame previousFrame) {
		frame = new JFrame(I18n.get("title"));
		frame.setIconImages(LauncherOptions.getIcons());
		frame.setUndecorated(true);
		frame.add(new JPanelBG("/res/login.bg.png") {{
			setPreferredSize(new Dimension(560, 384));
			setLayout(new GridBagLayout());
			setBackground(new Color(0, 1, 1, 0));
			// Top line
			add(new JPanel() {{
				setPreferredSize(new Dimension(128, 25));
				setBackground(new Color(0, 1, 0, 0));
			}}, gbc(1, 1, 0, 0));
			add(new JPanel() {{
				setPreferredSize(new Dimension(305, 25));
				setBackground(new Color(1, 0, 0, 0));
			}}, gbc(1, 1, 1, 0));
			add(new JPanel() {{
				setPreferredSize(new Dimension(102, 25));
				setBackground(new Color(1, 0.5f, 0, 0));
			}}, gbc(1, 1, 2, 0));
			add(new JPanel() {{
				setPreferredSize(new Dimension(25, 25));
				setBackground(new Color(0, 0, 0, 0));
				add(new JPanelBG("/res/cross.png") {{
					setPreferredSize(new Dimension(14, 14));
					setBackground(new Color(0, 0, 0, 0));
					addMouseListener(GAWTUtil.createCloseListener(frame));
				}});
				addMouseListener(GAWTUtil.createCloseListener(frame));
			}}, gbc(1, 1, 3, 0));
			add(new JPanel() {{
				setPreferredSize(new Dimension(128, 25));
				setBackground(new Color(0, 1, 0.5f, 0));
			}}, gbc(1, 1, 0, 1));
			add(new JPanel() {{
				setPreferredSize(new Dimension(128, 287));
				setBackground(new Color(0.5f, 1, 0, 0));
			}}, gbc(1, 1, 0, 2));
			add(new JPanel() {{
				setPreferredSize(new Dimension(128, 45));
				setBackground(new Color(1, 1, 0, 0));
			}}, gbc(1, 1, 0, 3));
			
			// Center panel
			add(centerPanel = new JPanel() {{
				setPreferredSize(new Dimension(305, 287));
				setBackground(new Color(0.3f, 0.6f, 0.5f, 0));
				setOpaque(false);
				setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			}}, gbc(1, 1, 1, 2));
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
					public void run() {
						displayProgress(I18n.get("login.loading"));
						if(LauncherOptions.sessionUser != null && !LauncherOptions.sessionUser.equals(userField.getText()))
							LauncherOptions.logOff();
						LauncherOptions.sessionUser = userField.getText();
						launcherMain();
					}
				}.start();				
			} else {
				if(errorPane != null)
					errorPane.setText(I18n.get("login.nousername"));
			}
		}
	}
	
	private void doLogin() {
		if(userField != null && passwordField != null) {
			if(userField.getText().length() > 0 && passwordField.getPassword().length > 0) {
				// goLogin() runs in window thread, so we need to start new thread to allow progress update
				new Thread() {
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
							errorString = I18n.get("login.exception", e.getLocalizedMessage());
							if(Main.TEST)
								e.printStackTrace();
						} catch(AuthError e) {
							errorString = I18n.get("login.exception", I18n.get("login.error." + e.errorCode));
							if(Main.TEST)
								e.printStackTrace();
						}
						displayLogin();
					}
				}.start();
			} else if(userField.getText().length() == 0 && passwordField.getPassword().length == 0) {
				if(errorPane != null)
					errorPane.setText(I18n.get("login.nousernamepassword"));
			} else if(userField.getText().length() == 0) {
				if(errorPane != null)
					errorPane.setText(I18n.get("login.nousername"));
			} else if(passwordField.getPassword().length == 0) {
				if(errorPane != null)
					errorPane.setText(I18n.get("login.nopassword"));
			}
		}
	}
	
	//@formatter:off
	private void displayProgress(final String progressString) {
		centerPanel.removeAll();
		centerPanel.invalidate();
		centerPanel.add(new JPanel() {{
			setOpaque(false);
			setLayout(new GridBagLayout());
			add(new JLabel() {{
				setOpaque(false);
				setAlignmentX(JLabel.CENTER_ALIGNMENT);
				setBackground(new Color(0, 0, 0, 0));
				setForeground(new Color(25, 97, 14, 255));
				setText(progressString);
				setFont(new Font("ClearSans", Font.BOLD, 30));
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
		centerPanel.removeAll();
		centerPanel.add(new JPanel() {{ // GC LOGO
			s(this, 305, 42);
			setBackground(new Color(0, 0, 0, 0));
		}});
		final String savedUser = userField != null ? userField.getText() : LauncherOptions.sessionUser;
		centerPanel.add(new JPanel() {{
			setOpaque(false);
			setLayout(new GridBagLayout());
			setBackground(Util.debugColor());
			add(new JLabel() {{
				setOpaque(false);
				setAlignmentX(JLabel.CENTER_ALIGNMENT);
				setBackground(Util.debugColor());
				setForeground(new Color(25, 97, 14, 255));
				setText(I18n.get("login.login"));
				setFont(new Font("ClearSans", Font.PLAIN, 18));
			}}, gbc(1, 1, 0, 0));
			add(userField = new JTextField(0) {
				Image bg;
				{
					try {
						bg = ImageIO.read(JPanelBG.class.getResource("/res/textfield.png"));
					} catch(Exception e) {
					}
					setBackground(Util.debugColor());
					s(this, 158, 23);
					setBorder(new EmptyBorder(0, 5, 0, 0));
					setOpaque(false);
					setForeground(new Color(170, 255, 102));
					setCaretColor(new Color(170, 255, 102));
					setFont(new Font("ClearSans", Font.PLAIN, 16));
					if(savedUser != null)
						setText(savedUser);
				}
				
				@Override
				public void paintComponent(Graphics g) {
					g.drawImage(bg, 0, 0, this);
					super.paintComponent(g);
			}}, gbc(1, 1, 1, 0));
			add(new JLabel() {{
				setOpaque(false);
				setAlignmentX(JLabel.CENTER_ALIGNMENT);
				setBackground(Util.debugColor());
				setForeground(new Color(25, 97, 14, 255));
				setText(I18n.get("login.password"));
				setFont(new Font("ClearSans", Font.PLAIN, 18));
			}}, gbc(1, 1, 0, 1));
			add(passwordField = new JPasswordField(0) {
				Image bg;
				{
					try {
						bg = ImageIO.read(JPanelBG.class.getResource("/res/textfield.png"));
					} catch(Exception e) {
					}
					setBackground(Util.debugColor());
					s(this, 158, 23);
					setBorder(new EmptyBorder(0, 5, 0, 0));
					setOpaque(false);
					setForeground(new Color(170, 255, 102));
					setCaretColor(new Color(170, 255, 102));
					setFont(new Font("ClearSans", Font.PLAIN, 16));
				}
				
				@Override
				public void paintComponent(Graphics g) {
					g.drawImage(bg, 0, 0, this);
					super.paintComponent(g);
			}}, gbc(1, 1, 1, 1));
		}});

		// Forgot password and registration
		centerPanel.add(new JPanel() {{
			setOpaque(false);
			setBackground(Util.debugColor());
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			add(Box.createHorizontalGlue());
			add(new JPanel() {{
				setLayout(new BorderLayout());
				setOpaque(false);
				setBackground(new Color(0, 0, 0, 0));
				add(new JTextPane() {{
					setHighlighter(null);
					setOpaque(false);
					StyledDocument doc = getStyledDocument();
					SimpleAttributeSet center = new SimpleAttributeSet();
					StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
					StyleConstants.setUnderline(center, true);
					StyleConstants.setLineSpacing(center, -1f);
					doc.setParagraphAttributes(0, doc.getLength(), center, false);
					setBackground(new Color(0, 0, 0, 0));
					setForeground(new Color(0, 18, 255, 255));
					setEditable(false);
					setFont(new Font("ClearSans", Font.PLAIN, 12));
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					if(LauncherUtil.canOpenBrowser()) {
						setText(I18n.get("login.forgot"));
						addMouseListener(new AbstractMouseListener() {
							@Override
							public void mouseClicked(MouseEvent e) {
								LauncherUtil.onenURLInBrowser(Main.PASSWORD_RECOVER_URL);
							}
						});
					}
				}}, BorderLayout.LINE_END);	
			}});
			add(new JPanel() {{
				s(this, 10, 10);
				setBackground(new Color(0, 0, 0, 0));
			}});
		}});
		
		centerPanel.add(new JPanel() {{
			setOpaque(false);
			setBackground(Util.debugColor());
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			add(Box.createHorizontalGlue());
			add(new JPanel() {{
				setLayout(new BorderLayout());
				setOpaque(false);
				setBackground(new Color(0, 0, 0, 0));
				add(new JTextPane() {{
					setOpaque(false);
					setHighlighter(null);
					StyledDocument doc = getStyledDocument();
					SimpleAttributeSet center = new SimpleAttributeSet();
					StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
					StyleConstants.setUnderline(center, true);
					StyleConstants.setLineSpacing(center, -1f);
					doc.setParagraphAttributes(0, doc.getLength(), center, false);
					setBackground(new Color(0, 0, 0, 0));
					setForeground(new Color(0, 18, 255, 255));
					setEditable(false);
					setFont(new Font("ClearSans", Font.PLAIN, 12));
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					if(LauncherUtil.canOpenBrowser()) {
						setText(I18n.get("login.register"));
						addMouseListener(new AbstractMouseListener() {
							@Override
							public void mouseClicked(MouseEvent e) {
								LauncherUtil.onenURLInBrowser(Main.REGISTRATION_URL);
							}
						});
					}
					setMargin(new Insets(0, 0, 0, 0));
				}}, BorderLayout.LINE_END);
			}});
			add(new JPanel() {{
				s(this, 10, 10);
				setBackground(new Color(0, 0, 0, 0));
			}});
		}});
		
		centerPanel.add(new JPanel() {{
			setOpaque(false);
			setBackground(Util.debugColor());
			add(errorPane = new JTextPane() {{
				setHighlighter(null);
				setOpaque(false);
				StyledDocument doc = getStyledDocument();
				SimpleAttributeSet center = new SimpleAttributeSet();
				StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				setBackground(Util.debugColor());
				setForeground(new Color(0, 0, 0, 255));
				setEditable(false);
				if(errorString != null) {
					setText(errorString);
					errorString = null;
				}
				setFont(new Font("ClearSans", Font.PLAIN, 12));
			}});
		}});
		
		// Checkboxes
		ImageIcon icon = null;
		try {
			icon = new ImageIcon(ImageIO.read(JPanelBG.class.getResource("/res/checkbox.png")));
		} catch(IOException e) {
		}
		final ImageIcon iconUnchecked = icon;
		try {
			icon = new ImageIcon(ImageIO.read(JPanelBG.class.getResource("/res/checkbox.checked.png")));
		} catch(IOException e) {
		}
		final ImageIcon iconChecked = icon;
		centerPanel.add(new JPanel() {{
			setOpaque(false);
			setBackground(new Color(0, 0, 0, 0));
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			add(new JPanel() {{
				s(this, 25, 5);
				setBackground(new Color(0, 0, 0, 0));
			}});
			add(autoLoginCheckBox = new JCheckBox(I18n.get("login.autologin"), LauncherOptions.autoLogin ? iconChecked : iconUnchecked, LauncherOptions.autoLogin) {{
				setBackground(Util.debugColor());
				setOpaque(false);
				setFont(new Font("ClearSans", Font.PLAIN, 16));
				setForeground(new Color(25, 97, 14, 255));
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
			add(Box.createHorizontalGlue());
		}});
		
		
		// Login button
		centerPanel.add(new JPanelBG("/res/button.png") {{
			paddingTop = 5;
			s(this, 253, 35);
			setBackground(new Color(0, 0, 0, 0));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new AbstractMouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					doLogin();
				}
			});
			add(new JTextPane() {{
				setHighlighter(null);
				setOpaque(false);
				StyledDocument doc = getStyledDocument();
				SimpleAttributeSet center = new SimpleAttributeSet();
				StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				setBackground(Util.debugColor());
				setForeground(new Color(170, 255, 102));
				setEditable(false);
				setText(I18n.get("login.dologin"));
				setFont(new Font("ClearSans", Font.PLAIN, 16));
				addMouseListener(new AbstractMouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {
						doLogin();
					}
				});
			}}, BorderLayout.PAGE_START);
		}});
		// Login button
		centerPanel.add(new JPanelBG("/res/button.png") {{
			paddingTop = 5;
			s(this, 253, 35);
			setBackground(new Color(0, 0, 0, 0));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new AbstractMouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					joinOffline();
				}
			});
			add(new JTextPane() {{
				setHighlighter(null);
				setOpaque(false);
				StyledDocument doc = getStyledDocument();
				SimpleAttributeSet center = new SimpleAttributeSet();
				StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				setBackground(Util.debugColor());
				setForeground(new Color(170, 255, 102));
				setEditable(false);
				setText(I18n.get("login.dologinoffline"));
				setFont(new Font("ClearSans", Font.PLAIN, 16));
				addMouseListener(new AbstractMouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {
						joinOffline();
					}
				});
			}}, BorderLayout.PAGE_START);
		}});
		
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
		
		centerPanel.revalidate();
		frame.repaint();
		if(LauncherOptions.sessionUser != null)
			passwordField.requestFocusInWindow();
		else
			userField.requestFocusInWindow();
	}
	//@formatter:on
	
	private static GridBagConstraints gbc(int width, int height, int x, int y) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = width;
		c.gridheight = height;
		c.gridx = x;
		c.gridy = y;
		return c;
	}
	
	private static void s(Component c, int width, int height) {
		c.setSize(width, height);
		Dimension d = new Dimension(width, height);
		c.setPreferredSize(d);
		c.setMaximumSize(d);
		c.setMinimumSize(d);
	}
}
