package org.greencubes.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.greencubes.downloader.Downloader;

public class Main {
	
	public static JFrame mainFrame;
	private static final String version = "4";
	
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
			e2.printStackTrace();
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
			e2.printStackTrace();
		}
		
		
		mainFrame.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		Container content = mainFrame.getContentPane();
		content.add(background, BorderLayout.CENTER);
		mainFrame.setPreferredSize(new Dimension(560, 384));
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
	}
	
	public static void main(String[] args) {
		new Main();		
	}
	
}
