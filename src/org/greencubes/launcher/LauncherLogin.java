package org.greencubes.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
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
import org.greencubes.swing.JPanelBG;
import org.greencubes.util.I18n;

public class LauncherLogin {
	
	private JFrame frame;
	private JPanel centerPanel;
	private JTextField userField;
	private JPasswordField passwordField;
	private JCheckBox autoLoginCheckBox;
	private String errorString;
	
	//@formatter:off
	public LauncherLogin(JFrame previousFrame) {
		frame = new JFrame(I18n.get("title"));
		frame.setIconImages(LauncherOptions.getIcons());
		frame.setUndecorated(!Main.TEST);
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
				}});
				addMouseListener(new AbstractMouseListener() {
					// TODO : Can add cross animation
					@Override
					public void mouseClicked(MouseEvent e) {
						frame.dispose();
						Main.close();
					}
				});
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
				setLayout(new GridBagLayout());
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
				} catch(Exception e) {
					if(Main.TEST)
						e.printStackTrace();
				}
			}
		}
		displayLogin();
	}
	//@formatter:on
	
	private void launcherMain() {
		// It is already in other thread
		new LauncherMain(frame); // Send current frame so next window can destroy it when ready
	}
	
	private void doLogin() {
		if(userField != null && passwordField != null && userField.getText().length() > 0 && passwordField.getPassword().length > 0) {
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
		}
	}
	
	//@formatter:off
	private void displayProgress(final String progressString) {
		centerPanel.removeAll();
		centerPanel.invalidate();
		centerPanel.add(new JTextPane() {{
				setOpaque(false);
				StyledDocument doc = getStyledDocument();
				SimpleAttributeSet center = new SimpleAttributeSet();
				StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				setBackground(new Color(0, 0, 0, 0));
				setForeground(new Color(25, 97, 14, 255));
				setEditable(false);
				setText(progressString);
				setFont(new Font("ClearSans", Font.BOLD, 30));
			}}, gbc(1, 1, 0, 0));
		centerPanel.revalidate();
		frame.repaint();
	}
	//@formatter:on
	
	//@formatter:off
	private void displayLogin() {
		centerPanel.removeAll();
		centerPanel.add(new JPanel() {{ // GC LOGO
				setPreferredSize(new Dimension(305, 42));
				setBackground(new Color(0.3f, 0.6f, 0.5f, 0));
			}}, gbc(4, 1, 0, 0));
		centerPanel.add(new JPanel() {{ // Left height column
				setPreferredSize(new Dimension(10, 245));
				setBackground(new Color(0.3f, 0.6f, 0.5f, 0));
			}}, gbc(1, 7, 0, 1));
		centerPanel.add(new JPanel() {{ // Right height column
				setPreferredSize(new Dimension(10, 245));
				setBackground(new Color(0.3f, 0.6f, 0.5f, 0));
			}}, gbc(1, 7, 3, 1));
		
		// Login and password captions
		centerPanel.add(new JTextPane() {{
				setOpaque(false);
				StyledDocument doc = getStyledDocument();
				SimpleAttributeSet center = new SimpleAttributeSet();
				StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				setBackground(new Color(0, 0, 0, 0));
				setForeground(new Color(25, 97, 14, 255));
				setEditable(false);
				setText(I18n.get("login.login"));
				setFont(new Font("ClearSans", Font.PLAIN, 18));
			}}, new GridBagConstraints() {{
				gridx = 1;
				gridy = 1;
				anchor = GridBagConstraints.LINE_END;
			}});
		centerPanel.add(new JTextPane() {{
				setOpaque(false);
				StyledDocument doc = getStyledDocument();
				SimpleAttributeSet center = new SimpleAttributeSet();
				StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				setBackground(new Color(0, 0, 0, 0));
				setForeground(new Color(25, 97, 14, 255));
				setEditable(false);
				setText(I18n.get("login.password"));
				setFont(new Font("ClearSans", Font.PLAIN, 18));
			}}, new GridBagConstraints() {{
				gridx = 1;
				gridy = 2;
				anchor = GridBagConstraints.LINE_END;
			}});
		
		final String savedUser = userField != null ? userField.getText() : LauncherOptions.sessionUser;
		
		// Login and password fields
		centerPanel.add(userField = new JTextField(0) {
			Image bg;
			{
				try {
					bg = ImageIO.read(JPanelBG.class.getResource("/res/textfield.png"));
				} catch(Exception e) {
				}
				setBackground(new Color(0, 0, 0, 0));
				setPreferredSize(new Dimension(158, 23));
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
			}}, gbc(1, 1, 2, 1));
		centerPanel.add(passwordField = new JPasswordField(0) {
			Image bg;
			{
				try {
					bg = ImageIO.read(JPanelBG.class.getResource("/res/textfield.png"));
				} catch(Exception e) {
				}
				setBackground(new Color(0, 0, 0, 0));
				setPreferredSize(new Dimension(158, 23));
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
			}}, gbc(1, 1, 2, 2));
		
		// Forgot password and registration
		centerPanel.add(new JTextPane() {{
				setOpaque(false);
				StyledDocument doc = getStyledDocument();
				SimpleAttributeSet center = new SimpleAttributeSet();
				StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
				StyleConstants.setUnderline(center, true);
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
			}}, new GridBagConstraints() {{
				gridx = 1;
				gridwidth = 2;
				gridy = 3;
				anchor = GridBagConstraints.LINE_END;
			}});
		centerPanel.add(new JTextPane() {{
				setOpaque(false);
				StyledDocument doc = getStyledDocument();
				SimpleAttributeSet center = new SimpleAttributeSet();
				StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
				StyleConstants.setUnderline(center, true);
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
			}}, new GridBagConstraints() {{
				gridx = 1;
				gridwidth = 2;
				gridy = 4;
				anchor = GridBagConstraints.LINE_END;
			}});
		
		centerPanel.add(new JPanel() {{
				setOpaque(false);
				setBackground(new Color(0.1f, 0.2f, 0.3f, 0));
				setPreferredSize(new Dimension(285, 50));
				if(errorString != null) {
					add(new JTextPane() {{
							setOpaque(false);
							StyledDocument doc = getStyledDocument();
							SimpleAttributeSet center = new SimpleAttributeSet();
							StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
							doc.setParagraphAttributes(0, doc.getLength(), center, false);
							setBackground(new Color(0, 0, 0, 0));
							setForeground(new Color(0, 0, 0, 255));
							setEditable(false);
							setText(errorString);
							setFont(new Font("ClearSans", Font.PLAIN, 12));
						}
					});
					errorString = null;
				}
			}}, gbc(2, 1, 1, 5));
		
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
		centerPanel.add(autoLoginCheckBox = new JCheckBox(I18n.get("login.autologin"), LauncherOptions.autoLogin ? iconChecked : iconUnchecked, LauncherOptions.autoLogin) {{
				setBackground(new Color(0, 0, 0, 0));
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
				setToolTipText(I18n.get("login.autologin.tip"));
			}}, gbc(2, 1, 1, 6));
		
		// Login button
		centerPanel.add(new JPanelBG("/res/button.png") {{
				paddingTop = 5;
				setPreferredSize(new Dimension(253, 40));
				setBackground(new Color(0.13f, 0.4f, 0.6f, 0));
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				addMouseListener(new AbstractMouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {
						doLogin();
					}
				});
				add(new JTextPane() {{
						setOpaque(false);
						StyledDocument doc = getStyledDocument();
						SimpleAttributeSet center = new SimpleAttributeSet();
						StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
						doc.setParagraphAttributes(0, doc.getLength(), center, false);
						setBackground(new Color(0.13f, 0.2f, 0.1f, 0));
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
			}}, gbc(2, 1, 1, 7));
		
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
}
