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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
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
import org.greencubes.swing.JPanelBG;
import org.greencubes.util.I18n;

public class LauncherLogin {
	
	private JFrame frame;
	private JPanel centerPanel;
	private JTextField userField;
	private JPasswordField passwordField;
	private JCheckBox autoLoginCheckBox;
	private JPanel centerPanelSpace;
	
	//@formatter:off
	public LauncherLogin() {
		frame = new JFrame(I18n.get("title"));
		frame.setIconImages(LauncherOptions.getIcons());
		frame.setUndecorated(!Main.TEST);
		frame.add(new JPanelBG("/res/login.bg.png") {{
			setPreferredSize(new Dimension(560, 384));
			setLayout(new GridBagLayout());
			setBackground(new Color(0, 1, 1, 0.5f));
			// Top line
			add(new JPanel() {{
				setPreferredSize(new Dimension(128, 25));
				setBackground(new Color(0, 1, 0, 0.5f));
			}}, gbc(1, 1, 0, 0));
			add(new JPanel() {{
				setPreferredSize(new Dimension(305, 25));
				setBackground(new Color(1, 0, 0, 0.5f));
			}}, gbc(1, 1, 1, 0));
			add(new JPanel() {{
				setPreferredSize(new Dimension(102, 25));
				setBackground(new Color(1, 0.5f, 0, 0.5f));
			}}, gbc(1, 1, 2, 0));
			add(new JPanel() {{
				setPreferredSize(new Dimension(25, 25));
				setBackground(new Color(0, 0, 1, 0.5f));
				add(new JPanelBG("/res/cross.png") {{
					setPreferredSize(new Dimension(14, 14));
					setBackground(new Color(0, 0, 1, 0.5f));
				}});
				addMouseListener(new AbstractMouseListener() {
					// TODO : Can add cross animation
					@Override
					public void mouseClicked(MouseEvent e) {
						Main.close();
					}
				});
			}}, gbc(1, 1, 3, 0));
			add(new JPanel() {{
				setPreferredSize(new Dimension(128, 25));
				setBackground(new Color(0, 1, 0.5f, 0.5f));
			}}, gbc(1, 1, 0, 1));
			add(new JPanel() {{
				setPreferredSize(new Dimension(128, 287));
				setBackground(new Color(0.5f, 1, 0, 0.5f));
			}}, gbc(1, 1, 0, 2));
			add(new JPanel() {{
				setPreferredSize(new Dimension(128, 45));
				setBackground(new Color(1, 1, 0, 0.5f));
			}}, gbc(1, 1, 0, 3));
			
			// Center panel
			add(centerPanel = new JPanel() {{
				setPreferredSize(new Dimension(305, 287));
				setBackground(new Color(0.3f, 0.6f, 0.5f, 0.5f));
				setOpaque(false);
				setLayout(new GridBagLayout());
			}}, gbc(1, 1, 1, 2));
		}}, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		//displayLogin();
		if(LauncherOptions.autoLogin) {
			LauncherOptions.loadSession();
			if(LauncherOptions.sessionUserId > 0) {
				displayProgress("Авторизация...");
				try {
					LauncherOptions.authSession();
					LauncherOptions.userInfo = LauncherUtil.sessionRequest("action=info");
					if(Main.TEST)
						System.out.println(LauncherOptions.userInfo);
					displayProgress("Загрузка...");
					launcherMain();
					return;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		displayLogin();
	}
	//@formatter:on
	
	private void launcherMain() {
		
	}
	
	private void doLogin() {
		
	}
	
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
	
	private void displayLogin() {
		centerPanel.removeAll();
		centerPanel.add(new JPanel() {{ // GC LOGO
			setPreferredSize(new Dimension(305, 42));
			setBackground(new Color(0.3f, 0.6f, 0.5f, 0.5f));
		}}, gbc(4, 1, 0, 0));
		centerPanel.add(new JPanel() {{ // Left height column
			setPreferredSize(new Dimension(20, 245));
			setBackground(new Color(0.3f, 0.6f, 0.5f, 0.5f));
		}}, gbc(1, 7, 0, 1));
		
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
			setText("Логин:");
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
			setText("Пароль:");
			setFont(new Font("ClearSans", Font.PLAIN, 18));
		}}, new GridBagConstraints() {{
			gridx = 1;
			gridy = 2;
			anchor = GridBagConstraints.LINE_END;
		}});
		
		// Login and password fields
		centerPanel.add(userField = new JTextField(0) {
			Image bg;
			{
				try {
					bg = ImageIO.read(JPanelBG.class.getResource("/res/textfield.png"));
				} catch(Exception e) {}
				setBackground(new Color(0, 0, 0, 0));
				setPreferredSize(new Dimension(158, 23));
				setBorder(new EmptyBorder(0, 5, 0, 0));
				setOpaque(false);
				setForeground(new Color(170, 255, 102));
				setCaretColor(new Color(170, 255, 102));
				setFont(new Font("ClearSans", Font.PLAIN, 16));
				if(LauncherOptions.sessionUser != null)
					setText(LauncherOptions.sessionUser);
			}
			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(bg, 0, 0, this);
				super.paintComponent(g);
			}
		}, gbc(1, 1, 2, 1));
		centerPanel.add(passwordField = new JPasswordField(0) {
			Image bg;
			{
				try {
					bg = ImageIO.read(JPanelBG.class.getResource("/res/textfield.png"));
				} catch(Exception e) {}
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
			}
		}, gbc(1, 1, 2, 2));
		
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
			setText("Забыли пароль?");
			setFont(new Font("ClearSans", Font.PLAIN, 12));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}}, new GridBagConstraints() {{
			gridx = 2;
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
			setText("Регистрация");
			setFont(new Font("ClearSans", Font.PLAIN, 12));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}}, new GridBagConstraints() {{
			gridx = 2;
			gridy = 4;
			anchor = GridBagConstraints.LINE_END;
		}});
		
		centerPanel.add(centerPanelSpace = new JPanel() {{
			setBackground(new Color(0.1f, 0.2f, 0.3f, 0.5f));
		}}, gbc(2, 1, 1, 5));
		
		// Checkboxes
		ImageIcon icon = null;
		try {
			icon = new ImageIcon(ImageIO.read(JPanelBG.class.getResource("/res/checkbox.png")));
		} catch(IOException e) {}
		final ImageIcon iconUnchecked = icon;
		try {
			icon = new ImageIcon(ImageIO.read(JPanelBG.class.getResource("/res/checkbox.checked.png")));
		} catch(IOException e) {}
		final ImageIcon iconChecked = icon;
		centerPanel.add(autoLoginCheckBox = new JCheckBox("Входить автоматически", LauncherOptions.autoLogin ? iconChecked : iconUnchecked, LauncherOptions.autoLogin) {{
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
		}}, gbc(2, 1, 1, 6));
		
		// Login button
		centerPanel.add(new JPanelBG("/res/login.png") {{
			setPreferredSize(new Dimension(253, 31));
			setBackground(new Color(0, 0, 0, 0));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new AbstractMouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					doLogin();
				}
			});
		}}, gbc(2, 1, 1, 7));
		
		centerPanel.revalidate();
		frame.repaint();
		if(LauncherOptions.sessionUser != null)
			passwordField.requestFocusInWindow();
	}
	
	private static GridBagConstraints gbc(int width, int height, int x, int y) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = width;
		c.gridheight = height;
		c.gridx = x;
		c.gridy = y;
		return c;
	}
	
}
