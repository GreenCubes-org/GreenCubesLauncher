package org.greencubes.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EventListener;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

import org.greencubes.main.Main;

public class GAWTUtil {
	
	private static GraphicsConfiguration bestConfig;
	
	public static JTextPane getNiceTextPane(String text, final int width) {
		JTextPane jtp = new JTextPane();
		jtp.setEditable(false);
		jtp.setOpaque(false);
		jtp.setHighlighter(null);
		Document doc = jtp.getDocument();
		try {
			doc.insertString(doc.getLength(), text, new SimpleAttributeSet());
		} catch(BadLocationException e) {
			throw new AssertionError(e);
		}
		fixtTextPaneWidth(jtp, width);
		return jtp;
	}
	
	public static void fixtTextPaneWidth(JTextPane pane, int width) {
		pane.setPreferredSize(null);
		pane.setSize(new Dimension(width, 10));
		pane.setPreferredSize(new Dimension(width, pane.getPreferredSize().height));
	}
	
	@SuppressWarnings("deprecation")
	public static int showDialog(String title, String dialogText, Object[] options, int dialogType, int maxWidth) {
		JTextPane jtp = getNiceTextPane(dialogText, maxWidth);
		JOptionPane pane = new JOptionPane(jtp, dialogType, JOptionPane.NO_OPTION, null, options, options[0]);

		pane.setInitialValue(options[0]);
		pane.setComponentOrientation(JOptionPane.getRootFrame().getComponentOrientation());
		
		int style = styleFromMessageType(dialogType);
		// We not always show dialogs in application
		// But when we do - we did it with reflection
		JDialog dialog;
		try {
			Method m = pane.getClass().getDeclaredMethod("createDialog", Component.class, String.class, Integer.TYPE);
			m.setAccessible(true);
			dialog = (JDialog) m.invoke(pane, null, title, style);
			
		} catch(Exception e) {
			throw new AssertionError(e);
		}
		dialog.setAlwaysOnTop(true);
		pane.selectInitialValue();
		dialog.show();
		dialog.dispose();
		
		Object selectedValue = pane.getValue();
		
		if(selectedValue == null)
			return JOptionPane.CLOSED_OPTION;
		for(int counter = 0, maxCounter = options.length; counter < maxCounter; counter++) {
			if(options[counter].equals(selectedValue))
				return counter;
		}
		return JOptionPane.CLOSED_OPTION;
		
		//return JOptionPane.showOptionDialog(null, jtp, title, JOptionPane.NO_OPTION, dialogType, null, options, options[0]);
	}
	
	private static int styleFromMessageType(int messageType) {
        switch (messageType) {
        case JOptionPane.ERROR_MESSAGE:
            return JRootPane.ERROR_DIALOG;
        case JOptionPane.QUESTION_MESSAGE:
            return JRootPane.QUESTION_DIALOG;
        case JOptionPane.WARNING_MESSAGE:
            return JRootPane.WARNING_DIALOG;
        case JOptionPane.INFORMATION_MESSAGE:
            return JRootPane.INFORMATION_DIALOG;
        case JOptionPane.PLAIN_MESSAGE:
        default:
            return JRootPane.PLAIN_DIALOG;
        }
    }
	
