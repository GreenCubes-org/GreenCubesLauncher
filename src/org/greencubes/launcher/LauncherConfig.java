package org.greencubes.launcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.greencubes.swing.AbstractMouseListener;
import org.greencubes.swing.GAWTUtil;
import org.greencubes.swing.GJBoxPanel;
import org.greencubes.swing.JPanelBG;
import org.greencubes.swing.UIScheme;
import org.greencubes.util.I18n;

public class LauncherConfig {
	
	private JDialog dialog;
	
	public LauncherConfig(final JFrame frame) {
		dialog = new JDialog(frame, Dialog.ModalityType.APPLICATION_MODAL);
		
		dialog.setUndecorated(true);
		dialog.add(new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.BACKGROUND) {{
			add(new JPanelBG("/res/login.top.png") {{ // Window buttons
				setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
				s(this, 400, 32);
				add(Box.createHorizontalGlue());
				add(new GJBoxPanel(BoxLayout.PAGE_AXIS, UIScheme.EMPTY) {{ // Close button
					s(this, 30, 30);
					add(Box.createVerticalGlue());
					add(new JPanelBG("/res/cross.png") {{
						s(this, 14, 14);
						setBackground(UIScheme.EMPTY);
						addMouseListener(createCloseListener());
					}});
					add(Box.createVerticalGlue());
					addMouseListener(createCloseListener());
				}});
			}});
			add(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
				setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, UIScheme.TOP_PANEL_BG));
				JScrollPane scrollPane;
				add(scrollPane = new JScrollPane(new GJBoxPanel(BoxLayout.PAGE_AXIS, null) {{
					setBackground(UIScheme.BACKGROUND);
					add(Box.createVerticalStrut(20));
					add(new GJBoxPanel(BoxLayout.LINE_AXIS, null) {{
						add(Box.createHorizontalGlue());
						add(new JLabel(I18n.get("login.title").toUpperCase()) {{
							setForeground(UIScheme.TEXT_COLOR);
							setFont(new Font(UIScheme.TITLE_FONT, Font.PLAIN, 24));
						}});
						add(Box.createHorizontalGlue());
					}});
				}}, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {{
					s(this, 398, 400);
					setOpaque(false);
					setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
				}});
				scrollPane.getViewport().setOpaque(false);
				scrollPane.getVerticalScrollBar().setUI(GAWTUtil.customScrollBarUI(UIScheme.BACKGROUND, new Color(62, 88, 86, 255)));
				scrollPane.getVerticalScrollBar().setOpaque(false);
				add(Box.createVerticalStrut(15));
			}});
		}});
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}
	
	private MouseListener createCloseListener() {
		return new AbstractMouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		};
	}
	
	private static void s(Component c, int width, int height) {
		c.setSize(width, height);
		Dimension d = new Dimension(width, height);
		c.setPreferredSize(d);
		c.setMaximumSize(d);
		c.setMinimumSize(d);
	}
	
}
