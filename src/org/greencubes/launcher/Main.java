package org.greencubes.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.greencubes.downloader.Downloader;

public class Main {
	
	public static JFrame mainFrame;
	//private static final String version = "4";
	
	private Downloader downloader = new Downloader("https://auth.greencubes.org/mc/");
	
	private Main() {
		downloader.addServer("https://auth1.greencubes.org/mc/");
		mainFrame = new JFrame("GreenCubes Launcher");
		mainFrame.setUndecorated(true);
		mainFrame.setAlwaysOnTop(true);
		try {
			ArrayList<BufferedImage> icons = new ArrayList<BufferedImage>(5);
			icons.add(ImageIO.read(Main.class.getResource("/gcico32x32.png")));
			icons.add(ImageIO.read(Main.class.getResource("/gcico48x48.png")));
			icons.add(ImageIO.read(Main.class.getResource("/gcico64x64.png")));
			icons.add(ImageIO.read(Main.class.getResource("/gcico128x128.png")));
			icons.add(ImageIO.read(Main.class.getResource("/gcico256x256.png")));
			mainFrame.setIconImages(icons);
		} catch(IOException e1) {
			e1.printStackTrace();
		}
		mainFrame.setBackground(new Color(1.0F, 1.0F, 1.0F, 0.0F));
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel background = null;
		try {
			background = new JPanel() {
				private static final long serialVersionUID = 1L;
				private Image image = ImageIO.read(Main.class.getResource("/bg.png"));
				
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.drawImage(image, 0, 0, this);
				}
			};
		} catch(IOException e2) {
			throw new AssertionError(e2);
		}
		JPanel button = null;
		try {
			button = new JPanel() {
				private static final long serialVersionUID = 1L;
				private Image image = ImageIO.read(Main.class.getResource("/login.png"));
				
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.drawImage(image, 0, 0, this);
				}
			};
		} catch(IOException e2) {
			throw new AssertionError(e2);
		}
		button.setBackground(new Color(0, 0, 0, 1));
		button.setPreferredSize(new Dimension(253, 31));
		JPanel cross = null;
		try {
			cross = new JPanel() {
				private static final long serialVersionUID = 1L;
				private Image image = ImageIO.read(Main.class.getResource("/cross.png"));
				
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.drawImage(image, 6, 6, this);
				}
			};
		} catch(IOException e2) {
			throw new AssertionError(e2);
		}
		cross.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		cross.setPreferredSize(new Dimension(26, 26));
		cross.addMouseListener(new MouseListener() {
			// Вот бы можно было сделать это лучше...
			@Override
            public void mouseClicked(MouseEvent e) {
				
            }
			@Override
            public void mousePressed(MouseEvent e) {
				System.exit(0);
            }

			@Override
            public void mouseReleased(MouseEvent e) {
            }

			@Override
            public void mouseEntered(MouseEvent e) {
            }

			@Override
            public void mouseExited(MouseEvent e) {
            }
		});
		
		/* Main Layer */
		mainFrame.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		mainFrame.setPreferredSize(new Dimension(560, 384));
		//mainFrame.setLayout(new GridLayout());
		mainFrame.add(background, BorderLayout.CENTER);
		
		background.setPreferredSize(new Dimension(560, 384));
		background.setOpaque(false);
		
		/* Main grid */
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		background.setLayout(layout);
		
		/* Top left corner to layout elements */
		JPanel panel = new JPanel();
		panel.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(128, 48));
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 2;
		background.add(panel, c);
		c.gridheight = 1;
		/* Bottom right corner to layout elements */
		panel = new JPanel();
		panel.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(128, 48));
		c.gridx = 2;
		c.gridy = 3;
		background.add(panel, c);
		
		/* Cross button */
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		background.add(cross, c);
		c.anchor = GridBagConstraints.CENTER;
		
		/* Inner grid */
		JPanel inner = new JPanel();
		inner.setBackground(new Color(0F, .5F, .5F, 0F));
		inner.setPreferredSize(new Dimension(304, 288));
		inner.setOpaque(false);
		layout = new GridBagLayout();
		inner.setLayout(layout);
		c.gridx = 1;
		c.gridy = 2;
		background.add(inner, c);
		
		/* Top padding */
		panel = new JPanel();
		panel.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(304, 50));
		c.gridx = 0;
		c.gridy = 0;
		inner.add(panel, c);
		/* Login row */
		JPanel loginRow = new JPanel();
		loginRow.setOpaque(false);
		loginRow.setLayout(new GridBagLayout());
		loginRow.setBackground(new Color(1.0F, .5F, 0.5F, 0F));
		loginRow.setPreferredSize(new Dimension(304, 23));
		c.gridx = 0;
		c.gridy = 1;
		inner.add(loginRow, c);
		/* Login password fields padding */
		panel = new JPanel();
		panel.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(304, 12));
		c.gridx = 0;
		c.gridy = 2;
		inner.add(panel, c);
		/* Password row */
		JPanel passwordRow = new JPanel();
		passwordRow.setOpaque(false);
		passwordRow.setLayout(new GridBagLayout());
		passwordRow.setBackground(new Color(1.0F, .5F, 0.5F, 0F));
		passwordRow.setPreferredSize(new Dimension(304, 23));
		c.gridx = 0;
		c.gridy = 3;
		inner.add(passwordRow, c);
		/* Button padding */
		panel = new JPanel();
		panel.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(304, 132));
		c.gridx = 0;
		c.gridy = 4;
		inner.add(panel, c);
		
		/* Login button */
		c.gridx = 0;
		c.gridy = 5;
		inner.add(button, c);
		/* After button padding */
		panel = new JPanel();
		panel.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(304, 17));
		c.gridx = 0;
		c.gridy = 6;
		inner.add(panel, c);
		
		/* Login row */
		panel = new JPanel();
		panel.setBackground(new Color(0.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(116, 23));
		c.gridx = 0;
		c.gridy = 0;
		loginRow.add(panel, c);
		JTextField loginField = new JTextField(0);
		loginField.setPreferredSize(new Dimension(158, 23));
		c.gridx = 1;
		c.gridy = 0;
		loginRow.add(loginField, c);
		loginField.setBorder(new EmptyBorder(0, 5, 0, 0));
		loginField.setOpaque(false);
		loginField.setForeground(new Color(170, 255, 102));
		loginField.setCaretColor(new Color(170, 255, 102));
		loginField.setFont(loginField.getFont().deriveFont(Font.BOLD));
		panel = new JPanel();
		panel.setBackground(new Color(0.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(30, 23));
		c.gridx = 2;
		c.gridy = 0;
		loginRow.add(panel, c);
		
		/* Password row */
		panel = new JPanel();
		panel.setBackground(new Color(0.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(116, 23));
		c.gridx = 0;
		c.gridy = 0;
		passwordRow.add(panel, c);
		JPasswordField passwordField = new JPasswordField(0);
		passwordField.setPreferredSize(new Dimension(158, 23));
		c.gridx = 1;
		c.gridy = 0;
		passwordRow.add(passwordField, c);
		passwordField.setBorder(new EmptyBorder(0, 5, 0, 0));
		passwordField.setOpaque(false);
		passwordField.setForeground(new Color(170, 255, 102));
		passwordField.setCaretColor(new Color(170, 255, 102));
		passwordField.setFont(loginField.getFont());
		panel = new JPanel();
		panel.setBackground(new Color(0.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(30, 23));
		c.gridx = 2;
		c.gridy = 0;
		passwordRow.add(panel, c);
		
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
	}
	
	public static void main(String[] args) {
		new Main();		
	}
	
}