	public static ScrollBarUI customScrollBarUI(final Color customBackgroundColor, final Color customThumbColor) {
		return new BasicScrollBarUI() {
			@Override
			protected void installComponents() {
				super.installComponents();
				scrollbar.remove(decrButton);
				decrButton.setPreferredSize(new Dimension(0, 0));
				scrollbar.remove(incrButton);
			}
			
			@Override
			protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
				g.translate(thumbBounds.x, thumbBounds.y);
				g.setColor(customThumbColor);
				g.fillRect(0, 0, thumbBounds.width, thumbBounds.height);
				g.translate(-thumbBounds.x, -thumbBounds.y);
			}
			
			@Override
			protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
				g.setColor(customBackgroundColor);
		        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
			}
		};
	}
	
	public static void removeMouseListeners(JComponent c) {
		EventListener handlers[] = c.getListeners(MouseListener.class);
		for(int i = 0; i < handlers.length; i++)
			c.removeMouseListener((MouseListener) handlers[i]);
		handlers = c.getListeners(MouseMotionListener.class);
		for(int i = 0; i < handlers.length; i++)
			c.removeMouseMotionListener((MouseMotionListener) handlers[i]);
	}
	
	public static String toURL(String str) {
		try {
			return new URL(str).toExternalForm();
		} catch(MalformedURLException exception) {
			return null;
		}
	}
	
	public static MouseListener createMinimizeListener(final Frame frame) {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				frame.setState(Frame.ICONIFIED);
			}
		};
	}
	
	public static MouseListener createCloseListener(final Frame lastFrame) {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				lastFrame.dispose();
				Main.close();
			}
		};
	}
	
	public static MouseListener createMaximizeListener(final UndecoratedJFrame frame) {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int state = frame.getExtendedState();
				if((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
					frame.setExtendedState(Frame.NORMAL);
					frame.setResizable(true);
				} else {
					frame.maximize();
					frame.setResizable(false);
				}
			}
		};
	}
	
	public static boolean safeTransparentBackground(Component c, Color newBg) {
		try {
			c.setBackground(newBg);
			return true;
		} catch(Exception e) {
			newBg = new Color(newBg.getRed(), newBg.getGreen(), newBg.getBlue(), 255);
			c.setBackground(newBg);
		}
		return false;
	}
	
	public static Border safePopupBorder() {
		return isTranslucencySupported() ? popupBorder() : mimicPopupBorder();
	}
	
	public static Border mimicPopupBorder() {
		return BorderFactory.createLineBorder(new Color(0, 0, 0, 255), 1);
	}
	
	public static Border popupBorder() {
		return new EmptyBorder(new Insets(23, 13, 13, 13)) {
			Image icon;
			{
				try {
					icon = ImageIO.read(GAWTUtil.class.getResource("/res/border.popup.png"));
				} catch(IOException e) {
					throw new AssertionError(e);
				}
			}
			
			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				g.translate(x, y);
				
				// Top-left
				g.drawImage(icon, 0, 10, 13, 10 + 13, 1, 11, 14, 24, c);
				// Top-left padding
				g.drawImage(icon, 13, 10, 13 + 7, 10 + 13, 15, 11, 15 + 7, 11 + 13, c);
				g.drawImage(icon, 13 + 7, 10, 13 + 7 + 7, 10 + 13, 23, 11, 23 + 1, 11 + 13, c);
				// Pimpochka
				g.drawImage(icon, 13 + 7 + 7, 0, 13 + 7 + 7 + 38, 23, 25, 1, 25 + 38, 1 + 23, c);
				
				// Top-right
				g.drawImage(icon, width - 13, 10, width, 10 + 13, 74, 11, 74 + 13, 11 + 13, c);
				// Top-right padding
				g.drawImage(icon, width - 13 - 7, 10, width - 13, 10 + 13, 66, 11, 66 + 7, 11 + 13, c);
				// Top
				g.drawImage(icon, 13 + 7 + 7 + 38, 10, width - 13 - 7, 10 + 13, 64, 11, 64 + 1, 11 + 13, c);
				// Right-top padding
				g.drawImage(icon, width - 13, 23, width, 23 + 7, 74, 25, 74 + 13, 25 + 7, c);
				// Right
				g.drawImage(icon, width - 13, 23 + 7, width, height - 13 - 7, 74, 33, 74 + 13, 33 + 1, c);
				// Right-bottom padding
				g.drawImage(icon, width - 13, height - 13 - 7, width, height - 13, 74, 35, 74 + 13, 35 + 7, c);
				// Right-bottom
				g.drawImage(icon, width - 13, height - 13, width, height, 74, 43, 74 + 13, 43 + 13, c);
				// Bottom-right padding
				g.drawImage(icon, width - 13 - 7, height - 13, width - 13, height, 66, 43, 66 + 7, 43 + 13, c);
				// Bottom
				g.drawImage(icon, 13 + 7, height - 13, width - 13 - 7, height, 23, 43, 23 + 1, 43 + 13, c);
				// Bottom-left padding
				g.drawImage(icon, 13, height - 13, 13 + 7, height, 15, 43, 15 + 7, 43 + 13, c);
				// Bottom-left
				g.drawImage(icon, 0, height - 13, 13, height, 1, 43, 1 + 13, 43 + 13, c);
				// Left-bottom padding
				g.drawImage(icon, 0, height - 13 - 7, 13, height - 13, 1, 35, 1 + 13, 35 + 7, c);
				// Left
				g.drawImage(icon, 0, 10 + 13 + 7, 13, height - 13 - 7, 1, 33, 1 + 13, 33 + 1, c);
				// Left-top padding
				g.drawImage(icon, 0, 10 + 13, 13, 10 + 13 + 7, 1, 25, 1 + 13, 25 + 7, c);
			}
			
			@Override
			public boolean isBorderOpaque() {
				return false;
			}
		};
	}
	
	public static boolean isTranslucencySupported() {
		return getBestConfiguration().isTranslucencyCapable();
	}
	
	public static GraphicsConfiguration getBestConfiguration() {
		if(bestConfig != null)
			return bestConfig;
		GraphicsConfiguration defaultConfig = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		if(defaultConfig.isTranslucencyCapable())
			return bestConfig = defaultConfig;
		GraphicsConfiguration[] configs = defaultConfig.getDevice().getConfigurations();
        for(int j = 0; j < configs.length; j++) {
            if(configs[j].isTranslucencyCapable())
                return bestConfig = configs[j];
        }
        return bestConfig = defaultConfig;
	}
}
